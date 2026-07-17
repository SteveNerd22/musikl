package io.rgbcolor.musikl.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.rgbcolor.musikl.SearchViewModel
import io.rgbcolor.musikl.model.TrackResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabContent(viewModel: SearchViewModel, onTrackClick: (TrackResult) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val listState = if (uiState.isMusicTab) viewModel.musicGridState else viewModel.videoGridState

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.updateQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cerca...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.performSearch(uiState.query)
                    focusManager.clearFocus()
                }
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = if (uiState.isMusicTab) 0 else 1) {
            Tab(selected = uiState.isMusicTab, onClick = { viewModel.updateTab(true) }, text = { Text("Musica") })
            Tab(selected = !uiState.isMusicTab, onClick = { viewModel.updateTab(false) }, text = { Text("Video") })
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentResults = if (uiState.isMusicTab) uiState.songResults else uiState.videoResults

                LazyVerticalGrid(
                    state = listState,
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = currentResults,
                    ) { track ->
                        val index = currentResults.indexOf(track)
                        if (index == currentResults.lastIndex && !uiState.isLoading && currentResults.isNotEmpty()) {
                            androidx.compose.runtime.LaunchedEffect(index) {
                                viewModel.performSearch(uiState.query, isLoadMore = true)
                            }
                        }

                        TrackGridItem(track, onClick = { onTrackClick(track) })
                    }

                    if (uiState.isLoading && currentResults.isNotEmpty()) {
                        item {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                if (uiState.isLoading && currentResults.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun TrackGridItem(track: TrackResult, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = track.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = track.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
        )
    }
}