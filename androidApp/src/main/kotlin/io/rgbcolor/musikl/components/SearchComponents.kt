package io.rgbcolor.musikl.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import io.rgbcolor.musikl.model.TrackResult
import coil3.compose.AsyncImage
import io.rgbcolor.musikl.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMainScreen(viewModel: SearchViewModel, onTrackClick: (TrackResult) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val isMusicTab = uiState.isMusicTab
    val listState = if (isMusicTab) viewModel.musicListState else viewModel.videoListState

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Cerca...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    viewModel.performSearch(uiState.query)
                    focusManager.clearFocus()
                }
            ) {
                Text("Cerca")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = if (isMusicTab) 0 else 1) {
            Tab(selected = isMusicTab, onClick = { viewModel.updateTab(true) }, text = { Text("Musica") })
            Tab(selected = !isMusicTab, onClick = { viewModel.updateTab(false) }, text = { Text("Video") })
        }

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentResults = if (isMusicTab) uiState.songResults else uiState.videoResults

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items=currentResults,
                        key = { track -> track.pageUrl }
                    ) { track ->
                        val index = currentResults.indexOf(track)
                        if (index == currentResults.lastIndex && !uiState.isLoading && currentResults.isNotEmpty()) {
                            androidx.compose.runtime.LaunchedEffect(index) {
                                viewModel.performSearch(uiState.query, isLoadMore = true)
                            }
                        }

                        TrackItem(track, onClick = { onTrackClick(track) })
                    }
                    if (uiState.isLoading && currentResults.isNotEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackItem(track: TrackResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail con Coil
            AsyncImage(
                model = track.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp, 60.dp) // Proporzione classica 4:3
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
        }
    }
}