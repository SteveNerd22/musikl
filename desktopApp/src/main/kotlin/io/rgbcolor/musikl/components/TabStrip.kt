package io.rgbcolor.musikl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.rgbcolor.musikl.tab.Tab
import io.rgbcolor.musikl.tab.TabIcon
import io.rgbcolor.musikl.tab.iconFor
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TabStrip(
    tabs: List<Tab>,
    activeTabId: String,
    onTabClick: (Tab) -> Unit,
    onTabClose: (Tab) -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val deltaY = event.changes.first().scrollDelta.y
                scope.launch { listState.scrollBy(deltaY * 60f) }
            },
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(tabs, key = { _, tab -> tab.id }) { _, tab ->
            TabChip(
                tab = tab,
                isActive = tab.id == activeTabId,
                onClick = { onTabClick(tab) },
                onClose = { onTabClose(tab) },
            )
        }
    }
}

@Composable
private fun TabChip(
    tab: Tab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    val background = if (isActive)
        MaterialTheme.colorScheme.primaryContainer
    else
        Color.Transparent

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (val icon = iconFor(tab.content)) {
            is TabIcon.Vector -> Icon(icon.icon, contentDescription = null)
            is TabIcon.Remote -> AsyncImage(
                model = icon.url,
                contentDescription = null,
                modifier = Modifier.size(28.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Chiudi tab")
        }
    }
}