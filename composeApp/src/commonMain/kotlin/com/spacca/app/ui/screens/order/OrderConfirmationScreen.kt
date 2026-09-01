package com.spacca.app.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.ButtonVariant
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.BackgroundSecondary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Green
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.White

@Composable
fun OrderConfirmationScreen(
    orderNumber: String = "SPC-0000",
    onTrackOrder: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success checkmark circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Green, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Order confirmed",
                tint = White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Thank you text
        DefaultText(
            text = "Thank you for your order!",
            fontSize = 20,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Order number
        DefaultText(
            text = "Order #$orderNumber",
            fontSize = 14,
            fontColor = AccentGreen,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        DefaultText(
            text = "We'll notify you when your order is ready for pickup.",
            fontSize = 14,
            fontColor = LightGrey,
            textAlign = TextAlign.Center,
            lineHeight = 20
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Track Order button
        DefaultButton(
            text = "Track Order",
            onClick = onTrackOrder,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Continue Shopping button
        DefaultButton(
            text = "Continue Shopping",
            onClick = onContinueShopping,
            variant = ButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
