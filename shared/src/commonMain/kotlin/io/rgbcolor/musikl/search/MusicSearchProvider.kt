package io.rgbcolor.musikl.search

import io.rgbcolor.musikl.model.TrackResult

interface MusicSearchProvider {
    suspend fun searchFirstSong(query: String): TrackResult
    suspend fun searchFirstVideo(query: String) : TrackResult
    suspend fun searchSongs(query: String) : List<TrackResult>
    suspend fun searchVideos(query: String) : List<TrackResult>
}