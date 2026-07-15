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

class SearchViewModel(private val provider: MusicSearchProvider) {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    fun performSearch(query: String, isMusic: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val results = withContext(Dispatchers.IO) {
                    if (isMusic) provider.searchSongs(query) else provider.searchVideos(query)
                }
                _uiState.value = _uiState.value.copy(results = results, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    suspend fun resolveStreamUrl(track: TrackResult): String =
        provider.resolveStreamUrl(track.pageUrl)
}

data class SearchUiState(
    val results: List<TrackResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)