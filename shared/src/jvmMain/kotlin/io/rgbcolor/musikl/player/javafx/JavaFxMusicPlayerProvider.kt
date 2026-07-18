package io.rgbcolor.musikl.player.javafx

import io.rgbcolor.musikl.AudioFormat
import io.rgbcolor.musikl.Capabilities
import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private object JavaFxToolkit {
    val started: Unit by lazy {
        Platform.startup {}
    }
}

class JavaFxMusicPlayerProvider : MusicPlayerProvider {

    private val _state = MutableStateFlow(PlayerUiState())
    override val state: StateFlow<PlayerUiState> = _state.asStateFlow()
    override val capabilities = Capabilities(
        supportedFormats = setOf(AudioFormat.M4A)
    )

    private var mediaPlayer: MediaPlayer? = null

    init {
        JavaFxToolkit.started
    }

    override fun play(url: String) {
        println("play() chiamato con url=$url")
        Platform.runLater {
            println("dentro runLater, sto per creare Media")
            mediaPlayer?.dispose()

            val forcedUrl = if (!url.contains(".m4a")) "$url&dummy=.m4a" else url

            val media = Media(forcedUrl)
            println("Media creato: $media")
            val player = MediaPlayer(media)
            mediaPlayer = player
            println("MediaPlayer creato")

            player.setOnReady {
                println("player pronto, durata=${player.totalDuration}")
                _state.value = _state.value.copy(
                    durationMs = player.totalDuration.toMillis().toLong(),
                )
            }
            player.currentTimeProperty().addListener { _, _, newValue ->
                _state.value = _state.value.copy(positionMs = newValue.toMillis().toLong())
            }
            player.setOnPlaying {
                println("ERRORE PLAYER: ${player.error}")
                _state.value = _state.value.copy(isPlaying = true, isBuffering = false)
            }
            player.setOnPaused {
                _state.value = _state.value.copy(isPlaying = false)
            }
            player.setOnStalled {
                _state.value = _state.value.copy(isBuffering = true)
            }
            player.setOnError {
                println("ERRORE PLAYER: ${player.error}")
                _state.value = _state.value.copy(error = player.error?.message)
            }

            println("chiamo player.play()")
            player.play()
        }
    }

    override fun pause() {
        Platform.runLater { mediaPlayer?.pause() }
    }

    override fun resume() {
        println("resume() chiamato")
        Platform.runLater { mediaPlayer?.play() }
    }

    override fun stop() {
        Platform.runLater { mediaPlayer?.stop() }
    }

    override fun seekTo(positionMs: Long) {
        Platform.runLater {
            mediaPlayer?.seek(javafx.util.Duration.millis(positionMs.toDouble()))
        }
    }

    override fun release() {
        Platform.runLater { mediaPlayer?.dispose() }
    }
}