package com.spacca.app.data

import com.spacca.app.data.cache.CacheStore
import com.spacca.app.data.model.Branch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * App-wide branch cache. Branches are persisted to disk via [CacheStore] so the
 * nearest-branch pickup display still works when the app is launched offline.
 *
 * On load: serve fresh disk cache immediately; otherwise fetch from the API and
 * write to disk. If the network fails, fall back to the (possibly stale) disk
 * cache so the app remains usable offline.
 */
class BranchStore(
    private val api: ApiService,
    private val cache: CacheStore,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val key = "branches"
    private val ttl = 24 * 60 * 60 * 1000L // 1 day

    suspend fun load(force: Boolean = false) {
        if (_loaded.value && !force) return

        // Serve fresh disk cache immediately (no network).
        if (cache.isFresh(key, ttl)) {
            cache.get(key)?.let { raw ->
                runCatching { json.decodeFromString(ListSerializer(Branch.serializer()), raw) }
                    .getOrNull()?.let { list ->
                        _branches.value = list
                        _loaded.value = true
                        return
                    }
            }
        }

        try {
            val list = api.branches()
            _branches.value = list
            cache.put(key, json.encodeToString(ListSerializer(Branch.serializer()), list))
        } catch (_: Exception) {
            // Network failure: fall back to stale disk cache.
            if (_branches.value.isEmpty()) {
                cache.get(key)?.let { raw ->
                    runCatching { json.decodeFromString(ListSerializer(Branch.serializer()), raw) }
                        .getOrNull()?.let { _branches.value = it }
                }
            }
        }
        _loaded.value = true
    }
}
