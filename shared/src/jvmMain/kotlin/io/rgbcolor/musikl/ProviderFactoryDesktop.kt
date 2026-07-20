package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.javafx.JavaFxMusicPlayerProvider
import io.rgbcolor.musikl.player.opuswebm.DesktopOpusMusicPlayerProvider
import io.rgbcolor.musikl.player.vlcj.VlcjMusicPlayerProvider
import io.rgbcolor.musikl.search.newpipe.NewPipeSearchProvider

actual fun defaultMusicPlayerProvider(): MusicPlayerProvider? {
    return DesktopOpusMusicPlayerProvider()
}

actual fun registerProviders() {
    val newPipeSearchProvider = NewPipeSearchProvider()
    ProviderRegistry.registerSearchProvider(newPipeSearchProvider)
    val opusProvider = DesktopOpusMusicPlayerProvider()
    ProviderRegistry.registerPlayerProvider(opusProvider)
    val javafxProvider = JavaFxMusicPlayerProvider()
    ProviderRegistry.registerPlayerProvider(javafxProvider)
    val vlcjMusicPlayerProvider = VlcjMusicPlayerProvider()
    ProviderRegistry.registerPlayerProvider(vlcjMusicPlayerProvider)
}