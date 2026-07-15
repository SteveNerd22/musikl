package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.search.MusicSearchProvider

expect fun defaultMusicSearchProvider(): MusicSearchProvider
expect fun defaultMusicPlayerProvider(): MusicPlayerProvider?

object ProviderRegistry {
    private var searchOverride: MusicSearchProvider? = null
    private var playerOverride: MusicPlayerProvider? = null

    fun registerSearchProvider(provider: MusicSearchProvider) {
        searchOverride = provider
    }

    fun registerPlayerProvider(provider: MusicPlayerProvider) {
        playerOverride = provider
    }

    fun musicSearchProvider(): MusicSearchProvider =
        searchOverride ?: defaultMusicSearchProvider()

    fun musicPlayerProvider(): MusicPlayerProvider =
        playerOverride
            ?: defaultMusicPlayerProvider()
            ?: error(
                "Nessun MusicPlayerProvider disponibile su questa piattaforma. " +
                        "Registra un provider con ProviderRegistry.registerPlayerProvider(...) " +
                        "prima di usarlo (richiesto su Android)."
            )
}