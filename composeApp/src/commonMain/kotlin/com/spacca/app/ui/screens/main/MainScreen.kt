package com.spacca.app.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.spacca.app.data.BranchStore
import com.spacca.app.data.CartStore
import com.spacca.app.data.location.LocationStore
import com.spacca.app.data.model.DrinkCategory
import com.spacca.app.data.model.HomeProduct
import com.spacca.app.ui.components.BottomTab
import com.spacca.app.ui.components.DefaultBottomAppBar
import com.spacca.app.ui.screens.cart.CartScreen
import com.spacca.app.ui.screens.categories.CategoriesScreen
import com.spacca.app.ui.screens.home.HomeScreen
import com.spacca.app.ui.screens.more.MoreScreen
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    onSearchClick: () -> Unit = {},
    onCategoryClick: (DrinkCategory) -> Unit = {},
    onCartClick: () -> Unit = {},
    onProfile: () -> Unit = {},
    onOrders: () -> Unit = {},
    onPoints: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onSavedDrinks: () -> Unit = {},
    onSavedCustomizedProducts: () -> Unit = {},
    onTerms: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onProductClick: (HomeProduct) -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(BottomTab.HOME) }
    val cartStore = koinInject<CartStore>()
    val cartLines by cartStore.lines.collectAsState()

    // On app open (main shell first appears), load branches and capture the
    // current location so the nearest branch can be determined.
    val locationStore = koinInject<LocationStore>()
    val branchStore = koinInject<BranchStore>()
    LaunchedEffect(Unit) {
        branchStore.load()
        locationStore.refresh()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            DefaultBottomAppBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                cartCount = cartLines.sumOf { it.quantity }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                BottomTab.HOME -> HomeScreen(
                    onSearchClick = onSearchClick,
                    onProfileClick = onProfile,
                    onCategoryClick = onCategoryClick,
                    onViewAllCategories = { currentTab = BottomTab.CATEGORIES },
                    onViewAllFavorites = onFavorites,
                    onViewAllSaved = onSavedDrinks,
                    onProductClick = onProductClick
                )
                BottomTab.CATEGORIES -> CategoriesScreen(
                    onCategoryClick = onCategoryClick
                )
                BottomTab.CART -> CartScreen(
                    onCheckout = onCartClick,
                    onStartShopping = { currentTab = BottomTab.HOME },
                    onContinueShopping = { currentTab = BottomTab.HOME }
                )
                BottomTab.MORE -> MoreScreen(
                    onProfile = onProfile,
                    onOrders = onOrders,
                    onPoints = onPoints,
                    onFavorites = onFavorites,
                    onSavedCustomizedProducts = onSavedCustomizedProducts,
                    onTerms = onTerms,
                    onPrivacy = onPrivacy
                )
            }
        }
    }
}
