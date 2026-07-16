package io.rgbcolor.musikl.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import io.rgbcolor.musikl.tab.TabContent

private val COLLAPSED_WIDTH = 72.dp
private val EXPANDED_WIDTH = 102.dp
private val ITEM_HEIGHT = 56.dp

@Composable
fun Sidebar(
    onNavigate: (TabContent) -> Unit,
    onNavigateNewTab: (TabContent) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val width by animateDpAsState(if (isHovered) EXPANDED_WIDTH else COLLAPSED_WIDTH)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .hoverable(interactionSource)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SidebarItem(
            icon = Icons.Filled.Home,
            label = "Home",
            expanded = isHovered,
            onClick = { onNavigate(TabContent.Home) },
            onMiddleClick = { onNavigateNewTab(TabContent.Home) },
        )
        SidebarItem(
            icon = Icons.Filled.Search,
            label = "Ricerca",
            expanded = isHovered,
            onClick = { onNavigate(TabContent.Search) },
            onMiddleClick = { onNavigateNewTab(TabContent.Search) },
        )
        SidebarItem(
            icon = Icons.Filled.LibraryMusic,
            label = "Playlist",
            expanded = isHovered,
            onClick = { onNavigate(TabContent.Playlist) },
            onMiddleClick = { onNavigateNewTab(TabContent.Playlist) }
        )
        SidebarItem(
            icon = Icons.Filled.Settings,
            label = "Opzioni",
            expanded = isHovered,
            onClick = { onNavigate(TabContent.Settings) },
            onMiddleClick = { onNavigateNewTab(TabContent.Settings) },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    onMiddleClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .height(ITEM_HEIGHT)
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Tertiary),
                onClick = onMiddleClick,
            )
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Primary),
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label)
        AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}