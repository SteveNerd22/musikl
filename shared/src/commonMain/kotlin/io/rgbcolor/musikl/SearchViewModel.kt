package io.rgbcolor.musikl

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.search.MusicSearchProvider
import io.rgbcolor.musikl.search.resolveStreamUrlSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(private val provider: MusicSearchProvider) {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    val musicListState = LazyListState()
    val videoListState = LazyListState()
    val musicGridState = LazyGridState()
    val videoGridState = LazyGridState()
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    private var songPage = 0
    private var videoPage = 0

    fun performSearch(query: String, isLoadMore: Boolean = false) {
        if (!isLoadMore) {
            songPage = 0
            videoPage = 0
            _uiState.value = _uiState.value.copy(
                songResults = emptyList(),
                videoResults = emptyList(),
                isLoading = true,
                error = null
            )
        } else {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val newSongs = withContext(Dispatchers.IO) { provider.searchSongs(query, page = songPage) }
                val newVideos = withContext(Dispatchers.IO) { provider.searchVideos(query, page = videoPage) }

                _uiState.value = _uiState.value.copy(
                    songResults = if (isLoadMore) _uiState.value.songResults + newSongs else newSongs,
                    videoResults = if (isLoadMore) _uiState.value.videoResults + newVideos else newVideos,
                    isLoading = false
                )

                if (newSongs.isNotEmpty()) songPage++
                if (newVideos.isNotEmpty()) videoPage++

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun updateTab(isMusicTab: Boolean) {
        _uiState.value = _uiState.value.copy(isMusicTab = isMusicTab)
    }

    suspend fun resolveStreamUrl(track: TrackResult): String? =
        withContext(Dispatchers.IO) {
            provider.resolveStreamUrlSafe(track.pageUrl)
        }
}

data class SearchUiState(
    val songResults: List<TrackResult> = emptyList(),
    val videoResults: List<TrackResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val isMusicTab: Boolean = true
)