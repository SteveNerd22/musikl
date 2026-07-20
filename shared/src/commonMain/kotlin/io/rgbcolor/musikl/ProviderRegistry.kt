package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

expect fun defaultMusicSearchProvider(): MusicSearchProvider?
expect fun defaultMusicPlayerProvider(): MusicPlayerProvider?
expect fun registerProviders();

object ProviderRegistry {
    private val registryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var searchOverride: MusicSearchProvider? = null
    private var playerOverride: MusicPlayerProvider? = null
    private val warmedUpProviders = mutableSetOf<MusicProvider>()

    fun setPlayerProvider(providerName: String) {
        warmedUpProviders
            .filterIsInstance<MusicPlayerProvider>()
            .firstOrNull { it.name == providerName }
            ?.let { playerOverride = it }
        performHandshake()
    }

    fun setSearchProvider(providerName: String) {
        warmedUpProviders
            .filterIsInstance<MusicSearchProvider>()
            .firstOrNull { it.name == providerName }
            ?.let { searchOverride = it }
        performHandshake()
    }

    fun getPlayerProviders(): List<String> =
        warmedUpProviders.filterIsInstance<MusicPlayerProvider>().map { it.name }

    fun getSearchProviders(): List<String> =
        warmedUpProviders.filterIsInstance<MusicSearchProvider>().map { it.name }

    fun registerPlayerProvider(provider: MusicPlayerProvider) {
        triggerWarmUp(provider)
    }

    fun registerSearchProvider(provider: MusicSearchProvider) {
        triggerWarmUp(provider)
    }

    fun musicSearchProvider(): MusicSearchProvider {
        registerProviders()
        if (searchOverride == null) {
            searchOverride = defaultMusicSearchProvider()
                    ?: error("Nessun MusicSearchProvider disponibile")
        }

        val provider = searchOverride!!
        triggerWarmUp(provider)
        performHandshake()
        return provider
    }

    fun musicPlayerProvider(): MusicPlayerProvider {
        registerProviders()
        if (playerOverride == null) {
            playerOverride = defaultMusicPlayerProvider()
                    ?: error("Nessun MusicPlayerProvider disponibile")
        }

        val provider = playerOverride!!
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
        val search = searchOverride ?: defaultMusicSearchProvider()
        val player = playerOverride ?: defaultMusicPlayerProvider()

        if (search != null && player != null) {
            val playerCaps = player.capabilities
            println("[registry] invio capabilits ${player.capabilities} da {${player.javaClass}} a {${search.javaClass}}")
            search.onHandshake(playerCaps)
        }
    }
}