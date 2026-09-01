package com.spacca.app.data

import com.spacca.app.data.cache.CacheStore
import com.spacca.app.data.model.CategoryProduct
import com.spacca.app.data.model.DrinkCategory
import com.spacca.app.data.model.DrinkDetailResponse
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Caching repository for the catalog (categories, products, drink details).
 *
 * Each module is cached to disk with a TTL. When a module is fresh it is served
 * straight from cache (no network). When stale, the repository performs an
 * incremental sync against the backend using the `since` cursor so only rows
 * that changed since the last sync are transferred and merged into the cache.
 *
 * If the network fails during a refresh, the stale cache is still returned so
 * the app remains usable offline.
 */
class CatalogRepository(
    private val api: ApiService,
    private val cache: CacheStore,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    // TTLs (millis)
    private val categoriesTtl = 24 * 60 * 60 * 1000L   // 1 day
    private val productsTtl = 60 * 60 * 1000L          // 1 hour
    private val drinkTtl = 60 * 60 * 1000L             // 1 hour

    private fun keyCategories() = "categories"
    private fun keyProducts(categoryId: Int) = "products_$categoryId"
    private fun keyDrink(drinkId: Int) = "drink_$drinkId"

    // ---- Categories ----
    suspend fun categories(): List<DrinkCategory> {
        val key = keyCategories()
        if (cache.isFresh(key, categoriesTtl)) {
            cache.get(key)?.let { raw ->
                runCatching { json.decodeFromString(ListSerializer(DrinkCategory.serializer()), raw) }
                    .getOrNull()?.let { return it }
            }
        }

        val lastUpdated = cache.lastUpdated(key)
        return try {
            if (lastUpdated != null) {
                // Incremental sync
                val sync = api.categoriesSince(lastUpdated.toString())
                val merged = mergeCategories(
                    current = cache.get(key)?.let { runCatching { json.decodeFromString(ListSerializer(DrinkCategory.serializer()), it) }.getOrNull() } ?: emptyList(),
                    changed = sync.categories,
                    deletedIds = sync.deletedIds
                )
                cache.put(key, json.encodeToString(ListSerializer(DrinkCategory.serializer()), merged))
                merged
            } else {
                // Full fetch
                val list = api.categories()
                cache.put(key, json.encodeToString(ListSerializer(DrinkCategory.serializer()), list))
                list
            }
        } catch (e: Exception) {
            // Fall back to stale cache on network failure
            cache.get(key)?.let { raw ->
                runCatching { json.decodeFromString(ListSerializer(DrinkCategory.serializer()), raw) }
                    .getOrNull()?.let { return it }
            }
            throw e
        }
    }

    // ---- Category products ----
    suspend fun categoryProducts(categoryId: Int): List<CategoryProduct> {
        val key = keyProducts(categoryId)
        if (cache.isFresh(key, productsTtl)) {
            cache.get(key)?.let { raw ->
                runCatching { json.decodeFromString(ListSerializer(CategoryProduct.serializer()), raw) }
                    .getOrNull()?.let { return it }
            }
        }

        val lastUpdated = cache.lastUpdated(key)
        return try {
            if (lastUpdated != null) {
                val sync = api.categoryProductsSince(categoryId, lastUpdated.toString())
                val merged = mergeProducts(
                    current = cache.get(key)?.let { runCatching { json.decodeFromString(ListSerializer(CategoryProduct.serializer()), it) }.getOrNull() } ?: emptyList(),
                    changed = sync.products,
                    deletedIds = sync.deletedIds
                )
                cache.put(key, json.encodeToString(ListSerializer(CategoryProduct.serializer()), merged))
                merged
            } else {
                val list = api.categoryProducts(categoryId)
                cache.put(key, json.encodeToString(ListSerializer(CategoryProduct.serializer()), list))
                list
            }
        } catch (e: Exception) {
            cache.get(key)?.let { raw ->
                runCatching { json.decodeFromString(ListSerializer(CategoryProduct.serializer()), raw) }
                    .getOrNull()?.let { return it }
            }
            throw e
        }
    }

    // ---- Drink detail ----
    suspend fun drinkDetail(drinkId: Int): DrinkDetailResponse {
        val key = keyDrink(drinkId)
        if (cache.isFresh(key, drinkTtl)) {
            cache.get(key)?.let { raw ->
                runCatching { json.decodeFromString(DrinkDetailResponse.serializer(), raw) }
                    .getOrNull()?.let { return it }
            }
        }
        return try {
            val detail = api.drinkDetail(drinkId)
            cache.put(key, json.encodeToString(DrinkDetailResponse.serializer(), detail))
            detail
        } catch (e: Exception) {
            cache.get(key)?.let { raw ->
                runCatching { json.decodeFromString(DrinkDetailResponse.serializer(), raw) }
                    .getOrNull()?.let { return it }
            }
            throw e
        }
    }

    // ---- Merge helpers ----
    private fun mergeCategories(
        current: List<DrinkCategory>,
        changed: List<DrinkCategory>,
        deletedIds: List<Int>
    ): List<DrinkCategory> {
        val deleted = deletedIds.toSet()
        val byId = current.filterNot { it.id in deleted }.associateBy { it.id }.toMutableMap()
        changed.forEach { byId[it.id] = it }
        return byId.values.sortedBy { it.sortOrder ?: Int.MAX_VALUE }
    }

    private fun mergeProducts(
        current: List<CategoryProduct>,
        changed: List<CategoryProduct>,
        deletedIds: List<Int>
    ): List<CategoryProduct> {
        val deleted = deletedIds.toSet()
        val byId = current.filterNot { it.id in deleted }.associateBy { it.id }.toMutableMap()
        changed.forEach { byId[it.id] = it }
        return byId.values.sortedWith(
            compareBy<CategoryProduct> { it.sortOrder ?: Int.MAX_VALUE }.thenBy { it.id }
        )
    }
}
