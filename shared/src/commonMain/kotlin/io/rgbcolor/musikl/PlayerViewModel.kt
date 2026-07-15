package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(private val provider: MusicPlayerProvider) {
    val uiState: StateFlow<PlayerUiState> = provider.state

    fun play(url: String) = provider.play(url)
    fun pause() = provider.pause()
    fun resume() = provider.resume()
    fun stop() = provider.stop()
    fun seekTo(positionMs: Long) = provider.seekTo(positionMs)
    fun release() = provider.release()
}