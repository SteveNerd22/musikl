package io.rgbcolor.musikl.tab

import io.rgbcolor.musikl.model.TrackResult

sealed interface TabContent {
    data object Home : TabContent
    data object Playlist : TabContent
    data object Settings : TabContent
    data class Song(val track: TrackResult) : TabContent
    data object Search : TabContent
}