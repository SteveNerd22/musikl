package io.rgbcolor.musikl.player.opuswebm

/**
 * Astrazione minimale di un "output audio" verso cui scrivere PCM 16 bit
 * interleaved. Isola la logica del provider (demux/decode/seek) dai dettagli
 * di piattaforma: su desktop è implementato con javax.sound.sampled.SourceDataLine,
 * su Android con android.media.AudioTrack.
 */
interface PcmAudioSink {
    fun start()
    fun write(bytes: ByteArray, offset: Int, length: Int)
    fun stop()
    fun flush()
    fun drain()
    fun close()
}