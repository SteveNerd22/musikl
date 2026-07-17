package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

expect fun defaultMusicSearchProvider(): MusicSearchProvider
expect fun defaultMusicPlayerProvider(): MusicPlayerProvider?

object ProviderRegistry {
    private val registryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var searchOverride: MusicSearchProvider? = null
    private var playerOverride: MusicPlayerProvider? = null
    private val warmedUpProviders = mutableSetOf<MusicSearchProvider>()

    fun registerPlayerProvider(provider: MusicPlayerProvider) {
        playerOverride = provider
    }

    fun registerSearchProvider(provider: MusicSearchProvider) {
        searchOverride = provider
        triggerWarmUp(provider)
    }

    fun musicSearchProvider(): MusicSearchProvider {
        val provider = searchOverride ?: defaultMusicSearchProvider()
        triggerWarmUp(provider)
        return provider
    }

    private fun triggerWarmUp(provider: MusicSearchProvider) {
        synchronized(warmedUpProviders) {
            if (provider !in warmedUpProviders) {
                warmedUpProviders.add(provider)
                registryScope.launch {
                    provider.warmUp()
                }
            }
        }
    }

    fun musicPlayerProvider(): MusicPlayerProvider =
        playerOverride
            ?: defaultMusicPlayerProvider()
            ?: error(
                "Nessun MusicPlayerProvider disponibile su questa piattaforma. " +
                        "Registra un provider con ProviderRegistry.registerPlayerProvider(...) " +
                        "prima di usarlo (richiesto su Android)."
            )
}