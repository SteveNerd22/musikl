package io.rgbcolor.musikl

import io.rgbcolor.musikl.ProviderRegistry.performHandshake
import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

expect fun defaultMusicSearchProvider(): MusicSearchProvider?
expect fun defaultMusicPlayerProvider(): MusicPlayerProvider?

object ProviderRegistry {
    private val registryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var _searchProvider: MusicSearchProvider? = null
    private var _playerProvider: MusicPlayerProvider? = null
    private var searchOverride: MusicSearchProvider? = null
    private var playerOverride: MusicPlayerProvider? = null
    private val warmedUpProviders = mutableSetOf<MusicProvider>()

    fun registerPlayerProvider(provider: MusicPlayerProvider) {
        playerOverride = provider
        triggerWarmUp(provider)
        performHandshake()
    }

    fun registerSearchProvider(provider: MusicSearchProvider) {
        searchOverride = provider
        triggerWarmUp(provider)
        performHandshake()
    }

    fun musicSearchProvider(): MusicSearchProvider {
        if (_searchProvider == null) {
            _searchProvider = searchOverride ?: defaultMusicSearchProvider()
                    ?: error("Nessun MusicSearchProvider disponibile")
        }

        val provider = _searchProvider!!
        triggerWarmUp(provider)
        performHandshake()
        return provider
    }

    fun musicPlayerProvider(): MusicPlayerProvider {
        if (_playerProvider == null) {
            _playerProvider = playerOverride ?: defaultMusicPlayerProvider()
                    ?: error("Nessun MusicSearchProvider disponibile")
        }

        val provider = _playerProvider!!
        triggerWarmUp(provider)
        performHandshake()
        return provider
    }

    private fun triggerWarmUp(provider: MusicProvider) {
        synchronized(warmedUpProviders) {
            if (provider !in warmedUpProviders) {
                warmedUpProviders.add(provider)
                registryScope.launch {
                    provider.warmUp()
                }
            }
        }
    }

    private fun performHandshake() {
        val search = _searchProvider ?: defaultMusicSearchProvider()
        val player = _playerProvider ?: defaultMusicPlayerProvider()

        if (search != null && player != null) {
            val playerCaps = player.capabilities
            println("[registry] invio capabilits ${player.capabilities} da {${player.javaClass}} a {${search.javaClass}}")
            search.onHandshake(playerCaps)
        }
    }
}