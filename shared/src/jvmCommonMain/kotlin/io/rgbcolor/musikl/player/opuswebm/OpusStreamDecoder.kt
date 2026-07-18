package io.rgbcolor.musikl.player.opuswebm

import io.github.jaredmdobson.concentus.OpusDecoder

/**
 * Decodifica pacchetti Opus grezzi in PCM a 16 bit interleaved, usando Concentus
 * (implementazione Java pura del decoder Opus di riferimento, nessuna dipendenza nativa).
 */
class OpusStreamDecoder(sampleRate: Int, private val channels: Int) {

    private val decodeSampleRate = normalizeSampleRate(sampleRate)
    private val decoder = OpusDecoder(decodeSampleRate, channels)

    private val maxFrameSamples = decodeSampleRate / 1000 * 120
    private val pcmBuffer = ShortArray(maxFrameSamples * channels)

    val outputSampleRate: Int get() = decodeSampleRate
    val outputChannels: Int get() = channels

    /**
     * Decodifica un pacchetto Opus. Ritorna un array PCM interleaved
     * (lunghezza = campioni_decodificati * channels), dimensionato esattamente
     * sull'audio prodotto (non sul buffer interno).
     */
    fun decode(packet: ByteArray): ShortArray {
        val samplesDecoded = decoder.decode(packet, 0, packet.size, pcmBuffer, 0, maxFrameSamples, false)
        return pcmBuffer.copyOf(samplesDecoded * channels)
    }

    private fun normalizeSampleRate(sr: Int): Int = when {
        sr >= 48000 -> 48000
        sr >= 24000 -> 24000
        sr >= 16000 -> 16000
        sr >= 12000 -> 12000
        else -> 8000
    }
}