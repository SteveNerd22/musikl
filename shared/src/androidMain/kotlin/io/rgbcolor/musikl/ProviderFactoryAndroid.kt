package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.opuswebm.AndroidOpusMusicPlayerProvider
import io.rgbcolor.musikl.search.newpipe.NewPipeSearchProvider

actual fun defaultMusicPlayerProvider(): MusicPlayerProvider? = null

actual fun registerProviders() {
    val newPipeSearchProvider = NewPipeSearchProvider()
    ProviderRegistry.registerSearchProvider(newPipeSearchProvider)
    val opusWebmProvider = AndroidOpusMusicPlayerProvider()
    ProviderRegistry.registerPlayerProvider(opusWebmProvider)
}