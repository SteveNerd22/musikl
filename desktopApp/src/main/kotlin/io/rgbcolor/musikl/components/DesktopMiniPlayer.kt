package io.rgbcolor.musikl.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.player.PlayerUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DesktopMiniPlayer(
    track: TrackResult,
    playerState: PlayerUiState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val maxUpwardDragPx = with(density) { 12.dp.toPx() }
    val dismissThresholdPx = with(density) { 60.dp.toPx() }
    val maxDownwardDragPx = with(density) { 140.dp.toPx() }

    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .alpha(1f - (offsetY.value / maxDownwardDragPx).coerceIn(0f, 1f) * 0.5f)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newValue = (offsetY.value + dragAmount)
                                .coerceIn(-maxUpwardDragPx, maxDownwardDragPx)
                            offsetY.snapTo(newValue)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (offsetY.value > dismissThresholdPx) {
                                offsetY.animateTo(maxDownwardDragPx, tween(150))
                                onDismiss()
                            } else {
                                offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    },
                )
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = track.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousClick) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Precedente")
                    }
                    IconButton(onClick = onPlayPauseClick) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pausa" else "Riproduci",
                        )
                    }
                    IconButton(onClick = onNextClick) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Successivo")
                    }

                    Spacer(Modifier.width(12.dp))

                    DesktopSeekBar(
                        positionMs = playerState.positionMs,
                        durationMs = playerState.durationMs,
                        onSeek = onSeek,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Chiudi player")
            }
        }
    }
}

@Composable
private fun DesktopSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    val safeDuration = durationMs.coerceAtLeast(1L).toFloat()
    val displayedValue = if (isDragging) dragValue else positionMs.toFloat().coerceIn(0f, safeDuration)

    Slider(
        value = displayedValue,
        onValueChange = { isDragging = true; dragValue = it },
        onValueChangeFinished = { onSeek(dragValue.toLong()); isDragging = false },
        valueRange = 0f..safeDuration,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier,
    )
}