package io.rgbcolor.musikl

fun createSearchViewModel(): SearchViewModel {
    val provider = createDefaultMusicSearchProvider()
    return SearchViewModel(provider)
}