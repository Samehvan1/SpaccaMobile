package com.spacca.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.White

// Top app bar (app_style_theme_guide.md §5.3)
@Composable
fun DefaultTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(BackgroundPrimary)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (showBack) {
            Icon(
                imageVector = if (showBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Close,
                contentDescription = "Back",
                tint = White,
                modifier = Modifier
                    .size(24.dp)
                    .clickableNoRipple(onClick = onBack)
            )
        }
        Text(
            text = title,
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
        if (trailingContent != null) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                trailingContent()
            }
        }
    }
}
