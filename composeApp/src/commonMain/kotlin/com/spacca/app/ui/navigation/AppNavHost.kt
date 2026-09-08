package com.spacca.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import com.spacca.app.data.ApiService
import com.spacca.app.data.CartStore
import com.spacca.app.data.SessionStore
import com.spacca.app.data.model.CategoryProduct
import com.spacca.app.data.model.DrinkCategory
import com.spacca.app.data.model.DrinkDetail
import com.spacca.app.ui.screens.auth.ConfirmPinScreen
import com.spacca.app.ui.screens.auth.LoginPinScreen
import com.spacca.app.ui.screens.auth.LoginScreen
import com.spacca.app.ui.screens.auth.OtpScreen
import com.spacca.app.ui.screens.auth.PinScreen
import com.spacca.app.ui.screens.auth.RegisterScreen
import com.spacca.app.ui.screens.cart.CartScreen
import com.spacca.app.ui.screens.categories.CategoriesScreen
import com.spacca.app.ui.screens.checkout.CheckoutScreen
import com.spacca.app.ui.screens.customization.CustomizationScreen
import com.spacca.app.ui.screens.favorites.FavoritesScreen
import com.spacca.app.ui.screens.friends.FriendsScreen
import com.spacca.app.ui.screens.home.HomeScreen
import com.spacca.app.ui.screens.main.MainScreen
import com.spacca.app.ui.screens.more.MoreScreen
import com.spacca.app.ui.screens.more.PrivacyPolicyScreen
import com.spacca.app.ui.screens.more.TermsAndConditionsScreen
import com.spacca.app.ui.screens.nutrition.NutritionHistoryScreen
import com.spacca.app.ui.screens.onboarding.OnboardingScreen
import com.spacca.app.ui.screens.order.OrderConfirmationScreen
import com.spacca.app.ui.screens.order.OrderDetailsScreen
import com.spacca.app.ui.screens.order.OrderSummaryScreen
import com.spacca.app.ui.screens.orders.OrdersScreen
import com.spacca.app.ui.screens.points.PointsScreen
import com.spacca.app.ui.screens.product.CustomizableDrinkDetailsScreen
import com.spacca.app.ui.screens.product.ProductDetailsScreen
import com.spacca.app.ui.screens.products.ProductsScreen
import com.spacca.app.ui.screens.profile.ChangePhoneScreen
import com.spacca.app.ui.screens.profile.ChangePinScreen
import com.spacca.app.ui.screens.profile.DeleteProfileReasonScreen
import com.spacca.app.ui.screens.profile.DeleteProfileScreen
import com.spacca.app.ui.screens.profile.EditProfileScreen
import com.spacca.app.ui.screens.profile.ProfileScreen
import com.spacca.app.ui.screens.saved.SavedCustomizedProductsScreen
import com.spacca.app.ui.screens.saved.SavedDrinksScreen
import com.spacca.app.ui.screens.search.SearchScreen
import com.spacca.app.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val OTP = "otp"
    const val PIN = "pin"
    const val CONFIRM_PIN = "confirm_pin"
    const val LOGIN_PIN = "login_pin"
    const val MAIN = "main"

    const val SEARCH = "search"
    const val PRODUCTS = "products"
    const val PRODUCT = "product"
    const val CUSTOMIZATION = "customization"
    const val CUSTOMIZABLE_DRINK_DETAILS = "customizable_drink_details"

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
    const val DELETE_PROFILE_REASON = "delete_profile_reason"
    const val DELETE_PROFILE = "delete_profile"
    const val POINTS = "points"
    const val FAVORITES = "favorites"
    const val SAVED_DRINKS = "saved_drinks"
    const val SAVED_CUSTOMIZED_PRODUCTS = "saved_customized_products"
    const val FRIENDS = "friends"
    const val NUTRITION_HISTORY = "nutrition_history"
    const val TERMS = "terms"
    const val PRIVACY = "privacy"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val session = koinInject<SessionStore>()
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()

    // Resume where the user left off: logged in → Home; registered (hasPin) →
    // phone + PIN login (no OTP); otherwise onboarding. The splash screen is
    // shown first and routes to the appropriate destination after a brief delay.
    val startDestination = Routes.SPLASH

    // Validate a restored session on startup. If the backend session expired
    // (me() returns null on 401), clear local state and route to the
    // appropriate login screen. Network failures (offline) keep the session.
    LaunchedEffect(Unit) {
        if (session.isLoggedIn.value) {
            try {
                val customer = api.me()
                if (customer == null) {
                    session.clearSession()
                    api.clearCookies()
                    val dest = if (session.hasPin.value == true)
                        "${Routes.LOGIN_PIN}?phone=${session.phone.value ?: ""}"
                    else Routes.ONBOARDING
                    navController.navigate(dest) { popUpTo(navController.graph.id) { inclusive = true } }
                }
            } catch (e: Exception) {
                // offline or other — keep the session
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            SplashScreen()
            // After a brief splash, route to the appropriate start destination.
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1200)
                val dest = when {
                    session.isLoggedIn.value -> Routes.MAIN
                    session.hasPin.value == true -> "${Routes.LOGIN_PIN}?phone=${session.phone.value ?: ""}"
                    else -> Routes.ONBOARDING
                }
                navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Routes.LOGIN) },
                onContinueAsGuest = { navController.navigate(Routes.MAIN) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onOtpSent = { phone -> navController.navigate("${Routes.OTP}?phone=$phone") },
                onPinLogin = { phone -> navController.navigate("${Routes.LOGIN_PIN}?phone=$phone") },
                onRegister = { phone -> navController.navigate("${Routes.REGISTER}?phone=$phone") }
            )
        }
        composable("${Routes.REGISTER}?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.read { getStringOrNull("phone") } ?: ""
            RegisterScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onContinue = { firstName, lastName, email, gender, dateOfBirth, referralCode ->
                    navController.navigate("${Routes.OTP}?phone=$phone")
                },
                onTerms = { navController.navigate(Routes.TERMS) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) }
            )
        }
        composable("${Routes.OTP}?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.read { getStringOrNull("phone") } ?: ""
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
            val phone = backStackEntry.arguments?.read { getStringOrNull("phone") } ?: ""
            PinScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onDone = { pin ->
                    navController.navigate("${Routes.CONFIRM_PIN}?phone=$phone&pin=$pin")
                }
            )
        }
        composable("${Routes.CONFIRM_PIN}?phone={phone}&pin={pin}") { backStackEntry ->
            val phone = backStackEntry.arguments?.read { getStringOrNull("phone") } ?: ""
            val pin = backStackEntry.arguments?.read { getStringOrNull("pin") } ?: ""
            ConfirmPinScreen(
                phone = phone,
                pin = pin,
                onBack = { navController.popBackStack() },
                onConfirmed = { navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } } }
            )
        }
        composable("${Routes.LOGIN_PIN}?phone={phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.read { getStringOrNull("phone") } ?: ""
            LoginPinScreen(
                phone = phone,
                onBack = { navController.popBackStack() },
                onDone = { navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } } }
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
                onSavedDrinks = { navController.navigate(Routes.SAVED_DRINKS) },
                onSavedCustomizedProducts = { navController.navigate(Routes.SAVED_CUSTOMIZED_PRODUCTS) },
                onTerms = { navController.navigate(Routes.TERMS) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
                onNutrition = { navController.navigate(Routes.NUTRITION_HISTORY) },
                onProductClick = { product ->
                    navController.navigate(
                        "${Routes.PRODUCT}?drinkId=${product.id}&name=${product.name ?: ""}&price=${product.price?.toDoubleOrNull() ?: 0.0}&customizable=false&imageUrl=${product.image ?: ""}"
                    )
                }
            )
        }

        // ---- Search / Products / Product / Customization ----
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onProductClick = { product ->
                    navController.navigate(
                        "${Routes.PRODUCT}?drinkId=${product.id}&name=${product.name ?: ""}&price=${product.price ?: 0.0}&customizable=${product.isCustomizable ?: false}&imageUrl=${product.imageUrl ?: ""}"
                    )
                }
            )
        }
        composable("${Routes.PRODUCTS}?categoryId={categoryId}&name={name}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.read { getStringOrNull("categoryId") }?.toIntOrNull() ?: 0
            val name = backStackEntry.arguments?.read { getStringOrNull("name") } ?: "Products"
            ProductsScreen(
                categoryId = categoryId,
                categoryName = name,
                onBack = { navController.popBackStack() },
                onProductClick = { product ->
                    navController.navigate(
                        "${Routes.PRODUCT}?drinkId=${product.id}&name=${product.name ?: ""}&price=${product.price ?: 0.0}&customizable=${product.isCustomizable ?: false}&imageUrl=${product.imageUrl ?: ""}"
                    )
                },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }
        composable("${Routes.PRODUCT}?drinkId={drinkId}&name={name}&price={price}&customizable={customizable}&imageUrl={imageUrl}") { backStackEntry ->
            val drinkId = backStackEntry.arguments?.read { getStringOrNull("drinkId") }?.toIntOrNull() ?: 0
            val name = backStackEntry.arguments?.read { getStringOrNull("name") } ?: "Product"
            val price = backStackEntry.arguments?.read { getStringOrNull("price") }?.toDoubleOrNull() ?: 0.0
            val customizable = backStackEntry.arguments?.read { getStringOrNull("customizable") }?.toBoolean() ?: false
            val imageUrl = backStackEntry.arguments?.read { getStringOrNull("imageUrl") } ?: ""
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
            val drinkId = backStackEntry.arguments?.read { getStringOrNull("drinkId") }?.toIntOrNull() ?: 0
            val name = backStackEntry.arguments?.read { getStringOrNull("name") } ?: "Product"
            val price = backStackEntry.arguments?.read { getStringOrNull("price") }?.toDoubleOrNull() ?: 0.0
            val qty = backStackEntry.arguments?.read { getStringOrNull("qty") }?.toIntOrNull() ?: 1
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
        composable("${Routes.CUSTOMIZABLE_DRINK_DETAILS}?drinkId={drinkId}") { backStackEntry ->
            val drinkId = backStackEntry.arguments?.read { getStringOrNull("drinkId") }?.toIntOrNull() ?: 0
            val cartStore = koinInject<CartStore>()
            CustomizableDrinkDetailsScreen(
                drinkId = drinkId,
                onBack = { navController.popBackStack() },
                onCustomize = {
                    navController.navigate("${Routes.CUSTOMIZATION}?drinkId=$drinkId&name=&price=0.0&qty=1")
                },
                onAddToCart = { quantity ->
                    cartStore.add(
                        com.spacca.app.data.CartLine(
                            id = 0,
                            drinkId = drinkId,
                            name = "",
                            quantity = quantity,
                            unitPrice = 0.0
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
                onStartShopping = { navController.popBackStack() },
                onContinueShopping = {
                    // Return to the category (ProductsScreen) the customer was
                    // browsing. If no category is in the back stack (e.g. cart
                    // opened straight from Home), fall back to the main screen.
                    val popped = navController.popBackStack(
                        "${Routes.PRODUCTS}?categoryId={categoryId}&name={name}",
                        inclusive = false
                    )
                    if (!popped) {
                        navController.popBackStack(Routes.MAIN, inclusive = false)
                    }
                }
            )
        }
        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onRequireLogin = { navController.navigate(Routes.LOGIN) },
                onPlaceOrder = { branchId, paymentMethod, pickupTime ->
                    navController.navigate("${Routes.ORDER_SUMMARY}?branchId=$branchId&payment=$paymentMethod&pickupTime=${pickupTime ?: ""}")
                }
            )
        }
        composable("${Routes.ORDER_SUMMARY}?branchId={branchId}&payment={payment}&pickupTime={pickupTime}") { backStackEntry ->
            val branchId = backStackEntry.arguments?.read { getStringOrNull("branchId") }?.toIntOrNull() ?: 0
            val payment = backStackEntry.arguments?.read { getStringOrNull("payment") } ?: "Cash"
            val pickupTime = backStackEntry.arguments?.read { getStringOrNull("pickupTime") }
            OrderSummaryScreen(
                onBack = { navController.popBackStack() },
                branchId = branchId,
                payment = payment,
                onError = { message ->
                    // Surface the failure instead of silently navigating to a
                    // fake confirmation. Pop back to checkout so the user can retry.
                    navController.popBackStack()
                },
                onSuccess = { orderNumber ->
                    navController.navigate("${Routes.ORDER_CONFIRMATION}?orderNumber=$orderNumber") {
                        popUpTo(Routes.MAIN)
                    }
                }
            )
        }
        composable("${Routes.ORDER_CONFIRMATION}?orderNumber={orderNumber}") { backStackEntry ->
            val orderNumber = backStackEntry.arguments?.read { getStringOrNull("orderNumber") } ?: "0000"
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
            val orderId = backStackEntry.arguments?.read { getStringOrNull("orderId") }?.toIntOrNull() ?: 0
            OrderDetailsScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() },
                onCancelled = { navController.popBackStack() },
                onReorder = { navController.navigate(Routes.CART) }
            )
        }

        // ---- Account screens ----
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onChangePhone = { navController.navigate(Routes.CHANGE_PHONE) },
                onChangePin = { navController.navigate(Routes.CHANGE_PIN) },
                onDeleteProfile = { navController.navigate(Routes.DELETE_PROFILE_REASON) },
                onLogout = {
                    scope.launch {
                        try { api.logout() } catch (e: Exception) { /* best-effort */ }
                        api.clearCookies()
                        session.clear()
                        navController.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } }
                    }
                }
            )
        }
        composable(Routes.DELETE_PROFILE_REASON) {
            DeleteProfileReasonScreen(
                onBack = { navController.popBackStack() },
                onNext = { reason ->
                    navController.navigate("${Routes.DELETE_PROFILE}?reason=${reason}")
                }
            )
        }
        composable("${Routes.DELETE_PROFILE}?reason={reason}") { backStackEntry ->
            val reason = backStackEntry.arguments?.read { getStringOrNull("reason") } ?: ""
            DeleteProfileScreen(
                reason = reason,
                onBack = { navController.popBackStack() },
                onKeep = { navController.popBackStack() },
                onDelete = {
                    scope.launch {
                        try { api.deleteAccount() } catch (e: Exception) { /* best-effort */ }
                        api.clearCookies()
                        session.clear()
                        navController.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } }
                    }
                }
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
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onDrinkClick = { fav ->
                    val drink = fav.drink
                    navController.navigate(
                        "${Routes.PRODUCT}?drinkId=${fav.drinkId}&name=${drink?.name ?: ""}&price=${drink?.basePrice ?: 0.0}&customizable=${drink?.isCustomizable ?: false}&imageUrl=${drink?.imageUrl ?: ""}"
                    )
                }
            )
        }
        composable(Routes.SAVED_DRINKS) {
            SavedDrinksScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SAVED_CUSTOMIZED_PRODUCTS) {
            SavedCustomizedProductsScreen(
                onBack = { navController.popBackStack() },
                onDrinkClick = { savedDrink ->
                    navController.navigate("${Routes.CUSTOMIZABLE_DRINK_DETAILS}?drinkId=${savedDrink.drinkId}")
                }
            )
        }
        composable(Routes.FRIENDS) {
            FriendsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.NUTRITION_HISTORY) {
            NutritionHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TERMS) {
            TermsAndConditionsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRIVACY) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
    }
}
