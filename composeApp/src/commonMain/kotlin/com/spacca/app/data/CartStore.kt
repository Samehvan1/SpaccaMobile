package com.spacca.app.data

import com.spacca.app.data.model.DrinkSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CartLine(
    val id: Long,
    val drinkId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val selections: List<DrinkSelection> = emptyList(),
    val customizationSummary: String? = null,
    val specialNotes: String? = null
)

// In-memory cart. There is no cart API — the cart is managed locally.
class CartStore {
    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    private var nextId = 1L

    fun add(line: CartLine) {
        _lines.value = _lines.value + line.copy(id = nextId++)
    }

    fun remove(id: Long) {
        _lines.value = _lines.value.filterNot { it.id == id }
    }

    fun clear() {
        _lines.value = emptyList()
    }

    val subtotal: Double
        get() = _lines.value.sumOf { it.unitPrice * it.quantity }
}
