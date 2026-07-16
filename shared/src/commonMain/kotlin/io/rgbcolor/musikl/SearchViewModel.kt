package io.rgbcolor.musikl

import io.rgbcolor.musikl.model.TrackResult
import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class SearchViewModel(private val provider: MusicSearchProvider) {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val songs = withContext(Dispatchers.IO) { provider.searchSongs(query) }
                val videos = withContext(Dispatchers.IO) { provider.searchVideos(query) }

                _uiState.value = _uiState.value.copy(
                    songResults = songs,
                    videoResults = videos,
                    isLoading = false
                )
            } catch (e: CancellationException) {
                throw e
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

    suspend fun resolveStreamUrl(track: TrackResult): String =
        provider.resolveStreamUrl(track.pageUrl)
}

data class SearchUiState(
    val songResults: List<TrackResult> = emptyList(),
    val videoResults: List<TrackResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val isMusicTab: Boolean = true
)