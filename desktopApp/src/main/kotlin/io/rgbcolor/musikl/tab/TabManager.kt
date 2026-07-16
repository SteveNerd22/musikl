package io.rgbcolor.musikl.tab

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.UUID

class TabManager {
    var tabs: List<Tab> by mutableStateOf(
        listOf(Tab(content = TabContent.Home, id = UUID.randomUUID().toString()))
    )
        private set
    var activeTabId: String by mutableStateOf(tabs.first().id)
        private set

    fun openContentInCurrentTab(content: TabContent) {
        tabs = tabs.map { if (it.id == activeTabId) it.copy(content = content) else it }
    }

    fun openContentInNewTab(content: TabContent) {
        val newTab = Tab(content = content, id = UUID.randomUUID().toString())
        tabs = tabs + newTab
        activeTabId = newTab.id
    }

    fun closeTab(id: String) {
        if (tabs.size == 1) return
        if (activeTabId == id && activeTabId == tabs.first().id)
            activeTabId = tabs[1].id
        else if (activeTabId == id) {
            val index = tabs.indexOfFirst { it.id == activeTabId }
            activeTabId = tabs[index-1].id
        }
        tabs = tabs.filterNot { it.id == id }
    }

    fun closeTab(i: Int) {
        if(i < tabs.size && i >= 0)
            closeTab(tabs[i].id)
    }

    fun closeTab(tab: Tab) {
        closeTab(tab.id)
    }

    fun activateTab (id: String) {
        if (tabs.any { it.id == id }) activeTabId = id
    }

    fun activateTab (i: Int) {
        if(i < tabs.size && i >= 0)
            activateTab(tabs[i].id)
    }

    fun activateTab (tab: Tab) {
        if(tabs.contains(tab))
            activeTabId = tab.id
    }
}