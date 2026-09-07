package com.spacca.app.ui.screens.categories

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.CatalogRepository
import com.spacca.app.data.model.DrinkCategory
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.SpaccaImage
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.BackgroundSecondary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Categories page — a 2-column grid of category cards with rich placeholder tiles.
 *
 * Each card shows a generated placeholder image (a diagonal-gradient tile with a
 * category-specific decorative icon and the category's initial) until the backend
 * provides real category images via [DrinkCategory.image]. When `image` is present
 * it is rendered via [SpaccaImage] and takes precedence over the generated placeholder.
 *
 * Design: dark layered depth with pistachio accent highlights, animated press states,
 * border glow on interaction, category-specific Canvas-drawn icons, and staggered
 * entrance for each card.
 */
@Composable
fun CategoriesScreen(
    onCategoryClick: (DrinkCategory) -> Unit = {}
) {
    val catalog = koinInject<CatalogRepository>()
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<DrinkCategory>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                categories = catalog.categories()
            } catch (e: Exception) {
                error = e.message ?: "Could not load categories"
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
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // ── Section header with count badge + accent underline ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                DefaultText(
                    text = "Categories",
                    fontSize = 24,
                    fontWeight = FontWeight.Bold,
                    fontColor = White
                )
                // Accent underline peeking below the title
                Spacer(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(width = 36.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AccentGreen)
                )
            }

            // Category count pill
            if (!loading && error == null && categories.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DefaultText(
                        text = "${categories.size}",
                        fontSize = 13,
                        fontWeight = FontWeight.SemiBold,
                        fontColor = AccentGreen
                    )
                }
            }
        }

        when {
            loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DefaultText(text = "Loading...", fontSize = 14, fontColor = MediumGrey)
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
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(categories) { index, category ->
                        CategoryCard(
                            category = category,
                            index = index,
                            onClick = { onCategoryClick(category) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CategoryCard — layered dark tile with gradient placeholder + press animation.
// ---------------------------------------------------------------------------
@Composable
private fun CategoryCard(
    category: DrinkCategory,
    index: Int,
    onClick: () -> Unit
) {
    // Staggered entrance: each card fades + slides + scales in with a small delay offset
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(35L * index)
        visible = true
    }
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400)
    )
    val translationYAnim by animateFloatAsState(
        targetValue = if (visible) 0f else 16f,
        animationSpec = tween(durationMillis = 400)
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.94f,
        animationSpec = tween(durationMillis = 400)
    )

    // Press interaction: scale down slightly + border glow
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120)
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) AccentGreen.copy(alpha = 0.55f) else DarkBorder,
        animationSpec = tween(durationMillis = 180)
    )

    Column(
        modifier = Modifier
            .graphicsLayer {
                alpha = alphaAnim
                translationY = translationYAnim
                scaleX = entranceScale * pressScale
                scaleY = entranceScale * pressScale
            }
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .background(BackgroundSecondary)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Image area: gradient tile with decorative elements (or real image)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (category.image != null) {
                // Real backend image
                SpaccaImage(
                    imageUrl = category.image,
                    contentDescription = category.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                // Generated placeholder — the visual hero
                CategoryPlaceholder(
                    name = category.name,
                    index = index
                )
            }
        }

        // ── Category label ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tiny accent dot indicator
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentGreen.copy(alpha = 0.45f))
            )
            Spacer(modifier = Modifier.height(6.dp))
            DefaultText(
                text = category.name ?: "Category",
                fontSize = 13,
                fontWeight = FontWeight.SemiBold,
                fontColor = White.copy(alpha = 0.90f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            if (category.sortOrder != null) {
                DefaultText(
                    text = "${category.sortOrder}",
                    fontSize = 10,
                    fontWeight = FontWeight.Normal,
                    fontColor = MediumGrey,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CategoryPlaceholder — diagonal gradient tile + category icon + initial.
// ---------------------------------------------------------------------------

@Composable
private fun CategoryPlaceholder(
    name: String?,
    index: Int
) {
    val baseColor = categoryTint(name)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.60f),
                        baseColor.copy(alpha = 0.28f),
                        BackgroundSecondary.copy(alpha = 0.92f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Category-specific icon + decorative elements drawn via Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val iconColor = White.copy(alpha = 0.09f)
            val iconColorBright = White.copy(alpha = 0.13f)

            // Faint outer decorative ring
            drawCircle(
                color = White.copy(alpha = 0.04f),
                radius = size.minDimension * 0.40f,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )

            // Category-specific decorative icon
            drawCategoryIcon(name, cx, cy, size.minDimension, iconColor, iconColorBright)
        }

        // Centered initial letter badge
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.15f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = White.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            DefaultText(
                text = categoryInitial(name),
                fontSize = 22,
                fontWeight = FontWeight.Bold,
                fontColor = Color.White.copy(alpha = 0.93f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Category-specific icon drawing
// ---------------------------------------------------------------------------

/**
 * Draws a unique decorative icon for each category using basic Canvas primitives.
 * Icons are rendered at low alpha as background atmosphere behind the initial badge.
 * The 12 categories each get a distinct, coffee-shop-appropriate symbol.
 */
private fun DrawScope.drawCategoryIcon(
    name: String?,
    cx: Float,
    cy: Float,
    dim: Float,
    color: Color,
    colorBright: Color
) {
    val normalized = name?.lowercase()?.trim() ?: "other"

    when {
        // ── Hot Coffee — three rising steam zigzag lines ──
        "hot coffee" in normalized -> {
            for (i in -1..1) {
                val x = cx + i * dim * 0.09f
                val yBase = cy - dim * 0.06f
                val s = dim * 0.05f
                drawLine(colorBright, Offset(x, yBase), Offset(x + s * 0.5f, yBase - s), 1.5f)
                drawLine(colorBright, Offset(x + s * 0.5f, yBase - s), Offset(x - s * 0.4f, yBase - s * 2), 1.5f)
                drawLine(color, Offset(x - s * 0.4f, yBase - s * 2), Offset(x + s * 0.3f, yBase - s * 3f), 1.5f)
            }
        }

        // ── Cold Coffee — ice diamond / crystal ──
        "cold coffee" in normalized -> {
            val r = dim * 0.13f
            val diamond = Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r * 0.65f, cy)
                lineTo(cx, cy + r)
                lineTo(cx - r * 0.65f, cy)
                close()
            }
            drawPath(diamond, colorBright, style = Stroke(width = 1.5f))
            // Inner diamond
            val ri = r * 0.45f
            val inner = Path().apply {
                moveTo(cx, cy - ri)
                lineTo(cx + ri * 0.65f, cy)
                lineTo(cx, cy + ri)
                lineTo(cx - ri * 0.65f, cy)
                close()
            }
            drawPath(inner, color, style = Stroke(width = 1f))
        }

        // ── Frappe — spiral / swirl ──
        "frappe" in normalized -> {
            var angle = 0f
            var radius = dim * 0.025f
            val path = Path().apply { moveTo(cx, cy) }
            while (angle < 540f && radius < dim * 0.20f) {
                val rad = Math.toRadians(angle.toDouble()).toFloat()
                val x = cx + kotlin.math.cos(rad.toDouble()).toFloat() * radius
                val y = cy + kotlin.math.sin(rad.toDouble()).toFloat() * radius
                path.lineTo(x, y)
                angle += 10f
                radius += dim * 0.002f
            }
            drawPath(path, colorBright, style = Stroke(width = 1.5f))
        }

        // ── Matcha — leaf silhouette with vein ──
        "matcha" in normalized -> {
            val s = dim * 0.18f
            val leaf = Path().apply {
                moveTo(cx - s, cy + s * 0.3f)
                quadraticTo(cx, cy - s * 0.95f, cx + s, cy + s * 0.3f)
                quadraticTo(cx, cy + s * 0.1f, cx - s, cy + s * 0.3f)
            }
            drawPath(leaf, colorBright, style = Stroke(width = 1.5f))
            // Leaf vein
            drawLine(
                color,
                Offset(cx - s * 0.5f, cy + s * 0.22f),
                Offset(cx + s * 0.55f, cy - s * 0.35f),
                strokeWidth = 1f
            )
        }

        // ── Chillers — snowflake (3 crossing lines with branches) ──
        "chiller" in normalized -> {
            val arm = dim * 0.14f
            for (i in 0..2) {
                val angle = Math.toRadians((i * 60).toDouble()).toFloat()
                val dx = kotlin.math.cos(angle.toDouble()).toFloat()
                val dy = kotlin.math.sin(angle.toDouble()).toFloat()
                drawLine(colorBright,
                    Offset(cx - dx * arm, cy - dy * arm),
                    Offset(cx + dx * arm, cy + dy * arm), 1.5f)
                // Small perpendicular branches
                val bx = dx * arm * 0.6f
                val by = dy * arm * 0.6f
                val perpX = -dy * arm * 0.25f
                val perpY = dx * arm * 0.25f
                drawLine(color,
                    Offset(cx + bx - perpX, cy + by - perpY),
                    Offset(cx + bx + perpX, cy + by + perpY), 1f)
                drawLine(color,
                    Offset(cx - bx - perpX, cy - by - perpY),
                    Offset(cx - bx + perpX, cy - by + perpY), 1f)
            }
        }

        // ── Hot Drinks — three ascending steam dots ──
        "hot drink" in normalized -> {
            for (i in 0..2) {
                val r = dim * (0.022f + i * 0.008f)
                val yOff = dim * (0.05f + i * 0.085f)
                drawCircle(colorBright, r, Offset(cx, cy - yOff))
            }
        }

        // ── Specialty — four-pointed star / sparkle ──
        "special" in normalized -> {
            val s = dim * 0.15f
            val thin = s * 0.18f
            val star = Path().apply {
                moveTo(cx, cy - s)
                quadraticTo(cx + thin, cy - thin, cx + s, cy)
                quadraticTo(cx + thin, cy + thin, cx, cy + s)
                quadraticTo(cx - thin, cy + thin, cx - s, cy)
                quadraticTo(cx - thin, cy - thin, cx, cy - s)
            }
            drawPath(star, colorBright, style = Stroke(width = 1.5f))
            // Center dot
            drawCircle(color, dim * 0.018f, Offset(cx, cy))
        }

        // ── Coffee2 — double concentric ring ──
        "coffee2" in normalized -> {
            drawCircle(colorBright, dim * 0.15f, Offset(cx, cy), style = Stroke(width = 1.5f))
            drawCircle(color, dim * 0.085f, Offset(cx, cy), style = Stroke(width = 1f))
        }

        // ── Pastry — crescent arc ──
        "pastry" in normalized -> {
            drawArc(
                color = colorBright,
                startAngle = 140f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - dim * 0.14f, cy - dim * 0.14f),
                size = Size(dim * 0.28f, dim * 0.28f),
                style = Stroke(width = 1.5f)
            )
            // Small decorative dot at the opening
            drawCircle(color, dim * 0.022f, Offset(cx + dim * 0.07f, cy - dim * 0.04f))
        }

        // ── Food — plate shape (rounded rectangle + inner circle) ──
        "food" in normalized -> {
            drawRoundRect(
                color = colorBright,
                topLeft = Offset(cx - dim * 0.14f, cy - dim * 0.10f),
                size = Size(dim * 0.28f, dim * 0.20f),
                cornerRadius = CornerRadius(dim * 0.04f),
                style = Stroke(width = 1.5f)
            )
            drawCircle(color, dim * 0.04f, Offset(cx, cy))
        }

        // ── Snacks — triangle of dots ──
        "snack" in normalized -> {
            val r = dim * 0.038f
            val d = dim * 0.10f
            drawCircle(colorBright, r, Offset(cx, cy - d))
            drawCircle(color, r, Offset(cx - d * 0.87f, cy + d * 0.5f))
            drawCircle(color, r, Offset(cx + d * 0.87f, cy + d * 0.5f))
        }

        // ── Other / default — horizontal ellipsis (three dots) ──
        else -> {
            for (i in -1..1) {
                drawCircle(colorBright, dim * 0.028f, Offset(cx + i * dim * 0.10f, cy))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Placeholder data generation
// ---------------------------------------------------------------------------

/** A stable, distinct tint per category name. */
private fun categoryTint(name: String?): Color {
    val palette = listOf(
        Color(0xFF8B6B4A), // warm coffee brown
        Color(0xFF4A7A8B), // cool ocean blue
        Color(0xFF7A5A8E), // dusty purple
        Color(0xFF5A8B6B), // sage green
        Color(0xFF8B7A4A), // golden amber
        Color(0xFF8B4A5A), // muted berry
        Color(0xFF4A8B7A), // deep teal
        Color(0xFF6B5A8E), // muted indigo
        Color(0xFF8B5A4A), // terracotta
        Color(0xFF5A6B8B), // steel blue
        Color(0xFF7A8B4A), // olive
        Color(0xFF8B4A7A)  // warm magenta
    )
    val idx = (name?.hashCode() ?: 0).let { if (it == Int.MIN_VALUE) 0 else Math.abs(it) } % palette.size
    return palette[idx]
}

/** The category's first letter (uppercase) for the placeholder tile. */
private fun categoryInitial(name: String?): String {
    val n = name?.trim().orEmpty()
    return if (n.isEmpty()) "?" else n.first().uppercase()
}
