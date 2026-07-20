package io.rgbcolor.musikl

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.rgbcolor.musikl.components.MiniPlayerBar
import io.rgbcolor.musikl.components.MyBottomNavigation
import io.rgbcolor.musikl.components.SearchMainScreen
import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.player.exo.ExoMusicPlayerProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val playerProvider = ExoMusicPlayerProvider(applicationContext)
        ProviderRegistry.registerPlayerProvider(playerProvider)
        ProviderRegistry.setPlayerProvider(playerProvider.name)

        setContent {
            MobileAppShell()
        }
    }
}

enum class Screen { OPZIONI, RICERCA, PLAYLIST }

@Composable
fun MobileAppShell() {
    val searchViewModel = remember { createSearchViewModel() }
    val playerViewModel = remember { createPlayerViewModel() }
    var currentScreen by remember { mutableStateOf(Screen.RICERCA) }
    val scope = rememberCoroutineScope()

    val currentTrack by playerViewModel.currentTrack.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()

    fun onTrackClick(track: TrackResult) {
        scope.launch {
            val streamUrl = searchViewModel.resolveStreamUrl(track)
            if (streamUrl != null) {
                if (playerState.isPlaying) {
                    playerViewModel.stop()
                }
                playerViewModel.play(track, streamUrl)
            }
        }
    }

    Scaffold(
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = currentTrack != null,
                    enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }) +
                            expandVertically(expandFrom = Alignment.Top),
                    exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) +
                            shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    currentTrack?.let { track ->
                        MiniPlayerBar(
                            track = track,
                            playerState = playerState,
                            onPlayPauseClick = { playerViewModel.togglePlayPause() },
                            onPreviousClick = { /* nessuna queue per ora */ },
                            onNextClick = { /* nessuna queue per ora */ },
                            onSeek = { playerViewModel.seekTo(it) },
                            onDismiss = { playerViewModel.dismiss() },
                        )
                    }
                }
                MyBottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                )
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.RICERCA -> SearchMainScreen(searchViewModel, onTrackClick = ::onTrackClick)
                Screen.OPZIONI -> { /* Qui metterai la UI delle opzioni */ }
                Screen.PLAYLIST -> { /* Qui metterai la UI delle playlist */ }
            }
        }
    }
}