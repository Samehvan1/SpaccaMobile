package com.spacca.app.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
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
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.model.DrinkDetail
import com.spacca.app.data.model.DrinkIngredient
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.ButtonVariant
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
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ProductDetailsScreen(
    drink: DrinkDetail,
    onBack: () -> Unit,
    onAddToCart: (quantity: Int) -> Unit,
    onCustomize: () -> Unit
) {
    val catalog = koinInject<CatalogRepository>()
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableStateOf(1) }
    var isFavorite by remember { mutableStateOf(false) }
    var favoriteChecked by remember { mutableStateOf(false) }
    var ingredients by remember { mutableStateOf<List<DrinkIngredient>>(emptyList()) }
    var ingredientsLoaded by remember { mutableStateOf(false) }

    if (!ingredientsLoaded) {
        ingredientsLoaded = true
        scope.launch {
            try {
                // Order by customer index; hide ingredients with index 0 (visually only —
                // their cost is still included via the customization recipe).
                ingredients = catalog.drinkDetail(drink.id).ingredients
                    .filter { (it.customerSortOrder ?: 1) > 0 }
                    .sortedBy { it.customerSortOrder ?: 1 }
            } catch (_: Exception) {
                // ingredients are optional
            }
        }
    }

    // Load the real favorite state from the backend (guests/offline fall back to unfavorited).
    if (!favoriteChecked) {
        favoriteChecked = true
        scope.launch {
            try {
                isFavorite = api.favorites().any { it.drinkId == drink.id }
            } catch (_: Exception) {
                // guest session or offline — leave as unfavorited
            }
        }
    }

    val price = drink.price ?: 0.0
    val isCustomizable = drink.isCustomizable == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Product Details", onBack = onBack)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp)
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBorder),
                contentAlignment = Alignment.Center
            ) {
                SpaccaImage(
                    imageUrl = drink.imageUrl,
                    contentDescription = drink.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name + Favorite toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DefaultText(
                    text = drink.name ?: "Product",
                    fontSize = 20,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle favorite",
                    tint = AccentGreen,
                    modifier = Modifier
                        .size(28.dp)
                        .clickableNoRipple {
                            val target = !isFavorite
                            isFavorite = target // optimistic
                            scope.launch {
                                try {
                                    if (target) api.addFavorite(drink.id) else api.removeFavorite(drink.id)
                                } catch (_: Exception) {
                                    isFavorite = !target // revert on failure (guest/offline)
                                }
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price
            DefaultText(
                text = "EGP ${"%.2f".format(price)}",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                fontColor = AccentGreen
            )

            if (drink.description != null && drink.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                DefaultText(
                    text = drink.description,
                    fontSize = 14,
                    fontColor = LightGrey,
                    lineHeight = 20
                )
            }

            // Ingredients
            if (ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                DefaultText(
                    text = "Ingredients",
                    fontSize = 16,
                    fontWeight = FontWeight.SemiBold,
                    fontColor = Grey
                )
                Spacer(modifier = Modifier.height(12.dp))
                ingredients.forEach { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BackgroundSecondary)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // Ingredient title (slot)
                            DefaultText(
                                text = ingredient.slotLabel ?: ingredient.name ?: "Ingredient",
                                fontSize = 14,
                                fontWeight = FontWeight.SemiBold,
                                fontColor = White
                            )
                            // Option -> Volume (structured detail, when present)
                            val detail = listOfNotNull(
                                ingredient.optionLabel,
                                ingredient.volumeLabel
                            ).joinToString(" · ")
                            if (detail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                DefaultText(
                                    text = detail,
                                    fontSize = 12,
                                    fontColor = LightGrey
                                )
                            }
                        }
                        if (ingredient.isRequired == true) {
                            DefaultText(
                                text = "Required",
                                fontSize = 11,
                                fontColor = MediumGrey
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quantity stepper
            DefaultText(
                text = "Quantity",
                fontSize = 14,
                fontWeight = FontWeight.Medium,
                fontColor = Grey
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBorder)
                        .clickableNoRipple(enabled = quantity > 1) { quantity-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease quantity",
                        tint = if (quantity > 1) White else MediumGrey,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DefaultText(
                    text = quantity.toString(),
                    fontSize = 16,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                // Plus button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBorder)
                        .clickableNoRipple { quantity++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase quantity",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Pinned bottom action bar (always visible, never scrolls away)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(BackgroundPrimary)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DefaultButton(
                    text = "Customize",
                    onClick = onCustomize,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f)
                )
                DefaultButton(
                    text = "Add to Cart",
                    onClick = { onAddToCart(quantity) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        }
    }
}
