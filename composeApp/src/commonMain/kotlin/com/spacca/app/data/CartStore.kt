package com.spacca.app.data

import com.spacca.app.data.cache.CacheStore
import com.spacca.app.data.model.DrinkSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CartLine(
    @SerialName("id") val id: Long,
    @SerialName("drinkId") val drinkId: Int,
    @SerialName("name") val name: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("unitPrice") val unitPrice: Double,
    @SerialName("selections") val selections: List<DrinkSelection> = emptyList(),
    @SerialName("customizationSummary") val customizationSummary: String? = null,
    @SerialName("specialNotes") val specialNotes: String? = null
)

/**
 * Local cart persisted to disk via [CacheStore] (key "cart") so it survives app
 * switches / backgrounding / process death. There is no cart API — the cart is
 * managed locally and restored on startup.
 */
class CartStore(
    private val cache: CacheStore
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    private var nextId = 1L

    init {
        // Restore a previously persisted cart (if any) on startup.
        val saved = cache.get(KEY)
        if (saved != null) {
            try {
                val restored = json.decodeFromString<List<CartLine>>(saved)
                _lines.value = restored
                nextId = (restored.maxOfOrNull { it.id } ?: 0L) + 1L
            } catch (_: Exception) {
                // Corrupt/old payload — start with an empty cart.
            }
        }
    }

    fun add(line: CartLine) {
        _lines.value = _lines.value + line.copy(id = nextId++)
        persist()
    }

    fun remove(id: Long) {
        _lines.value = _lines.value.filterNot { it.id == id }
        persist()
    }

    fun clear() {
        _lines.value = emptyList()
        persist()
    }

    val subtotal: Double
        get() = _lines.value.sumOf { it.unitPrice * it.quantity }

    private fun persist() {
        try {
            cache.put(KEY, json.encodeToString(_lines.value))
        } catch (_: Exception) {
            // Persistence must never crash the app.
        }
    }

    private companion object {
        const val KEY = "cart"
    }
}
