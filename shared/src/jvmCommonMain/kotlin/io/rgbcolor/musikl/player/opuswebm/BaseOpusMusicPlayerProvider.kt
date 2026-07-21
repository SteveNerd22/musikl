package io.rgbcolor.musikl.player.opuswebm

import io.rgbcolor.musikl.AudioFormat
import io.rgbcolor.musikl.Capabilities
import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Logica comune ai provider Opus/WebM su JVM e Android: apertura HTTP con
 * supporto a Range, demux WebM, decodifica Opus, gestione dello stato di
 * riproduzione e seek (approssimato + resync sul prossimo Cluster).
 *
 * Le sottoclassi devono solo fornire un [PcmAudioSink] per un dato sample
 * rate/canali: tutto il resto (incluso il seek) è già gestito qui.
 */
abstract class BaseOpusMusicPlayerProvider : MusicPlayerProvider {

    private val _state = MutableStateFlow(PlayerUiState())
    override val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    override val capabilities = Capabilities(
        supportedFormats = setOf(AudioFormat.OPUS)
    )

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var playbackJob: Job? = null
    private var sink: PcmAudioSink? = null

    private var currentUrl: String? = null
    private var contentLength: Long = -1
    private var trackInfo: OpusTrackInfo? = null
    private var demuxer: WebmOpusDemuxer? = null
    private var countingInput: CountingInputStream? = null

    @Volatile private var paused = false

    /** Crea il sink audio di piattaforma per il sample rate/canali indicati. */
    protected abstract fun createAudioSink(sampleRate: Int, channels: Int): PcmAudioSink

    override fun play(url: String) {
        currentUrl = url
        playbackJob?.cancel()
        sink?.close()

        playbackJob = scope.launch {
            try {
                startFromScratch(url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = e.message, isPlaying = false, isBuffering = false)
            }
        }
    }

    override suspend fun warmUp() {
        super.warmUp()
        withContext(Dispatchers.IO) {
            try {
                val warmupSink = createAudioSink(48_000, 2)
                warmupSink.start()
                warmupSink.stop()
                warmupSink.close()
            } catch (e: Exception) {
            }

            try {
                val warmupDecoder = OpusStreamDecoder(48_000, 2)
                repeat(3) { warmupDecoder.warmUp() }
            } catch (e: Exception) {
            }
        }
    }

    private fun openHttpStream(url: String, rangeStart: Long): InputStream {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            if (rangeStart > 0) setRequestProperty("Range", "bytes=$rangeStart-")
        }
        if (rangeStart == 0L) contentLength = connection.contentLengthLong
        return BufferedInputStream(connection.inputStream, 64 * 1024)
    }

    private suspend fun startFromScratch(url: String) {
        _state.value = _state.value.copy(isBuffering = true, error = null, positionMs = 0)

        val counting = CountingInputStream(streamFactory = { offset -> openHttpStream(url, offset) })
        countingInput = counting

        val demux = WebmOpusDemuxer(counting)
        demuxer = demux
        val info = demux.readTrackInfo()
        trackInfo = info

        demux.durationMs?.let { d ->
            _state.value = _state.value.copy(durationMs = d)
        }

        val decoder = OpusStreamDecoder(info.sampleRate, info.channels)
        val audioSink = createAudioSink(decoder.outputSampleRate, decoder.outputChannels)
        audioSink.start()
        sink = audioSink

        _state.value = _state.value.copy(isBuffering = false, isPlaying = true)

        playPackets(demux.readPackets(info), decoder, audioSink, baselinePositionMs = 0)
    }

    private suspend fun playPackets(
        packets: Sequence<ByteArray>,
        decoder: OpusStreamDecoder,
        audioSink: PcmAudioSink,
        baselinePositionMs: Long,
    ) {
        var samplesWritten = 0L

        for (packet in packets) {
            currentCoroutineContext().ensureActive()

            while (paused) {
                delay(50)
            }

            val pcm = decoder.decode(packet)
            val bytes = shortsToLittleEndianBytes(pcm)
            audioSink.write(bytes, 0, bytes.size)

            samplesWritten += pcm.size / decoder.outputChannels
            val elapsedMs = (samplesWritten * 1000) / decoder.outputSampleRate
            _state.value = _state.value.copy(positionMs = baselinePositionMs + elapsedMs)
        }

        audioSink.drain()
        _state.value = _state.value.copy(isPlaying = false)
    }

    private fun shortsToLittleEndianBytes(shorts: ShortArray): ByteArray {
        val bytes = ByteArray(shorts.size * 2)
        for (i in shorts.indices) {
            val s = shorts[i].toInt()
            bytes[i * 2] = (s and 0xFF).toByte()
            bytes[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
        }
        return bytes
    }

    override fun pause() {
        paused = true
        sink?.stop()
        _state.value = _state.value.copy(isPlaying = false)
    }

    override fun resume() {
        paused = false
        sink?.start()
        _state.value = _state.value.copy(isPlaying = true)
    }

    override fun stop() {
        playbackJob?.cancel()
        sink?.stop()
        sink?.close()
        _state.value = _state.value.copy(isPlaying = false, positionMs = 0)
    }

    override fun seekTo(positionMs: Long) {
        val info = trackInfo ?: return
        val duration = _state.value.durationMs
        val counting = countingInput ?: return
        val demux = demuxer ?: return

        if (duration <= 0 || contentLength <= 0) {
            println("[opus] seekTo non disponibile: durata o content-length sconosciuti")
            return
        }

        val targetMs = positionMs.coerceIn(0, duration)
        // Stima grezza dell'offset in byte, assumendo bitrate ~costante.
        val approxOffset = ((targetMs.toDouble() / duration) * contentLength).toLong()
            .coerceIn(0, contentLength - 1)

        playbackJob?.cancel()
        sink?.stop()
        sink?.flush()

        playbackJob = scope.launch {
            try {
                counting.seek(approxOffset)

                // Decoder nuovo: evita di portarsi dietro stato di decodifica
                // incoerente dopo un salto arbitrario nello stream.
                val decoder = OpusStreamDecoder(info.sampleRate, info.channels)
                val audioSink = sink ?: createAudioSink(decoder.outputSampleRate, decoder.outputChannels).also { sink = it }
                audioSink.start()

                _state.value = _state.value.copy(isBuffering = false, isPlaying = true, positionMs = targetMs)

                playPackets(demux.resyncAndReadPackets(info), decoder, audioSink, baselinePositionMs = targetMs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = e.message, isPlaying = false, isBuffering = false)
            }
        }
    }

    override fun release() {
        playbackJob?.cancel()
        sink?.close()
        scope.cancel()
    }
}