package com.spacca.app.data

import com.spacca.app.data.cache.CacheStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Manages the API environment the app connects to (production VPS vs a local
 * dev server). The selected base URL is persisted to disk via [CacheStore] so
 * the choice survives app restarts, and it is applied immediately by updating
 * [ApiConfig.BASE_URL] (the Ktor client resolves it per-request).
 */
class EnvironmentStore(private val cacheStore: CacheStore) {
    private val key = "api_environment"
    private val json = Json { ignoreUnknownKeys = true }

    private val _baseUrl = MutableStateFlow(ApiConfig.BASE_URL)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    init {
        load()
    }

    /** The currently active base URL (VPS or local). */
    fun currentBaseUrl(): String = _baseUrl.value

    /** True when the active base URL is the production VPS. */
    fun isUsingVps(): Boolean = _baseUrl.value.trimEnd('/') == ApiConfig.VPS_URL.trimEnd('/')

    /**
     * Switches the active base URL and persists it. Takes effect immediately for
     * all subsequent API calls and image loads.
     */
    fun setBaseUrl(url: String) {
        val normalized = url.trim().trimEnd('/')
        if (normalized.isEmpty()) return
        ApiConfig.BASE_URL = normalized
        _baseUrl.value = normalized
        persist()
    }

    /** Switches to the production VPS. */
    fun useVps() = setBaseUrl(ApiConfig.VPS_URL)

    /** Switches to the given local server URL. */
    fun useLocal(url: String) = setBaseUrl(url)

    private fun load() {
        val raw = cacheStore.get(key) ?: return
        try {
            val s = json.decodeFromString<EnvironmentState>(raw)
            if (!s.baseUrl.isNullOrBlank()) {
                ApiConfig.BASE_URL = s.baseUrl.trimEnd('/')
                _baseUrl.value = ApiConfig.BASE_URL
            }
        } catch (e: Exception) {
            // ignore corrupt state
        }
    }

    private fun persist() {
        val s = EnvironmentState(_baseUrl.value)
        cacheStore.put(key, json.encodeToString(EnvironmentState.serializer(), s))
    }
}

@Serializable
private data class EnvironmentState(
    val baseUrl: String? = null
)
