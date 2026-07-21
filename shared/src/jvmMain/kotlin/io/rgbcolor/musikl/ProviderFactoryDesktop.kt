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
    ProviderRegistry.registerSearchProvider { NewPipeSearchProvider() }
    ProviderRegistry.registerPlayerProvider { DesktopOpusMusicPlayerProvider() }
    //ProviderRegistry.registerPlayerProvider { JavaFxMusicPlayerProvider() } // temporaneamnete disabilitato
    ProviderRegistry.registerPlayerProvider { VlcjMusicPlayerProvider() }
}