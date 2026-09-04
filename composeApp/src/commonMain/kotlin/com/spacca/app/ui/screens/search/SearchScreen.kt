package com.spacca.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacca.app.util.formatPrice
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.model.CategoryProduct
import com.spacca.app.ui.components.DefaultEmptyState
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.LightGrey
import org.koin.compose.koinInject

data class SearchResult(
    val product: CategoryProduct
)

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onProductClick: (CategoryProduct) -> Unit
) {
    val catalog = koinInject<CatalogRepository>()
    var query by remember { mutableStateOf("") }
    var allDrinks by remember { mutableStateOf<List<CategoryProduct>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            // Aggregate every category's products into a single searchable index.
            val categories = catalog.categories()
            val products = mutableListOf<CategoryProduct>()
            for (category in categories) {
                runCatching { catalog.categoryProducts(category.id) }
                    .getOrNull()
                    ?.let { products.addAll(it) }
            }
            allDrinks = products
        } catch (e: Exception) {
            error = e.message ?: "Could not load products"
        } finally {
            loading = false
        }
    }

    val results = if (query.isBlank()) allDrinks
    else allDrinks.filter { it.name?.contains(query, ignoreCase = true) == true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Search", onBack = onBack)

        // Search field
        DefaultTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search drinks...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when {
            loading -> {
                DefaultText(
                    text = "Loading products...",
                    fontSize = 13,
                    fontColor = LightGrey,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            error != null -> {
                DefaultEmptyState(
                    title = "Could not load products",
                    body = error ?: "Try again later",
                    icon = Icons.Filled.Search
                )
            }
            results.isEmpty() -> {
                DefaultEmptyState(
                    title = "No results found",
                    body = "Try a different search term",
                    icon = Icons.Filled.Search
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results) { drink ->
                        SearchResultRow(SearchResult(drink)) { onProductClick(drink) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(drink: SearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DefaultText(
            text = drink.product.name ?: "Unknown",
            fontSize = 14,
            fontWeight = FontWeight.Medium
        )
        DefaultText(
            text = drink.product.price?.let { "${it.formatPrice()} EGP" } ?: "",
            fontSize = 13,
            fontColor = AccentGreen,
            fontWeight = FontWeight.SemiBold
        )
    }
}
