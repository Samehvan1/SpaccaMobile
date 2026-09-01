package com.spacca.app.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.SavedDrink
import com.spacca.app.ui.components.DefaultEmptyState
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.SpaccaImage
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SavedDrinksScreen(
    onBack: () -> Unit
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var savedDrinks by remember { mutableStateOf<List<SavedDrink>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                savedDrinks = api.savedDrinks()
            } catch (e: Exception) {
                error = e.message ?: "Could not load saved drinks"
            } finally {
                loading = false
            }
        }
    }

    load()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Saved Drinks", onBack = onBack)

        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
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
                    com.spacca.app.ui.components.DefaultButton(text = "Retry", onClick = { load() })
                }
            }
            savedDrinks.isEmpty() -> {
                DefaultEmptyState(
                    title = "No saved drinks",
                    body = "Save your favorite customizations for quick reorder",
                    icon = Icons.Filled.Bookmark
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedDrinks) { drink ->
                        SavedDrinkCard(
                            name = drink.name ?: drink.drink?.name ?: "Drink",
                            customizations = drink.selections ?: "",
                            price = drink.drink?.basePrice,
                            imageUrl = drink.drink?.imageUrl,
                            onRemove = {
                                scope.launch {
                                    try {
                                        api.removeSavedDrink(drink.id)
                                        savedDrinks = savedDrinks.filterNot { it.id == drink.id }
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedDrinkCard(
    name: String,
    customizations: String,
    price: Double?,
    imageUrl: String?,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundPrimary),
                contentAlignment = Alignment.Center
            ) {
                SpaccaImage(
                    imageUrl = imageUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DefaultText(
                        text = name,
                        fontSize = 15,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    DefaultText(
                        text = if (price != null) "EGP ${"%.2f".format(price)}" else "",
                        fontSize = 13,
                        fontColor = AccentGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                DefaultText(
                    text = customizations,
                    fontSize = 12,
                    fontColor = LightGrey,
                    modifier = Modifier.padding(top = 6.dp)
                )
                DefaultText(
                    text = "Remove",
                    fontSize = 12,
                    fontColor = Red,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickableNoRipple(onClick = onRemove)
                )
            }
        }
    }
}
