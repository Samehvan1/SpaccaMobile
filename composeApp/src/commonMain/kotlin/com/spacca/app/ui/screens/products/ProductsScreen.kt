package com.spacca.app.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.model.CategoryProduct
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.SpaccaImage
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.BackgroundSecondary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ProductsScreen(
    categoryId: Int,
    categoryName: String,
    onBack: () -> Unit,
    onProductClick: (CategoryProduct) -> Unit
) {
    val catalog = koinInject<CatalogRepository>()
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<CategoryProduct>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                products = catalog.categoryProducts(categoryId)
            } catch (e: Exception) {
                error = e.message ?: "Could not load products"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(categoryId) {
        load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = categoryName, onBack = onBack)

        when {
            loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DefaultText(text = "Loading...", fontSize = 14, fontColor = LightGrey)
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DefaultText(text = error ?: "", fontSize = 14, fontColor = Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    DefaultButton(text = "Retry", onClick = { load() })
                }
            }
            products.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DefaultText(text = "No products in this category", fontSize = 14, fontColor = LightGrey)
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: CategoryProduct,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundSecondary)
            .clickableNoRipple(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBorder),
            contentAlignment = Alignment.Center
        ) {
            SpaccaImage(
                imageUrl = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        DefaultText(
            text = product.name ?: "Product",
            fontSize = 13,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            DefaultText(
                text = "EGP ${"%.2f".format(product.price ?: 0.0)}",
                fontSize = 13,
                fontWeight = FontWeight.Bold,
                fontColor = AccentGreen
            )
        }

        if (product.isCustomizable == true) {
            Spacer(modifier = Modifier.height(4.dp))
            DefaultText(
                text = "Customizable",
                fontSize = 10,
                fontColor = Grey,
                textAlign = TextAlign.Start
            )
        }
    }
}
