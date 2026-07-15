package io.rgbcolor.musikl

import io.rgbcolor.musikl.search.MusicSearchProvider
import io.rgbcolor.musikl.search.newpipe.NewPipeSearchProvider

actual fun defaultMusicSearchProvider(): MusicSearchProvider = NewPipeSearchProvider()