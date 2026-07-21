package io.rgbcolor.musikl.player.vlcj

import io.rgbcolor.musikl.AudioFormat
import io.rgbcolor.musikl.Capabilities
import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

class VlcNotAvailableException(message: String) : Exception(message)

class VlcjMusicPlayerProvider : MusicPlayerProvider {
    override val name = "VLCj"

    private val _state = MutableStateFlow(PlayerUiState())
    override val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val audioComponent: AudioPlayerComponent
    private val player: MediaPlayer get() = audioComponent.mediaPlayer()

    init {
        val found = NativeDiscovery().discover()
        if (!found) {
            throw VlcNotAvailableException(
                "VLC non è installato o non è stato trovato sul sistema."
            )
        }

        audioComponent = try {
            AudioPlayerComponent()
        } catch (e: Throwable) {
            throw VlcNotAvailableException("Inizializzazione di VLC fallita: ${e.message}")
        }

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

    override suspend fun warmUp() {
        super.warmUp()
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

    override val capabilities = Capabilities(
        supportedFormats = setOf(AudioFormat.AAC, AudioFormat.M4A, AudioFormat.MP3, AudioFormat.WAV, AudioFormat.OPUS)
    )
}