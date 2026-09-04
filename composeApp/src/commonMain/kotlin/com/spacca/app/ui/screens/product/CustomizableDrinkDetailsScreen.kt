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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
import com.spacca.app.data.model.DrinkDetailResponse
import com.spacca.app.data.model.DrinkIngredient
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.ButtonVariant
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.SpaccaImage
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun CustomizableDrinkDetailsScreen(
    drinkId: Int,
    onBack: () -> Unit,
    onCustomize: () -> Unit = {},
    onAddToCart: (quantity: Int) -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<DrinkDetailResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                detail = api.drinkDetail(drinkId)
            } catch (e: Exception) {
                error = e.message ?: "Could not load drink details"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = detail?.drink?.name ?: "Drink Details", onBack = onBack)

        Box(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Drink image in a 200dp rounded box
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBorder),
                    contentAlignment = Alignment.Center
                ) {
                    SpaccaImage(
                        imageUrl = detail?.drink?.imageUrl,
                        contentDescription = detail?.drink?.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drink name
                DefaultText(
                    text = detail?.drink?.name ?: "Drink",
                    fontSize = 20,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                DefaultText(
                    text = "EGP ${(detail?.drink?.price ?: 0.0).formatPrice()}",
                    fontSize = 16,
                    fontColor = AccentGreen,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                val description = detail?.drink?.description
                if (description != null && description.isNotBlank()) {
                    DefaultText(
                        text = description,
                        fontSize = 13,
                        fontColor = LightGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Ingredients section
                val ingredients = detail?.ingredients
                if (ingredients?.isNotEmpty() == true) {
                    DefaultText(
                        text = "Ingredients",
                        fontSize = 16,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ingredients.forEach { ingredient ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            DefaultText(
                                text = ingredient.slotLabel ?: ingredient.name ?: "Ingredient",
                                fontSize = 14,
                                fontWeight = FontWeight.SemiBold
                            )
                            val subtitle = listOfNotNull(ingredient.optionLabel, ingredient.volumeLabel)
                                .joinToString(" · ")
                            if (subtitle.isNotBlank()) {
                                DefaultText(
                                    text = subtitle,
                                    fontSize = 12,
                                    fontColor = LightGrey
                                )
                            }
                        }
                    }
                }
            }

            // Pinned bottom action bar
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
                    if (detail?.drink?.isCustomizable == true) {
                        DefaultButton(
                            text = "Customize",
                            variant = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f),
                            onClick = onCustomize
                        )
                    }
                    DefaultButton(
                        text = "Add to Cart",
                        modifier = Modifier.weight(1f),
                        onClick = { onAddToCart(1) }
                    )
                }
            }

            // Loading overlay
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundPrimary),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DefaultText(text = "Loading...", fontSize = 14, fontColor = LightGrey)
                }
            }

            // Error overlay
            if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundPrimary),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DefaultText(text = error ?: "", fontSize = 14, fontColor = Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    DefaultButton(text = "Retry", onClick = { load() })
                }
            }
        }
    }
}