package io.rgbcolor.musikl.player.exo

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.SilenceMediaSource
import io.rgbcolor.musikl.AudioFormat
import io.rgbcolor.musikl.Capabilities
import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class ExoMusicPlayerProvider(context: Context) : MusicPlayerProvider {
    override val name = "Media3/Exo"

    private val _state = MutableStateFlow(PlayerUiState())
    override val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var warmUpJob: Job? = null

    @Volatile private var isWarmingUp = false

    private val exoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isWarmingUp) return
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (isWarmingUp) return
                _state.value = _state.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    durationMs = duration.coerceAtLeast(0),
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                if (isWarmingUp) return
                _state.value = _state.value.copy(error = error.message)
            }
        })
    }

    init {
        scope.launch {
            while (isActive) {
                if (!isWarmingUp) {
                    _state.value = _state.value.copy(positionMs = exoPlayer.currentPosition)
                }
                delay(500)
            }
        }
    }

    override suspend fun warmUp() {
        super.warmUp()
        val job = scope.launch { performWarmUp() }
        warmUpJob = job
        job.join()
    }

    private suspend fun performWarmUp() = withContext(Dispatchers.Main.immediate) {
        isWarmingUp = true
        try {
            val silence = SilenceMediaSource.Factory()
                .setDurationUs(300_000L) // 300ms: bastano a far allocare AudioTrack e decoder
                .createMediaSource()

            exoPlayer.volume = 0f
            exoPlayer.setMediaSource(silence)
            exoPlayer.prepare()
            exoPlayer.play()

            suspendCancellableCoroutine<Unit> { cont ->
                val listener = object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            exoPlayer.removeListener(this)
                            if (cont.isActive) cont.resume(Unit) {}
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        exoPlayer.removeListener(this)
                        if (cont.isActive) cont.resume(Unit) {}
                    }
                }
                exoPlayer.addListener(listener)
                cont.invokeOnCancellation { exoPlayer.removeListener(listener) }
            }
        } catch (e: Exception) {
            // Il warmup è un'ottimizzazione best-effort: se fallisce,
            // la prima riproduzione reale sarà semplicemente un po' più lenta,
            // ma l'app non deve crashare per questo.
        } finally {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.volume = 1f
            isWarmingUp = false
        }
    }

    override fun play(url: String) {
        // Se arriva una richiesta di riproduzione reale mentre il warmup
        // è ancora in corso, lo interrompiamo subito: la priorità è
        // sempre la riproduzione richiesta dall'utente, non il warmup.
        warmUpJob?.cancel()
        if (isWarmingUp) {
            isWarmingUp = false
            exoPlayer.volume = 1f
        }

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

    override val capabilities = Capabilities(
        supportedFormats = setOf(AudioFormat.AAC, AudioFormat.M4A, AudioFormat.MP3, AudioFormat.WAV, AudioFormat.OPUS)
    )
}