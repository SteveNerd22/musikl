package io.rgbcolor.musikl.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import io.rgbcolor.musikl.Screen

@Composable
fun MyBottomNavigation(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentScreen == Screen.OPZIONI,
            onClick = { onScreenSelected(Screen.OPZIONI) },
            label = { Text("Opzioni") },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.RICERCA,
            onClick = { onScreenSelected(Screen.RICERCA) },
            label = { Text("Ricerca") },
            icon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.PLAYLIST,
            onClick = { onScreenSelected(Screen.PLAYLIST) },
            label = { Text("Playlist") },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) }
        )
    }
}