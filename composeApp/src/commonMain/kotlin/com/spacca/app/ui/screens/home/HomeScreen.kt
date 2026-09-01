package com.spacca.app.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.ApiService
import com.spacca.app.data.BranchStore
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.location.LocationStore
import com.spacca.app.data.location.LocationStatus
import com.spacca.app.data.location.nearestBranch
import com.spacca.app.data.model.DrinkCategory
import com.spacca.app.data.model.HomeProduct
import com.spacca.app.data.model.HomeSliderItem
import com.spacca.app.data.model.SavedDrink
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.BackgroundSecondary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.DarkGrey
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import spaccamobile.composeapp.generated.resources.Res
import spaccamobile.composeapp.generated.resources.ic_category_placeholder
import spaccamobile.composeapp.generated.resources.ic_coffee_cup
import spaccamobile.composeapp.generated.resources.ic_home_slider_placeholder
import spaccamobile.composeapp.generated.resources.ic_home_top_bar_bg

// ---------------------------------------------------------------------------
// Static fallback data (used until the backend home endpoints exist)
// ---------------------------------------------------------------------------
private val fallbackSlider = listOf(
    HomeSliderItem(1, title = "American Coffee\nLike no other"),
    HomeSliderItem(2, title = "Cold Brew\nRefreshing & bold"),
    HomeSliderItem(3, title = "Signature Latte\nCrafted for you")
)

private val fallbackFeatured = listOf(
    HomeProduct(1, name = "Caramel Latte", price = "3.50", originalPrice = "4.50", isFeatured = true),
    HomeProduct(2, name = "Iced Americano", price = "2.75", isFeatured = true),
    HomeProduct(3, name = "Mocha Frappe", price = "4.00", isFeatured = true),
    HomeProduct(4, name = "Vanilla Cold Brew", price = "3.25", isFeatured = true)
)

private val fallbackOffers = listOf(
    HomeProduct(5, name = "Espresso", price = "1.50", originalPrice = "2.00", onSale = true),
    HomeProduct(6, name = "Flat White", price = "2.90", originalPrice = "3.50", onSale = true),
    HomeProduct(7, name = "Matcha Latte", price = "3.10", originalPrice = "3.90", onSale = true)
)

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit = {},
    onCategoryClick: (DrinkCategory) -> Unit = {},
    onViewAllCategories: () -> Unit = {},
    onViewAllSaved: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val catalog = koinInject<CatalogRepository>()
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<DrinkCategory>>(emptyList()) }
    var categoriesError by remember { mutableStateOf<String?>(null) }
    var points by remember { mutableStateOf<Int?>(null) }
    var savedDrinks by remember { mutableStateOf<List<SavedDrink>>(emptyList()) }
    var slider by remember { mutableStateOf<List<HomeSliderItem>>(fallbackSlider) }
    var featured by remember { mutableStateOf<List<HomeProduct>>(fallbackFeatured) }
    var offers by remember { mutableStateOf<List<HomeProduct>>(fallbackOffers) }

    // Current location + branches for the nearest-branch pickup display.
    val locationStore = koinInject<LocationStore>()
    val branchStore = koinInject<BranchStore>()
    val currentLocation by locationStore.location.collectAsState()
    val locationStatus by locationStore.status.collectAsState()
    val branches by branchStore.branches.collectAsState()
    val nearest = currentLocation?.let { nearestBranch(branches, it) }
    val pickupText = when {
        nearest != null -> nearest.name ?: "Cairo"
        locationStatus == LocationStatus.LOCATING -> "Locating..."
        else -> "Cairo"
    }

    fun loadData() {
        scope.launch {
            categoriesError = null
            try {
                categories = catalog.categories()
            } catch (e: Exception) {
                categoriesError = e.message ?: "Could not load categories"
            }
            try {
                points = api.points().points
            } catch (_: Exception) {
                // points are optional
            }
            try {
                savedDrinks = api.savedDrinks()
            } catch (_: Exception) {
                // saved drinks are optional
            }
            // Home endpoints are not implemented yet; fall back to static data on failure.
            try {
                val remote = api.homeSlider()
                if (remote.isNotEmpty()) slider = remote
            } catch (_: Exception) {
                // keep fallbackSlider
            }
            try {
                val remote = api.featuredProducts()
                if (remote.isNotEmpty()) featured = remote
            } catch (_: Exception) {
                // keep fallbackFeatured
            }
            try {
                val remote = api.offers()
                if (remote.isNotEmpty()) offers = remote
            } catch (_: Exception) {
                // keep fallbackOffers
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        HomeTopBar(onSearchClick = onSearchClick, pickupLocation = pickupText)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                CategoriesSection(
                    categories = categories,
                    categoriesError = categoriesError,
                    onRetry = { loadData() },
                    onCategoryClick = onCategoryClick,
                    onViewAll = onViewAllCategories
                )
            }

            item {
                PointsBanner(points = points ?: 0)
            }

            item {
                FeaturedCoffeeSection(slides = slider)
            }

            // Featured products: shown only when there is at least one.
            if (featured.isNotEmpty()) {
                item {
                    ProductSection(
                        title = "Featured",
                        products = featured,
                        showPrice = true,
                        onViewAll = onViewAllCategories
                    )
                }
            }

            // Offers: shown only when there is at least one offer.
            if (offers.isNotEmpty()) {
                item {
                    ProductSection(
                        title = "Offers",
                        products = offers,
                        showPrice = true,
                        showOriginalPrice = true,
                        onViewAll = onViewAllCategories
                    )
                }
            }

            // Saved products: shown only when there is at least one.
            if (savedDrinks.isNotEmpty()) {
                item {
                    SavedDrinksSection(savedDrinks = savedDrinks, onViewAll = onViewAllSaved)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HomeTopBar
// ---------------------------------------------------------------------------
@Composable
private fun HomeTopBar(onSearchClick: () -> Unit, pickupLocation: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(BackgroundPrimary)
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_home_top_bar_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DefaultText(
                    text = "SPACCA",
                    fontSize = 22,
                    fontWeight = FontWeight.Bold,
                    fontColor = White
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AccentGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = DarkGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefaultText(
                        text = "Pickup from",
                        fontSize = 12,
                        fontColor = White
                    )
                    DefaultText(
                        text = pickupLocation,
                        fontSize = 14,
                        fontWeight = FontWeight.SemiBold,
                        fontColor = AccentGreen
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickableNoRipple(onClick = onSearchClick)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CategoriesSection
// ---------------------------------------------------------------------------
@Composable
private fun CategoriesSection(
    categories: List<DrinkCategory>,
    categoriesError: String?,
    onRetry: () -> Unit,
    onCategoryClick: (DrinkCategory) -> Unit,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultText(
                text = "Categories",
                fontSize = 14,
                fontWeight = FontWeight.SemiBold
            )
DefaultText(
                text = "View all",
                fontSize = 12,
                fontColor = AccentGreen,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickableNoRipple(onClick = onViewAll)
            )
        }

        when {
            categoriesError != null -> {
                DefaultText(
                    text = categoriesError,
                    fontSize = 13,
                    fontColor = Red,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                com.spacca.app.ui.components.DefaultButton(
                    text = "Retry",
                    onClick = onRetry,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            categories.isEmpty() -> {
                DefaultText(
                    text = "Loading categories...",
                    fontSize = 13,
                    fontColor = LightGrey,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { cat ->
                        CategoryChip(
                            name = cat.name ?: "Category",
                            onClick = { onCategoryClick(cat) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CategoryChip - circular chip matching the original app's CategoryItem
// ---------------------------------------------------------------------------
@Composable
private fun CategoryChip(
    name: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickableNoRipple(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(65.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = AccentGreen,
                    shape = CircleShape
                )
                .background(BackgroundSecondary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_category_placeholder),
                contentDescription = name,
                contentScale = ContentScale.Inside,
                modifier = Modifier.size(45.dp)
            )
        }
        DefaultText(
            text = name,
            fontSize = 12,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// PointsBanner
// ---------------------------------------------------------------------------
@Composable
private fun PointsBanner(points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGrey)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DefaultText(
                text = "Drink. Enjoy. Win.",
                fontSize = 17,
                fontWeight = FontWeight.Bold
            )
            DefaultText(
                text = "You've $points pts in your balance",
                fontSize = 10,
                fontColor = Grey
            )
        }
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 30.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            DefaultText(
                text = "Show my pts",
                fontSize = 12,
                fontWeight = FontWeight.Medium,
                fontColor = DarkGrey,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------
// FeaturedCoffeeSection (auto-scrolling slider with backend fallback)
// ---------------------------------------------------------------------------
@Composable
private fun FeaturedCoffeeSection(slides: List<HomeSliderItem>) {
    if (slides.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount <= 1) return@LaunchedEffect
        while (true) {
            delay(5000)
            val next = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HorizontalPager(state = pagerState) { page ->
            val slide = slides[page]
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(Res.drawable.ic_home_slider_placeholder),
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                DefaultText(
                    text = slide.title ?: "",
                    fontSize = 20,
                    lineHeight = 26,
                    fontWeight = FontWeight.SemiBold,
                    fontColor = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 16.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            slides.indices.forEach { index ->
                val isSelected = index == pagerState.currentPage
                Canvas(
                    modifier = Modifier
                        .size(8.dp)
                        .clickableNoRipple {
                            scope.launch { pagerState.scrollToPage(index) }
                        },
                    onDraw = {
                        drawCircle(color = if (isSelected) AccentGreen else Color.LightGray)
                    }
                )
                if (index < slides.size - 1) Spacer(Modifier.width(3.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ProductSection (Featured / Offers) - horizontal row of product cards
// ---------------------------------------------------------------------------
@Composable
private fun ProductSection(
    title: String,
    products: List<HomeProduct>,
    showPrice: Boolean = true,
    showOriginalPrice: Boolean = false,
    onViewAll: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp, start = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultText(
                text = title,
                fontSize = 14,
                fontWeight = FontWeight.Bold
            )
            DefaultText(
                text = "View all",
                fontSize = 12,
                fontColor = AccentGreen,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickableNoRipple(onClick = onViewAll)
            )
        }
        Spacer(Modifier.height(24.dp))
        LazyRow(
            contentPadding = PaddingValues(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    showPrice = showPrice,
                    showOriginalPrice = showOriginalPrice
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: HomeProduct,
    showPrice: Boolean,
    showOriginalPrice: Boolean
) {
    Box(modifier = Modifier.width(126.dp)) {
        Box(
            modifier = Modifier
                .width(126.dp)
                .height(108.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BackgroundSecondary)
                .padding(10.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                DefaultText(
                    text = product.name ?: "Product",
                    fontSize = 12,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                if (showPrice) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DefaultText(
                            text = "EGP ${product.price ?: ""}",
                            fontSize = 12,
                            fontWeight = FontWeight.Bold,
                            fontColor = AccentGreen
                        )
                        if (showOriginalPrice && product.originalPrice != null) {
                            Spacer(Modifier.width(6.dp))
                            DefaultText(
                                text = "EGP ${product.originalPrice}",
                                fontSize = 10,
                                fontColor = Grey,
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                        }
                    }
                }
            }
        }
        Image(
            painter = painterResource(Res.drawable.ic_coffee_cup),
            contentDescription = product.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
        )
    }
}

// ---------------------------------------------------------------------------
// SavedDrinksSection
// ---------------------------------------------------------------------------
@Composable
private fun SavedDrinksSection(savedDrinks: List<SavedDrink>, onViewAll: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp, start = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultText(
                text = "Saved drinks",
                fontSize = 14,
                fontWeight = FontWeight.Bold
            )
            DefaultText(
                text = "View all",
                fontSize = 12,
                fontColor = AccentGreen,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickableNoRipple(onClick = onViewAll)
            )
        }
        Spacer(Modifier.height(24.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(savedDrinks) { drink ->
                Box(
                    modifier = Modifier
                        .width(126.dp)
                        .height(108.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBorder),
                    contentAlignment = Alignment.Center
                ) {
                    DefaultText(
                        text = drink.name ?: drink.drink?.name ?: "Drink",
                        fontSize = 13,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
