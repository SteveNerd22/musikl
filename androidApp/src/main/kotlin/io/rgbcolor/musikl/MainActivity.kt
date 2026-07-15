package io.rgbcolor.musikl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.rgbcolor.musikl.components.MyBottomNavigation
import io.rgbcolor.musikl.components.SearchMainScreen
import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.player.exo.ExoMusicPlayerProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ProviderRegistry.registerPlayerProvider(ExoMusicPlayerProvider(applicationContext))

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

    fun onTrackClick(track: TrackResult) {
        scope.launch {
            val streamUrl = searchViewModel.resolveStreamUrl(track)
            playerViewModel.play(streamUrl)
        }
    }

    Scaffold(
        bottomBar = {
            MyBottomNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.RICERCA -> {
                    SearchMainScreen(searchViewModel, onTrackClick = ::onTrackClick)
                }
                Screen.OPZIONI -> { /* Qui metterai la UI delle opzioni */ }
                Screen.PLAYLIST -> { /* Qui metterai la UI delle playlist */ }
            }
        }
    }
}