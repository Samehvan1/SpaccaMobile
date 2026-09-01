package com.spacca.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Material color-scheme mapping (app_style_theme_guide.md §2.3)
private val SpaccaColorScheme = darkColorScheme(
    primary = AccentGreen,
    secondary = MediumGrey,
    tertiary = LightGrey,
    background = DarkGrey,
    surface = BackgroundSecondary,
    onPrimary = DarkGrey,
    onSecondary = BackgroundPrimary,
    onTertiary = BackgroundPrimary,
    onBackground = LightGrey,
    onSurface = Grey,
    error = Red,
    onError = White,
    surfaceVariant = BackgroundSecondary,
    onSurfaceVariant = Grey,
    outline = DarkBorder
)

@Composable
fun SpaccaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpaccaColorScheme,
        typography = SpaccaTypography,
        content = content
    )
}
