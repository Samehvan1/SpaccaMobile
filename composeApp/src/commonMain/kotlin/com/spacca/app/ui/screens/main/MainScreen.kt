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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
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

// Persists the selected tab across navigation (e.g. More -> Profile -> back
// must return to the More tab, not reset to Home). Saved by enum name so it
// works on every platform.
private val BottomTabSaver = Saver<BottomTab, String>(
    save = { it.name },
    restore = { name -> BottomTab.entries.firstOrNull { it.name == name } ?: BottomTab.HOME }
)

@OptIn(ExperimentalComposeUiApi::class)
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
    onNutrition: () -> Unit = {},
    onProductClick: (HomeProduct) -> Unit = {}
) {
    var currentTab by rememberSaveable(stateSaver = BottomTabSaver) { mutableStateOf(BottomTab.HOME) }
    val cartStore = koinInject<CartStore>()
    val cartLines by cartStore.lines.collectAsState()

    // System back on any tab except Home returns to the Home tab instead of
    // exiting the app. On Home, the default back behavior (exit) is preserved.
    BackHandler(enabled = currentTab != BottomTab.HOME) {
        currentTab = BottomTab.HOME
    }

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
                    onShowPoints = onPoints,
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
                    onPrivacy = onPrivacy,
                    onNutrition = onNutrition
                )
            }
        }
    }
}
