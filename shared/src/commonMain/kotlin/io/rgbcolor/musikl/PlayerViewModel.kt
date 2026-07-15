package io.rgbcolor.musikl

import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel(private val provider: MusicPlayerProvider) {
    val playerState: StateFlow<PlayerUiState> = provider.state

    private val _currentTrack = MutableStateFlow<TrackResult?>(null)
    val currentTrack: StateFlow<TrackResult?> = _currentTrack.asStateFlow()

    fun play(track: TrackResult, streamUrl: String) {
        _currentTrack.value = track
        provider.play(streamUrl)
    }

    fun togglePlayPause() {
        if (playerState.value.isPlaying) provider.pause() else provider.resume()
    }

    fun dismiss() {
        provider.stop()
        _currentTrack.value = null
    }

    fun seekTo(positionMs: Long) = provider.seekTo(positionMs)
    fun stop() = provider.stop()
    fun release() = provider.release()
}