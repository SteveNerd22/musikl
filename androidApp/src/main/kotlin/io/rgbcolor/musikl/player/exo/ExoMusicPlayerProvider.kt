package io.rgbcolor.musikl.player.exo

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.rgbcolor.musikl.AudioFormat
import io.rgbcolor.musikl.Capabilities
import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ExoMusicPlayerProvider(context: Context) : MusicPlayerProvider {
    override val name = "Media3/Exo"

    private val _state = MutableStateFlow(PlayerUiState())
    override val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    private val exoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.value = _state.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    durationMs = duration.coerceAtLeast(0),
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.value = _state.value.copy(error = error.message)
            }
        })
    }

    init {
        scope.launch {
            while (isActive) {
                _state.value = _state.value.copy(positionMs = exoPlayer.currentPosition)
                delay(500)
            }
        }
    }

    override fun play(url: String) {
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun resume() {
        exoPlayer.play()
    }

    override fun stop() {
        exoPlayer.stop()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun release() {
        exoPlayer.release()
    }

    override val capabilities = Capabilities (
        supportedFormats= setOf(AudioFormat.AAC, AudioFormat.M4A, AudioFormat.MP3, AudioFormat.WAV, AudioFormat.OPUS)
    )
}