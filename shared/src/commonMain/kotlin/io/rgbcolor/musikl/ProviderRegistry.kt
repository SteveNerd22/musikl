package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.search.MusicSearchProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

expect fun defaultMusicSearchProvider(): MusicSearchProvider?
expect fun defaultMusicPlayerProvider(): MusicPlayerProvider?
expect fun registerProviders();

object ProviderRegistry {
    private val registryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var searchOverride: MusicSearchProvider? = null
    private var playerOverride: MusicPlayerProvider? = null
    private val warmedUpProviders = mutableSetOf<MusicProvider>()

    private val _activeSearchProviderName = MutableStateFlow<String?>(null)
    val activeSearchProviderName: StateFlow<String?> = _activeSearchProviderName.asStateFlow()

    private val _activePlayerProviderName = MutableStateFlow<String?>(null)
    val activePlayerProviderName: StateFlow<String?> = _activePlayerProviderName.asStateFlow()

    fun setPlayerProvider(providerName: String) {
        warmedUpProviders
            .filterIsInstance<MusicPlayerProvider>()
            .firstOrNull { it.name == providerName }
            ?.let { setActivePlayerProvider(it) }
        performHandshake()
    }

    fun setSearchProvider(providerName: String) {
        warmedUpProviders
            .filterIsInstance<MusicSearchProvider>()
            .firstOrNull { it.name == providerName }
            ?.let { setActiveSearchProvider(it) }
        performHandshake()
    }

    fun getPlayerProviders(): List<String> =
        warmedUpProviders.filterIsInstance<MusicPlayerProvider>().map { it.name }

    fun getSearchProviders(): List<String> =
        warmedUpProviders.filterIsInstance<MusicSearchProvider>().map { it.name }

    inline fun <reified T : MusicPlayerProvider> registerPlayerProvider(noinline factory: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        return triggerWarmUp(T::class, factory) as T
    }

    inline fun <reified T : MusicSearchProvider> registerSearchProvider(noinline factory: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        return triggerWarmUp(T::class, factory) as T
    }

    fun getWarmedUpProviders(): List<String> =
        synchronized(warmedUpProviders) {
            warmedUpProviders.map { "${it::class.simpleName} (${it.name})" }
        }

    fun musicSearchProvider(): MusicSearchProvider {
        registerProviders()
        if (searchOverride == null) {
            val default = defaultMusicSearchProvider()
                ?: error("Nessun MusicSearchProvider disponibile")
            setActiveSearchProvider(triggerWarmUp(default) as MusicSearchProvider)
        }

        val provider = searchOverride!!
        triggerWarmUp(provider)
        performHandshake()
        return provider
    }

    fun musicPlayerProvider(): MusicPlayerProvider {
        registerProviders()
        if (playerOverride == null) {
            val default = defaultMusicPlayerProvider()
                ?: error("Nessun MusicPlayerProvider disponibile")
            setActivePlayerProvider(triggerWarmUp(default) as MusicPlayerProvider)
        }

        val provider = playerOverride!!
        triggerWarmUp(provider)
        performHandshake()
        return provider
    }

    private fun setActiveSearchProvider(provider: MusicSearchProvider) {
        searchOverride = provider
        _activeSearchProviderName.value = provider.name
    }

    private fun setActivePlayerProvider(provider: MusicPlayerProvider) {
        playerOverride = provider
        _activePlayerProviderName.value = provider.name
    }

    @PublishedApi
    internal fun triggerWarmUp(key: KClass<*>, factory: () -> MusicProvider): MusicProvider {
        synchronized(warmedUpProviders) {
            val existing = warmedUpProviders.firstOrNull { it::class == key }
            if (existing != null) return existing

            val provider = factory()
            warmedUpProviders.add(provider)
            registryScope.launch { provider.warmUp() }
            return provider
        }
    }

    private fun triggerWarmUp(provider: MusicProvider): MusicProvider =
        triggerWarmUp(provider::class) { provider }

    private fun performHandshake() {
        val search = searchOverride ?: defaultMusicSearchProvider()
        val player = playerOverride ?: defaultMusicPlayerProvider()

        if (search != null && player != null) {
            val playerCaps = player.capabilities
            search.onHandshake(playerCaps)
        }
    }
}