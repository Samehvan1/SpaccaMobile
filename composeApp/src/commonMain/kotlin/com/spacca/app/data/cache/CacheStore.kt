package com.spacca.app.data.cache

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import com.spacca.app.util.currentTimeMillis

/**
 * Simple file-based JSON cache with per-module TTL.
 *
 * Each cache entry is stored as a single file `<key>.json` containing:
 *   { "lastUpdated": <epochMillis>, "data": "<serialized payload JSON string>" }
 *
 * The `data` field is an opaque JSON string so the caller (repository) decides
 * how to (de)serialize the payload. This keeps the store generic and lets each
 * module cache lists, objects, or raw responses.
 */
class CacheStore(
    private val directory: Path,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    @Serializable
    private data class CacheEntry(
        val lastUpdated: Long,
        val data: String
    )

    /** Returns the cached payload JSON string for [key], or null if absent/corrupt. */
    fun get(key: String): String? {
        val file = file(key) ?: return null
        if (!fileSystem.exists(file)) return null
        return try {
            val text = fileSystem.read(file) { readUtf8() }
            json.decodeFromString<CacheEntry>(text).data
        } catch (e: Exception) {
            null
        }
    }

    /** Returns the epoch-millis timestamp the entry for [key] was last written, or null. */
    fun lastUpdated(key: String): Long? {
        val file = file(key) ?: return null
        if (!fileSystem.exists(file)) return null
        return try {
            val text = fileSystem.read(file) { readUtf8() }
            json.decodeFromString<CacheEntry>(text).lastUpdated
        } catch (e: Exception) {
            null
        }
    }

    /** True when an entry exists and its age is within [ttlMillis]. */
    fun isFresh(key: String, ttlMillis: Long, nowMillis: Long = currentTimeMillis()): Boolean {
        val last = lastUpdated(key) ?: return false
        return nowMillis - last <= ttlMillis
    }

    /** Writes [payloadJson] (a JSON string) for [key] with the current timestamp. */
    fun put(key: String, payloadJson: String, nowMillis: Long = currentTimeMillis()) {
        val file = file(key) ?: return
        try {
            fileSystem.createDirectories(file.parent!!)
            val entry = json.encodeToString(CacheEntry.serializer(), CacheEntry(nowMillis, payloadJson))
            fileSystem.write(file) { writeUtf8(entry) }
        } catch (e: Exception) {
            // Cache writes must never crash the app.
        }
    }

    /** Removes the entry for [key], if present. */
    fun remove(key: String) {
        val file = file(key) ?: return
        try {
            if (fileSystem.exists(file)) fileSystem.delete(file)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun file(key: String): Path? {
        val safe = key.replace(Regex("[^A-Za-z0-9._-]"), "_")
        if (safe.isBlank()) return null
        return directory.resolve("$safe.json")
    }
}
