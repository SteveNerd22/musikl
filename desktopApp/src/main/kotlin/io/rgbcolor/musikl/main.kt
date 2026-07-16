package io.rgbcolor.musikl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.rgbcolor.musikl.components.DesktopMiniPlayer
import io.rgbcolor.musikl.components.SearchTabContent
import io.rgbcolor.musikl.components.Sidebar
import io.rgbcolor.musikl.components.TabStrip
import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.tab.TabContent
import io.rgbcolor.musikl.tab.TabManager
import kotlinx.coroutines.launch

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "musikl", icon = painterResource("icon.png")) {
        DesktopAppShell()
    }
}

@Composable
fun DesktopAppShell() {
    val tabManager = remember { TabManager() }
    val playerViewModel = remember { createPlayerViewModel() }
    val scope = rememberCoroutineScope()

    val currentTrack by playerViewModel.currentTrack.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()

    fun onTrackClick(track: TrackResult, searchViewModel: SearchViewModel) {
        scope.launch {
            val streamUrl = searchViewModel.resolveStreamUrl(track)
            if (playerState.isPlaying) playerViewModel.stop()
            playerViewModel.play(track, streamUrl)
        }
    }

    Row(Modifier.fillMaxSize()) {
        Sidebar(
            onNavigate = { content -> tabManager.openContentInCurrentTab(content) },
            onNavigateNewTab = { content -> tabManager.openContentInNewTab(content) },
        )

        Column(Modifier.weight(1f)) {
            AnimatedVisibility(visible = tabManager.tabs.size > 1) {
                TabStrip(
                    tabs = tabManager.tabs,
                    activeTabId = tabManager.activeTabId,
                    onTabClick = { tab -> tabManager.activateTab(tab) },
                    onTabClose = { tab -> tabManager.closeTab(tab) },
                )
            }

            Box(Modifier.weight(1f)) {
                val activeTab = tabManager.tabs.find { it.id == tabManager.activeTabId }
                when (val content = activeTab?.content) {
                    is TabContent.Home -> { /* TODO: schermata Home */ }
                    is TabContent.Search -> SearchTabContent (
                        viewModel = activeTab.searchViewModel,
                        onTrackClick = { track -> onTrackClick(track, activeTab.searchViewModel) },
                    )
                    is TabContent.Song -> { /* TODO: dettaglio brano */ }
                    is TabContent.Playlist -> {/* TODO: schermata playlist */}
                    is TabContent.Settings -> {/* TODO: schermata settings */}
                    null -> { /* non dovrebbe mai succedere: tabs non è mai vuota */ }
                }
            }

            AnimatedVisibility(visible = currentTrack != null) {
                currentTrack?.let { track ->
                    DesktopMiniPlayer(
                        track = track,
                        playerState = playerState,
                        onPlayPauseClick = { playerViewModel.togglePlayPause() },
                        onPreviousClick = {},
                        onNextClick = {},
                        onSeek = { playerViewModel.seekTo(it) },
                        onDismiss = { playerViewModel.dismiss() },
                    )
                }
            }
        }
    }
}