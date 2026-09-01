package com.spacca.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey

// Empty state (app_style_theme_guide.md §5.5)
@Composable
fun DefaultEmptyState(
    title: String,
    body: String = "",
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.WifiOff,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LightGrey,
            modifier = Modifier
                .size(80.dp)
                .padding(14.dp)
        )
        DefaultText(
            text = title,
            fontSize = 14,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        if (body.isNotEmpty()) {
            DefaultText(
                text = body,
                fontSize = 12,
                fontColor = LightGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (action != null) {
            Column(modifier = Modifier.padding(top = 30.dp)) {
                action()
            }
        }
    }
}
