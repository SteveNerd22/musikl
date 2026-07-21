package io.rgbcolor.musikl.player.opuswebm

import io.github.jaredmdobson.concentus.OpusDecoder

class OpusStreamDecoder(sampleRate: Int, private val channels: Int) {

    private val decodeSampleRate = normalizeSampleRate(sampleRate)
    private val decoder = OpusDecoder(decodeSampleRate, channels)

    private val maxFrameSamples = decodeSampleRate / 1000 * 120
    private val pcmBuffer = ShortArray(maxFrameSamples * channels)

    val outputSampleRate: Int get() = decodeSampleRate
    val outputChannels: Int get() = channels

    fun decode(packet: ByteArray): ShortArray {
        val samplesDecoded = decoder.decode(packet, 0, packet.size, pcmBuffer, 0, maxFrameSamples, false)
        return pcmBuffer.copyOf(samplesDecoded * channels)
    }

    fun warmUp() {
        val frameSamples = decodeSampleRate / 1000 * 20
        decoder.decode(null, 0, 0, pcmBuffer, 0, frameSamples, false)
    }

    private fun normalizeSampleRate(sr: Int): Int = when {
        sr >= 48000 -> 48000
        sr >= 24000 -> 24000
        sr >= 16000 -> 16000
        sr >= 12000 -> 12000
        else -> 8000
    }
}