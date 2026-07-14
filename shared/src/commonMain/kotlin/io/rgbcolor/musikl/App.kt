package io.rgbcolor.musikl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.launch

private val musicQueries = listOf(
    "Kasane Teto Bubble",
    "Hatsune Miku Romeo and Cinderella",
    "Kasane Teto & Hatsune Miku daidaidaidaidaikirai",
    "Kasane Teto Snowman Cover",
    "Hatsune Miku nyanyanyanyanyanya",
)

private enum class SearchAction(val label: String) {
    FIRST_SONG("Prima canzone"),
    FIRST_VIDEO("Primo video"),
    SONGS("Canzoni"),
    VIDEOS("Video"),
}

private suspend fun MusicSearchProvider.run(action: SearchAction, query: String): List<TrackResult> =
    when (action) {
        SearchAction.FIRST_SONG -> listOf(searchFirstSong(query))
        SearchAction.FIRST_VIDEO -> listOf(searchFirstVideo(query))
        SearchAction.SONGS -> searchSongs(query)
        SearchAction.VIDEOS -> searchVideos(query)
    }

@Composable
@Preview
fun App() {
    MaterialTheme {
        val provider = remember { createDefaultMusicSearchProvider() }
        var results by remember { mutableStateOf<List<TrackResult>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        fun triggerSearch(action: SearchAction) {
            val query = musicQueries.random()
            loading = true
            error = null
            scope.launch {
                try {
                    results = provider.run(action, query)
                } catch (t: Throwable) {
                    t.printStackTrace()
                    error = "${t::class.simpleName}: ${t.message ?: "errore sconosciuto"}"
                } finally {
                    loading = false
                }
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchAction.entries.forEach { action ->
                    Button(
                        enabled = !loading,
                        onClick = { triggerSearch(action) },
                    ) {
                        Text(action.label)
                    }
                }
            }

            if (loading) {
                CircularProgressIndicator()
            }

            error?.let { Text("Errore: $it") }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(results) { track ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AsyncImage(model = track.thumbnailUrl, contentDescription = null)
                        Text(track.title)
                    }
                }
            }
        }
    }
}