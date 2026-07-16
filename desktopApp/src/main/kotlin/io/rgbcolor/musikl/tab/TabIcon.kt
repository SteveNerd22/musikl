package io.rgbcolor.musikl.tab

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import io.rgbcolor.musikl.tab.TabIcon.Remote
import io.rgbcolor.musikl.tab.TabIcon.Vector

sealed interface TabIcon {
    data class Vector(val icon: ImageVector) : TabIcon
    data class Remote(val url: String) : TabIcon
}

fun iconFor(content: TabContent) : TabIcon {
    return when(content) {
        is TabContent.Playlist -> Vector(Icons.Filled.LibraryMusic)
        is TabContent.Search -> Vector(Icons.Filled.Search)
        is TabContent.Settings -> Vector(Icons.Filled.Settings)
        is TabContent.Home -> Vector(Icons.Filled.Home)
        is TabContent.Song -> Remote(content.track.thumbnailUrl)
    }
}