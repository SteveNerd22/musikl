package io.rgbcolor.musikl.search

import io.rgbcolor.musikl.MusicProvider
import io.rgbcolor.musikl.model.TrackResult

interface MusicSearchProvider : MusicProvider{
    suspend fun searchSongs(query: String, page: Int) : List<TrackResult>
    suspend fun searchVideos(query: String, page: Int) : List<TrackResult>
    suspend fun resolveStreamUrlInternal(pageUrl: String): String

    companion object {
        const val DEFAULT_PAGE_SIZE = 30
    }
}

suspend fun MusicSearchProvider.resolveStreamUrlSafe(pageUrl: String): String? {
    return try {
        resolveStreamUrlInternal(pageUrl)
    } catch (e: Exception) {
        println("MUSIKL resolveStreamUrlInternal fallito per $pageUrl: ${e.stackTraceToString()}")
        null
    }
}