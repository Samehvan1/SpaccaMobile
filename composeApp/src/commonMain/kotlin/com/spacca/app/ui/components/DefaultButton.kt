package com.spacca.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White

enum class ButtonVariant {
    PRIMARY, SECONDARY, DESTRUCTIVE
}

@Composable
fun DefaultButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    height: Int = 40
) {
    val fill = when (variant) {
        ButtonVariant.PRIMARY -> AccentGreen
        ButtonVariant.SECONDARY -> DarkBorder
        ButtonVariant.DESTRUCTIVE -> Red
    }
    val textColor = when (variant) {
        ButtonVariant.PRIMARY -> BackgroundPrimary
        ButtonVariant.SECONDARY -> White
        ButtonVariant.DESTRUCTIVE -> White
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) fill else DarkBorder)
            .clickableNoRipple(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
