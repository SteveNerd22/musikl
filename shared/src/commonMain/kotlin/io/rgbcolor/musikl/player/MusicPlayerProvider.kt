package io.rgbcolor.musikl.player

import io.rgbcolor.musikl.MusicProvider
import kotlinx.coroutines.flow.StateFlow

interface MusicPlayerProvider : MusicProvider{
    val state: StateFlow<PlayerUiState>

    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun release()
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val isBuffering: Boolean = false,
    val error: String? = null,
)