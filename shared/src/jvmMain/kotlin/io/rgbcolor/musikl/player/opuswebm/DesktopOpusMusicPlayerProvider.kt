package io.rgbcolor.musikl.player.opuswebm

import javax.sound.sampled.AudioFormat as JavaAudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

/** Implementazione desktop del provider Opus/WebM, con output via javax.sound.sampled. */
class DesktopOpusMusicPlayerProvider : BaseOpusMusicPlayerProvider() {

    override fun createAudioSink(sampleRate: Int, channels: Int): PcmAudioSink {
        val javaFormat = JavaAudioFormat(
            sampleRate.toFloat(),
            16, // bit per campione
            channels,
            true, // signed
            false, // little-endian
        )
        val sourceLine = AudioSystem.getSourceDataLine(javaFormat)
        sourceLine.open(javaFormat)
        return SourceDataLineSink(sourceLine)
    }
}

private class SourceDataLineSink(private val line: SourceDataLine) : PcmAudioSink {
    override fun start() = line.start()
    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        line.write(bytes, offset, length)
    }
    override fun stop() = line.stop()
    override fun flush() = line.flush()
    override fun drain() = line.drain()
    override fun close() = line.close()
}