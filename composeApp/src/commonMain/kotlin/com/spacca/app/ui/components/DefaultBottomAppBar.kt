package com.spacca.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.DarkGrey
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White

enum class BottomTab(val label: String) {
    HOME("Home"),
    CATEGORIES("Categories"),
    CART("Cart"),
    MORE("More")
}

private val BottomTab.icon: ImageVector
    get() = when (this) {
        BottomTab.HOME -> Icons.Filled.Home
        BottomTab.CATEGORIES -> Icons.Filled.GridView
        BottomTab.CART -> Icons.Filled.ShoppingCart
        BottomTab.MORE -> Icons.Filled.Menu
    }

// Bottom app bar / navigation (app_style_theme_guide.md §5.4)
// Faithful port of the original spacca-android DefaultBottomAppBar: an animated
// pill-style bar where the selected tab expands to show its label with
// critically-damped spring animations.
@Composable
fun DefaultBottomAppBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    cartCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .height(60.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())
            .background(BackgroundPrimary)
            .clip(RoundedCornerShape(topEnd = 10.dp, topStart = 10.dp)),
        containerColor = DarkGrey,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val selected = tab == currentTab
                BottomNavItem(
                    tab = tab,
                    selected = selected,
                    badgeAmount = if (tab == BottomTab.CART) cartCount else 0
                ) {
                    if (!selected) onTabSelected(tab)
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    modifier: Modifier = Modifier,
    tab: BottomTab,
    selected: Boolean,
    badgeAmount: Int = 0,
    onClick: () -> Unit
) {
    // Critically-damped springs (fast, no bounce)
    val springFloat = remember { spring<Float>(dampingRatio = 1f, stiffness = 700f) }
    val springDp = remember { spring<Dp>(dampingRatio = 1f, stiffness = 700f) }
    val springColor = remember { spring<Color>(dampingRatio = 1f, stiffness = 700f) }
    val springSize = remember { spring<IntSize>(dampingRatio = 1f, stiffness = 700f) }
    val springOffset = remember { spring<IntOffset>(dampingRatio = 1f, stiffness = 700f) }

    val background by animateColorAsState(
        targetValue = if (selected) DarkBorder else DarkGrey,
        animationSpec = springColor, label = "bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) AccentGreen else Color.Transparent,
        animationSpec = springColor, label = "fg"
    )
    val innerPadding by animateDpAsState(
        targetValue = if (selected) 10.dp else 8.dp,
        animationSpec = springDp, label = "pad"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.96f,
        animationSpec = springFloat, label = "scale"
    )

    Box(
        modifier = modifier
            .clickableNoRipple { onClick() }
            .wrapContentSize()
            .background(background, RoundedCornerShape(42.dp))
            .animateContentSize(animationSpec = springSize)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = innerPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
            ) {
                BottomAppBarIcon(
                    isSelected = selected,
                    icon = tab.icon,
                    title = tab.label,
                    badgeAmount = badgeAmount
                )
            }

            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = springFloat) +
                        expandHorizontally(
                            animationSpec = springSize,
                            expandFrom = Alignment.Start
                        ) +
                        slideInHorizontally(animationSpec = springOffset) { -12 },
                exit = fadeOut(animationSpec = springFloat) +
                        shrinkHorizontally(
                            animationSpec = springSize,
                            shrinkTowards = Alignment.Start
                        ) +
                        slideOutHorizontally(animationSpec = springOffset) { -12 }
            ) {
                Text(
                    text = tab.label,
                    color = contentColor,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun BottomAppBarIcon(
    isSelected: Boolean,
    icon: ImageVector,
    title: String,
    badgeAmount: Int = 0
) {
    BadgedBox(badge = { BottomAppBarIconViewBadge(badgeAmount) }) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) AccentGreen else Grey
        )
    }
}

@Composable
private fun BottomAppBarIconViewBadge(count: Int = 0) {
    if (count > 0) {
        Badge(
            containerColor = Red,
            contentColor = White
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
