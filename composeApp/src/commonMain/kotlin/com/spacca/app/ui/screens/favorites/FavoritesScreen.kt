package com.spacca.app.ui.screens.favorites

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.unit.dp
import com.spacca.app.util.formatPrice
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.Favorite
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
fun FavoritesScreen(
    onBack: () -> Unit,
    onDrinkClick: (Favorite) -> Unit
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var favorites by remember { mutableStateOf<List<Favorite>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                favorites = api.favorites()
            } catch (e: Exception) {
                error = e.message ?: "Could not load favorites"
            } finally {
                loading = false
            }
        }
    }

LaunchedEffect(Unit) {
        load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Favorites", onBack = onBack)

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
            favorites.isEmpty() -> {
                DefaultEmptyState(
                    title = "No favorites yet",
                    body = "Tap the heart icon on drinks you love",
                    icon = Icons.Filled.Favorite
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites) { fav ->
                        FavoriteCard(
                            name = fav.drink?.name ?: "Drink",
                            price = fav.drink?.basePrice,
                            imageUrl = fav.drink?.imageUrl,
                            onClick = { onDrinkClick(fav) },
                            onRemove = {
                                scope.launch {
                                    try {
                                        api.removeFavorite(fav.drinkId)
                                        favorites = favorites.filterNot { it.drinkId == fav.drinkId }
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
private fun FavoriteCard(
    name: String,
    price: Double?,
    imageUrl: String?,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .clickableNoRipple(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
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
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultText(
                text = name,
                fontSize = 13,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            // Filled favorite icon (tap to remove)
            DefaultText(
                text = "♥",
                fontSize = 18,
                fontColor = Red,
                modifier = Modifier.clickableNoRipple(onClick = onRemove)
            )
        }
        DefaultText(
            text = if (price != null) "EGP ${price.formatPrice()}" else "",
            fontSize = 12,
            fontColor = AccentGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
