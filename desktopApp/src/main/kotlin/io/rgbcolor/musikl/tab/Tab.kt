package io.rgbcolor.musikl.tab

import io.rgbcolor.musikl.SearchViewModel
import io.rgbcolor.musikl.createSearchViewModel

data class Tab(
    val content: TabContent,
    val id: String,
    val searchViewModel: SearchViewModel = createSearchViewModel()
)