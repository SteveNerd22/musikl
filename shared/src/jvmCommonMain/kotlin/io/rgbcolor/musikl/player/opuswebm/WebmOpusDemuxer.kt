package io.rgbcolor.musikl.player.opuswebm

import java.io.EOFException
import java.io.InputStream
import java.lang.Float

/** Metadati della traccia audio Opus trovata dentro Tracks. */
data class OpusTrackInfo(
    val trackNumber: Long,
    val sampleRate: Int,
    val channels: Int,
)

/**
 * Demuxer WebM/EBML minimale, pensato solo per estrarre una traccia audio Opus
 * da uno stream sequenziale (no seek, no random access — adatto a leggere
 * direttamente da una connessione HTTP).
 *
 * NON supporta: video, sottotitoli, lacing nei SimpleBlock (assume un frame
 * per blocco, il caso comune per audio-only da YouTube), seek nel file.
 */
class WebmOpusDemuxer(private val input: InputStream) {

    // ID degli elementi EBML/Matroska che ci interessano (in esadecimale, con i bit di marker).
    private object Ids {
        const val EBML = 0x1A45DFA3L
        const val SEGMENT = 0x18538067L
        const val SEEK_HEAD = 0x114D9B74L
        const val INFO = 0x1549A966L
        const val TIMECODE_SCALE = 0x2AD7B1L
        const val DURATION = 0x4489L
        const val TRACKS = 0x1654AE6BL
        const val TRACK_ENTRY = 0xAEL
        const val TRACK_NUMBER = 0xD7L
        const val TRACK_TYPE = 0x83L
        const val CODEC_ID = 0x86L
        const val AUDIO = 0xE1L
        const val SAMPLING_FREQUENCY = 0xB5L
        const val CHANNELS = 0x9FL
        const val CLUSTER = 0x1F43B675L
        const val SIMPLE_BLOCK = 0xA3L
        const val BLOCK_GROUP = 0xA0L
        const val BLOCK = 0xA1L
    }

    private var trackInfo: OpusTrackInfo? = null
    private var timecodeScaleNs: Long = 1_000_000L // default Matroska: 1 ms per tick
    private var durationTicks: Double? = null
    val durationMs: Long?
        get() = durationTicks?.let { ((it * timecodeScaleNs) / 1_000_000.0).toLong() }
    /**
     * Naviga il file finché non trova la traccia Opus dentro Tracks, e ne ritorna
     * i metadati. Consuma dallo stream tutto ciò che precede (EBML header, Segment,
     * SeekHead, Info, Tracks) — dopo questa chiamata lo stream è posizionato subito
     * dopo Tracks, pronto per leggere i Cluster con [readPackets].
     */
    fun readTrackInfo(): OpusTrackInfo {
        trackInfo?.let { return it }

        // Salta l'header EBML (non ci serve leggerne il contenuto).
        val ebmlHeader = Ebml.readElementHeader(input)
        require(ebmlHeader.id == Ids.EBML) { "atteso EBML header, trovato id=${ebmlHeader.id.toString(16)}" }
        Ebml.skipExact(input, ebmlHeader.size ?: error("EBML header con size sconosciuta, inatteso"))

        val segmentHeader = Ebml.readElementHeader(input)
        require(segmentHeader.id == Ids.SEGMENT) { "atteso Segment, trovato id=${segmentHeader.id.toString(16)}" }
        // Segment ha quasi sempre "unknown size" nei file da streaming: va bene,
        // semplicemente continuiamo a leggere i suoi figli finché non troviamo Tracks.

        while (true) {
            val header = Ebml.readElementHeader(input)
            when (header.id) {
                Ids.INFO -> {
                    val size = header.size ?: error("Info con size sconosciuta, non gestito")
                    parseInfo(size)
                }
                Ids.TRACKS -> {
                    val info = parseTracks(header.size)
                    trackInfo = info
                    return info
                }
                Ids.CLUSTER -> {
                    // Non dovremmo arrivare a un Cluster prima di aver trovato Tracks
                    // (di norma Tracks precede i Cluster), ma per sicurezza lo segnaliamo.
                    error("trovato Cluster prima di Tracks: file WebM con ordine inatteso")
                }
                else -> {
                    // SeekHead, Info, Tags, ecc: non ci servono, li saltiamo.
                    val size = header.size ?: error("elemento id=${header.id.toString(16)} con size sconosciuta, non gestito")
                    Ebml.skipExact(input, size)
                }
            }
        }
    }

    private fun parseInfo(infoSize: Long) {
        var remaining = infoSize
        while (remaining > 0) {
            val header = Ebml.readElementHeader(input)
            remaining -= header.headerLength
            val elementSize = header.size ?: error("elemento in Info con size sconosciuta, non gestito")

            when (header.id) {
                Ids.TIMECODE_SCALE -> timecodeScaleNs = readUInt(elementSize)
                Ids.DURATION -> durationTicks = readFloat(elementSize)
                else -> Ebml.skipExact(input, elementSize)
            }
            remaining -= elementSize
        }
    }

    /** Parsa Tracks per trovare la prima traccia audio con CodecID = A_OPUS. */
    private fun parseTracks(tracksSize: Long?): OpusTrackInfo {
        // Leggiamo TrackEntry per TrackEntry finché non troviamo quella Opus.
        // Nota: assume che Tracks abbia size nota (praticamente sempre vero).
        val size = tracksSize ?: error("Tracks con size sconosciuta, non gestito")
        var remaining = size

        while (remaining > 0) {
            val entryHeader = Ebml.readElementHeader(input)
            remaining -= entryHeader.headerLength

            if (entryHeader.id == Ids.TRACK_ENTRY) {
                val entrySize = entryHeader.size ?: error("TrackEntry con size sconosciuta, non gestito")
                val info = parseTrackEntry(entrySize)
                remaining -= entrySize
                if (info != null) {
                    // Trovata la traccia Opus: scartiamo il resto di Tracks e usciamo.
                    Ebml.skipExact(input, remaining)
                    return info
                }
            } else {
                val s = entryHeader.size ?: error("elemento in Tracks con size sconosciuta, non gestito")
                Ebml.skipExact(input, s)
                remaining -= s
            }
        }
        error("nessuna traccia Opus trovata dentro Tracks")
    }

    /** Parsa una singola TrackEntry; ritorna OpusTrackInfo se è la traccia Opus, altrimenti null. */
    private fun parseTrackEntry(entrySize: Long): OpusTrackInfo? {
        var remaining = entrySize
        var trackNumber: Long? = null
        var codecId: String? = null
        var sampleRate: Int = 48000 // default ragionevole, sovrascritto se presente
        var channels: Int = 2

        while (remaining > 0) {
            val header = Ebml.readElementHeader(input)
            remaining -= header.headerLength
            val elementSize = header.size ?: error("elemento in TrackEntry con size sconosciuta, non gestito")

            when (header.id) {
                Ids.TRACK_NUMBER -> {
                    trackNumber = readUInt(elementSize)
                }
                Ids.CODEC_ID -> {
                    codecId = String(Ebml.readExact(input, elementSize.toInt()), Charsets.US_ASCII)
                }
                Ids.AUDIO -> {
                    val (sr, ch) = parseAudioSettings(elementSize)
                    sampleRate = sr
                    channels = ch
                }
                else -> {
                    Ebml.skipExact(input, elementSize)
                }
            }
            remaining -= elementSize
        }

        return if (codecId == "A_OPUS" && trackNumber != null) {
            OpusTrackInfo(trackNumber, sampleRate, channels)
        } else {
            null
        }
    }

    /** Parsa il sotto-elemento Audio dentro TrackEntry per SamplingFrequency/Channels. */
    private fun parseAudioSettings(audioSize: Long): Pair<Int, Int> {
        var remaining = audioSize
        var sampleRate = 48000
        var channels = 2

        while (remaining > 0) {
            val header = Ebml.readElementHeader(input)
            remaining -= header.headerLength
            val elementSize = header.size ?: error("elemento in Audio con size sconosciuta, non gestito")

            when (header.id) {
                Ids.SAMPLING_FREQUENCY -> {
                    // Codificato come IEEE float (4 o 8 byte)
                    sampleRate = readFloat(elementSize).toInt()
                }
                Ids.CHANNELS -> {
                    channels = readUInt(elementSize).toInt()
                }
                else -> {
                    Ebml.skipExact(input, elementSize)
                }
            }
            remaining -= elementSize
        }
        return sampleRate to channels
    }

    private fun readUInt(size: Long): Long {
        val bytes = Ebml.readExact(input, size.toInt())
        var value = 0L
        for (b in bytes) value = (value shl 8) or (b.toLong() and 0xFF)
        return value
    }

    private fun readFloat(size: Long): Double {
        val bytes = Ebml.readExact(input, size.toInt())
        return when (bytes.size) {
            4 -> Float.intBitsToFloat(
                (bytes[0].toInt() and 0xFF shl 24) or (bytes[1].toInt() and 0xFF shl 16) or
                        (bytes[2].toInt() and 0xFF shl 8) or (bytes[3].toInt() and 0xFF)
            ).toDouble()
            8 -> java.lang.Double.longBitsToDouble(
                bytes.fold(0L) { acc, b -> (acc shl 8) or (b.toLong() and 0xFF) }
            )
            else -> error("size inattesa per un float EBML: ${bytes.size}")
        }
    }

    /**
     * Sequenza pigra dei pacchetti Opus grezzi, estratti man mano dai Cluster
     * che seguono. Va chiamata dopo [readTrackInfo]. Ogni elemento è un pacchetto
     * Opus pronto da passare a [OpusStreamDecoder.decode].
     */
    fun readPackets(track: OpusTrackInfo): Sequence<ByteArray> = sequence {
        while (true) {
            val header = try {
                Ebml.readElementHeader(input)
            } catch (e: EOFException) {
                break // fine dello stream, fine della riproduzione
            }

            when (header.id) {
                Ids.CLUSTER -> {
                    // Cluster è un master element: entriamo, non lo saltiamo.
                    // Se ha size nota, iteriamo finché non la esauriamo; se unknown,
                    // iteriamo finché non troviamo un altro Cluster o EOF (raro per file statici).
                    yieldAll(readClusterPackets(header.size, track.trackNumber))
                }
                else -> {
                    val size = header.size ?: break // elemento top-level unknown-size non gestito: ci fermiamo
                    Ebml.skipExact(input, size)
                }
            }
        }
    }

    private fun readClusterPackets(clusterSize: Long?, trackNumber: Long): Sequence<ByteArray> = sequence {
        var remaining = clusterSize ?: Long.MAX_VALUE

        while (remaining > 0) {
            val header = try {
                Ebml.readElementHeader(input)
            } catch (e: EOFException) {
                break
            }
            remaining -= header.headerLength

            when (header.id) {
                Ids.SIMPLE_BLOCK -> {
                    val size = header.size ?: error("SimpleBlock con size sconosciuta, non gestito")
                    val packet = readBlockPayload(size, trackNumber)
                    remaining -= size
                    if (packet != null) yield(packet)
                }
                Ids.BLOCK_GROUP -> {
                    val size = header.size ?: error("BlockGroup con size sconosciuta, non gestito")
                    val packet = readBlockGroupPayload(size, trackNumber)
                    remaining -= size
                    if (packet != null) yield(packet)
                }
                else -> {
                    val size = header.size ?: break
                    Ebml.skipExact(input, size)
                    remaining -= size
                }
            }
        }
    }

    private fun readBlockGroupPayload(size: Long, trackNumber: Long): ByteArray? {
        var remaining = size
        var packet: ByteArray? = null
        while (remaining > 0) {
            val header = Ebml.readElementHeader(input)
            remaining -= header.headerLength
            val elementSize = header.size ?: error("elemento in BlockGroup con size sconosciuta, non gestito")

            if (header.id == Ids.BLOCK) {
                packet = readBlockPayload(elementSize, trackNumber)
            } else {
                Ebml.skipExact(input, elementSize)
            }
            remaining -= elementSize
        }
        return packet
    }

    /**
     * Legge il payload di un SimpleBlock/Block: track number (vint), timecode (2 byte),
     * flags (1 byte), poi il frame audio grezzo. Assume nessun lacing (bit 1-2 dei flags a 0),
     * caso comune per audio Opus da YouTube.
     */
    private fun readBlockPayload(size: Long, expectedTrackNumber: Long): ByteArray? {
        val trackNumResult = Ebml.readVint(input, keepMarker = false)
        val header2 = Ebml.readExact(input, 3) // 2 byte timecode + 1 byte flags
        val flags = header2[2].toInt()
        val lacing = (flags shr 1) and 0x03
        if (lacing != 0) {
            // TODO: supporto al lacing non implementato. Se capita, va gestito qui
            // (raro per audio Opus semplice, ma possibile con alcuni muxer).
            println("[demux] attenzione: lacing rilevato (tipo=$lacing), non supportato, blocco scartato")
        }

        val consumedSoFar = trackNumResult.lengthInBytes + 3
        val frameSize = (size - consumedSoFar).toInt()
        val frame = Ebml.readExact(input, frameSize)

        return if (trackNumResult.value == expectedTrackNumber && lacing == 0) frame else null
    }

    /**
     * Da usare dopo un seek "grezzo" (es. Range HTTP su un offset approssimato,
     * non necessariamente allineato all'inizio di un Cluster): risincronizza
     * lo stream cercando la prossima occorrenza dell'ID Cluster (0x1F43B675),
     * poi continua a leggere pacchetti normalmente da lì.
     */
    fun resyncAndReadPackets(track: OpusTrackInfo): Sequence<ByteArray> = sequence {
        resyncToNextCluster()
        val sizeResult = Ebml.readVint(input, keepMarker = false)
        val allOnes = (1L shl (7 * sizeResult.lengthInBytes)) - 1
        val clusterSize = if (sizeResult.value == allOnes) null else sizeResult.value
        yieldAll(readClusterPackets(clusterSize, track.trackNumber))
        yieldAll(readPackets(track)) // continua con i Cluster successivi normalmente
    }

    /** Scandisce byte per byte finché non trova l'ID di un Cluster (0x1F 0x43 0xB6 0x75). */
    private fun resyncToNextCluster() {
        val marker = intArrayOf(0x1F, 0x43, 0xB6, 0x75)
        var matched = 0
        while (matched < marker.size) {
            val b = input.read()
            if (b == -1) throw EOFException("fine stream durante resync su Cluster")
            matched = if (b == marker[matched]) matched + 1 else if (b == marker[0]) 1 else 0
        }
    }
}