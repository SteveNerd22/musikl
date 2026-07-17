package io.rgbcolor.musikl

fun createSearchViewModel(): SearchViewModel =
    SearchViewModel(ProviderRegistry.musicSearchProvider())

fun createPlayerViewModel(): PlayerViewModel =
    PlayerViewModel(ProviderRegistry.musicPlayerProvider())