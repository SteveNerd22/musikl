package io.rgbcolor.musikl.search

import io.rgbcolor.musikl.model.TrackResult

interface MusicSearchProvider {
    suspend fun searchSongs(query: String, page: Int) : List<TrackResult>
    suspend fun searchVideos(query: String, page: Int) : List<TrackResult>
    suspend fun resolveStreamUrlInternal(pageUrl: String): String

    suspend fun warmUp() {}

    companion object {
        const val DEFAULT_PAGE_SIZE = 30
    }
}

suspend fun MusicSearchProvider.resolveStreamUrlSafe(pageUrl: String): String? {
    return try {
        resolveStreamUrlInternal(pageUrl)
    } catch (e: Exception) {
        null
    }
}