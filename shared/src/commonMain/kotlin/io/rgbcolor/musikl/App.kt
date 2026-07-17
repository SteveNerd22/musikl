package io.rgbcolor.musikl

import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun createSearchViewModel(): SearchViewModel =
    SearchViewModel(ProviderRegistry.musicSearchProvider())

fun createPlayerViewModel(): PlayerViewModel =
    PlayerViewModel(ProviderRegistry.musicPlayerProvider())