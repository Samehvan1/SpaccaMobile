package com.spacca.app.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
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
import com.spacca.app.data.model.SavedDrink
import com.spacca.app.ui.components.DefaultButton
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
fun SavedCustomizedProductsScreen(
    onBack: () -> Unit,
    onDrinkClick: (SavedDrink) -> Unit = {}
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

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "My Customized Drinks", onBack = onBack)

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
                    DefaultButton(text = "Retry", onClick = { load() })
                }
            }
            savedDrinks.isEmpty() -> {
                DefaultEmptyState(
                    title = "No customized drinks",
                    body = "Save your favorite customizations for quick reorder",
                    icon = Icons.Filled.Bookmark
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(savedDrinks) { drink ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkBorder)
                                .clickableNoRipple { onDrinkClick(drink) }
                                .padding(12.dp)
                        ) {
                            SpaccaImage(
                                imageUrl = drink.drink?.imageUrl,
                                contentDescription = drink.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DefaultText(
                                text = drink.name ?: "Drink",
                                fontSize = 13,
                                fontWeight = FontWeight.SemiBold
                            )
                            DefaultText(
                                text = "EGP ${(drink.drink?.basePrice ?: 0.0).formatPrice()}",
                                fontSize = 12,
                                fontColor = AccentGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            val selections = drink.selections
                            if (selections != null && selections.isNotBlank()) {
                                DefaultText(
                                    text = selections,
                                    fontSize = 11,
                                    fontColor = LightGrey,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}