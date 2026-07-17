package io.rgbcolor.musikl.player.vlcj

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

class VlcjMusicPlayerProvider : MusicPlayerProvider {

    private val _state = MutableStateFlow(PlayerUiState())
    override val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    private val audioComponent = AudioPlayerComponent()
    private val player: MediaPlayer get() = audioComponent.mediaPlayer()

    init {
        player.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mediaPlayer: MediaPlayer) {
                _state.value = _state.value.copy(isPlaying = true, isBuffering = false)
            }

            override fun paused(mediaPlayer: MediaPlayer) {
                _state.value = _state.value.copy(isPlaying = false)
            }

            override fun stopped(mediaPlayer: MediaPlayer) {
                _state.value = _state.value.copy(isPlaying = false)
            }

            override fun buffering(mediaPlayer: MediaPlayer, newCache: Float) {
                _state.value = _state.value.copy(isBuffering = newCache < 100f)
            }

            override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
                _state.value = _state.value.copy(durationMs = newLength)
            }

            override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
                _state.value = _state.value.copy(positionMs = newTime)
            }

            override fun error(mediaPlayer: MediaPlayer) {
                _state.value = _state.value.copy(error = "Errore di riproduzione VLC")
            }
        })
    }

    override fun play(url: String) {
        player.media().play(url)
    }

    override fun pause() {
        player.controls().pause()
    }

    override fun resume() {
        player.controls().play()
    }

    override fun stop() {
        player.controls().stop()
    }

    override fun seekTo(positionMs: Long) {
        player.controls().setTime(positionMs)
    }

    override fun release() {
        audioComponent.release()
    }
}