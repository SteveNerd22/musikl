package io.rgbcolor.musikl

interface MusicProvider {
    public val name: String
    val capabilities: Capabilities

    suspend fun warmUp() {}
    fun onHandshake(otherCapabilities: Capabilities) {}
}

enum class AudioFormat {
    MP3, AAC, OPUS, WAV, M4A
}

data class Capabilities(
    val supportedFormats: Set<AudioFormat>
)