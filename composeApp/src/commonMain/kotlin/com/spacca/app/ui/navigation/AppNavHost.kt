package com.spacca.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spacca.app.data.CartStore
import com.spacca.app.data.model.CategoryProduct
import com.spacca.app.data.model.DrinkCategory
import com.spacca.app.data.model.DrinkDetail
import com.spacca.app.data.model.OrderItem
import com.spacca.app.data.model.PlaceOrderRequest
import com.spacca.app.ui.screens.auth.LoginPinScreen
import com.spacca.app.ui.screens.auth.LoginScreen
import com.spacca.app.ui.screens.auth.OtpScreen
import com.spacca.app.ui.screens.auth.PinScreen
import com.spacca.app.ui.screens.cart.CartScreen
import com.spacca.app.ui.screens.categories.CategoriesScreen
import com.spacca.app.ui.screens.checkout.CheckoutScreen
import com.spacca.app.ui.screens.customization.CustomizationScreen
import com.spacca.app.ui.screens.favorites.FavoritesScreen
import com.spacca.app.ui.screens.friends.FriendsScreen
import com.spacca.app.ui.screens.home.HomeScreen
import com.spacca.app.ui.screens.main.MainScreen
import com.spacca.app.ui.screens.more.MoreScreen
import com.spacca.app.ui.screens.onboarding.OnboardingScreen
import com.spacca.app.ui.screens.order.OrderConfirmationScreen
import com.spacca.app.ui.screens.order.OrderDetailsScreen
import com.spacca.app.ui.screens.order.OrderSummaryScreen
import com.spacca.app.ui.screens.orders.OrdersScreen
import com.spacca.app.ui.screens.points.PointsScreen
import com.spacca.app.ui.screens.product.ProductDetailsScreen
import com.spacca.app.ui.screens.products.ProductsScreen
import com.spacca.app.ui.screens.profile.ChangePhoneScreen
import com.spacca.app.ui.screens.profile.ChangePinScreen
import com.spacca.app.ui.screens.profile.EditProfileScreen
import com.spacca.app.ui.screens.profile.ProfileScreen
import com.spacca.app.ui.screens.saved.SavedDrinksScreen
import com.spacca.app.ui.screens.search.SearchScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val OTP = "otp"
    const val PIN = "pin"
    const val LOGIN_PIN = "login_pin"
    const val MAIN = "main"

    const val SEARCH = "search"
    const val PRODUCTS = "products"
    const val PRODUCT = "product"
    const val CUSTOMIZATION = "customization"

    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDER_SUMMARY = "order_summary"
    const val ORDER_CONFIRMATION = "order_confirmation"
    const val ORDER_DETAILS = "order_details"
    const val ORDERS = "orders"

    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PHONE = "change_phone"
    const val CHANGE_PIN = "change_pin"
    const val POINTS = "points"
    const val FAVORITES = "favorites"
    const val SAVED_DRINKS = "saved_drinks"
    const val FRIENDS = "friends"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Routes.LOGIN) },
                onContinueAsGuest = { navController.navigate(Routes.MAIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onOtpSent = { phone -> navController.navigate("${Routes.OTP}?phone=$phone") }
            )
        }
        composable("${Routes.OTP}?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OtpScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onVerified = { hasPin ->
                    if (hasPin) {
                        navController.navigate("${Routes.LOGIN_PIN}?phone=$phone")
                    } else {
                        navController.navigate("${Routes.PIN}?phone=$phone")
                    }
                }
            )
        }
        composable("${Routes.PIN}?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            PinScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onDone = { navController.navigate(Routes.MAIN) { popUpTo(Routes.ONBOARDING) { inclusive = true } } }
            )
        }
        composable("${Routes.LOGIN_PIN}?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            LoginPinScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onDone = { navController.navigate(Routes.MAIN) { popUpTo(Routes.ONBOARDING) { inclusive = true } } }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onCategoryClick = { cat ->
                    navController.navigate("${Routes.PRODUCTS}?categoryId=${cat.id}&name=${cat.name ?: ""}")
                },
                onCartClick = { navController.navigate(Routes.CART) },
                onProfile = { navController.navigate(Routes.PROFILE) },
                onOrders = { navController.navigate(Routes.ORDERS) },
                onPoints = { navController.navigate(Routes.POINTS) },
                onFavorites = { navController.navigate(Routes.FAVORITES) },
                onSavedDrinks = { navController.navigate(Routes.SAVED_DRINKS) }
            )
        }

        // ---- Search / Products / Product / Customization ----
        composable(Routes.SEARCH) {
            SearchScreen(onBack = { navController.popBackStack() })
        }
        composable("${Routes.PRODUCTS}?categoryId={categoryId}&name={name}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 0
            val name = backStackEntry.arguments?.getString("name") ?: "Products"
            ProductsScreen(
                categoryId = categoryId,
                categoryName = name,
                onBack = { navController.popBackStack() },
                onProductClick = { product ->
                    navController.navigate(
                        "${Routes.PRODUCT}?drinkId=${product.id}&name=${product.name ?: ""}&price=${product.price ?: 0.0}&customizable=${product.isCustomizable ?: false}&imageUrl=${product.imageUrl ?: ""}"
                    )
                }
            )
        }
        composable("${Routes.PRODUCT}?drinkId={drinkId}&name={name}&price={price}&customizable={customizable}&imageUrl={imageUrl}") { backStackEntry ->
            val drinkId = backStackEntry.arguments?.getString("drinkId")?.toIntOrNull() ?: 0
            val name = backStackEntry.arguments?.getString("name") ?: "Product"
            val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 0.0
            val customizable = backStackEntry.arguments?.getString("customizable")?.toBoolean() ?: false
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            val drink = DrinkDetail(
                id = drinkId,
                name = name,
                price = price,
                isCustomizable = customizable,
                imageUrl = imageUrl
            )
            val cartStore = koinInject<CartStore>()
            ProductDetailsScreen(
                drink = drink,
                onBack = { navController.popBackStack() },
                onAddToCart = { quantity ->
                    cartStore.add(
                        com.spacca.app.data.CartLine(
                            id = 0,
                            drinkId = drinkId,
                            name = name,
                            quantity = quantity,
                            unitPrice = price
                        )
                    )
                    navController.navigate(Routes.CART)
                },
                onCustomize = {
                    navController.navigate("${Routes.CUSTOMIZATION}?drinkId=$drinkId&name=$name&price=$price&qty=1")
                }
            )
        }
        composable("${Routes.CUSTOMIZATION}?drinkId={drinkId}&name={name}&price={price}&qty={qty}") { backStackEntry ->
            val drinkId = backStackEntry.arguments?.getString("drinkId")?.toIntOrNull() ?: 0
            val name = backStackEntry.arguments?.getString("name") ?: "Product"
            val price = backStackEntry.arguments?.getString("price")?.toDoubleOrNull() ?: 0.0
            val qty = backStackEntry.arguments?.getString("qty")?.toIntOrNull() ?: 1
            val cartStore = koinInject<CartStore>()
            CustomizationScreen(
                drinkId = drinkId,
                name = name,
                price = price,
                initialQty = qty,
                onBack = { navController.popBackStack() },
                onAdd = { quantity, unitPrice, selections, customizationSummary ->
                    // Add to cart via CartStore
                    cartStore.add(
                        com.spacca.app.data.CartLine(
                            id = 0,
                            drinkId = drinkId,
                            name = name,
                            quantity = quantity,
                            unitPrice = unitPrice,
                            selections = selections,
                            customizationSummary = customizationSummary
                        )
                    )
                    navController.navigate(Routes.CART)
                }
            )
        }

        // ---- Cart / Checkout / Order flow ----
        composable(Routes.CART) {
            CartScreen(
                onCheckout = { navController.navigate(Routes.CHECKOUT) },
                onStartShopping = { navController.popBackStack() }
            )
        }
        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onPlaceOrder = { branchId, paymentMethod ->
                    navController.navigate("${Routes.ORDER_SUMMARY}?branchId=$branchId&payment=$paymentMethod")
                }
            )
        }
        composable("${Routes.ORDER_SUMMARY}?branchId={branchId}&payment={payment}") { backStackEntry ->
            val branchId = backStackEntry.arguments?.getString("branchId")?.toIntOrNull() ?: 0
            val payment = backStackEntry.arguments?.getString("payment") ?: "Cash"
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            val cartStore = koinInject<CartStore>()
            val api = koinInject<com.spacca.app.data.ApiService>()
            OrderSummaryScreen(
                onBack = { navController.popBackStack() },
                onConfirm = {
                    // Place order via ApiService
                    scope.launch {
                        try {
                            val items = cartStore.lines.value.map {
                                OrderItem(
                                    drinkId = it.drinkId,
                                    quantity = it.quantity,
                                    selections = it.selections.ifEmpty { null },
                                    specialNotes = it.specialNotes
                                )
                            }
                            val order = api.placeOrder(
                                PlaceOrderRequest(
                                    branchId = branchId,
                                    items = items,
                                    paymentMethod = payment
                                )
                            )
                            cartStore.clear()
                            val orderNumber = order?.orderNumber ?: "0000"
                            navController.navigate("${Routes.ORDER_CONFIRMATION}?orderNumber=$orderNumber") {
                                popUpTo(Routes.MAIN)
                            }
                        } catch (e: Exception) {
                            // ignore for now
                        }
                    }
                }
            )
        }
        composable("${Routes.ORDER_CONFIRMATION}?orderNumber={orderNumber}") { backStackEntry ->
            val orderNumber = backStackEntry.arguments?.getString("orderNumber") ?: "0000"
            OrderConfirmationScreen(
                orderNumber = orderNumber,
                onTrackOrder = { navController.navigate(Routes.ORDERS) },
                onContinueShopping = { navController.navigate(Routes.MAIN) { popUpTo(Routes.MAIN) { inclusive = true } } }
            )
        }
        composable(Routes.ORDERS) {
            OrdersScreen(
                onBack = { navController.popBackStack() },
                onOrderClick = { id -> navController.navigate("${Routes.ORDER_DETAILS}?orderId=$id") }
            )
        }
        composable("${Routes.ORDER_DETAILS}?orderId={orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
            OrderDetailsScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() },
                onCancelled = { navController.popBackStack() }
            )
        }

        // ---- Account screens ----
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onChangePhone = { navController.navigate(Routes.CHANGE_PHONE) },
                onChangePin = { navController.navigate(Routes.CHANGE_PIN) },
                onLogout = { navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.MAIN) { inclusive = true } } }
            )
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Routes.CHANGE_PHONE) {
            ChangePhoneScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.CHANGE_PIN) {
            ChangePinScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.POINTS) {
            PointsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.FAVORITES) {
            FavoritesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SAVED_DRINKS) {
            SavedDrinksScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.FRIENDS) {
            FriendsScreen(onBack = { navController.popBackStack() })
        }
    }
}
