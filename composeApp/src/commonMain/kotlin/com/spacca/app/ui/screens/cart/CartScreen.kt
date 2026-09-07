package com.spacca.app.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacca.app.util.formatPrice
import com.spacca.app.data.CartStore
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultEmptyState
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.Red
import org.koin.compose.koinInject

@Composable
fun CartScreen(
    onCheckout: () -> Unit = {},
    onStartShopping: () -> Unit = {},
    onContinueShopping: () -> Unit = {}
) {
    val cartStore = koinInject<CartStore>()
    val items by cartStore.lines.collectAsState()

    if (items.isEmpty()) {
        DefaultEmptyState(
            title = "Your cart looks empty!",
            body = "Add some drinks to get started",
            icon = Icons.Filled.ShoppingCart,
            action = {
                DefaultButton(text = "Start Shopping", onClick = onStartShopping)
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            DefaultText(
                text = "My Cart",
                fontSize = 20,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBorder)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            DefaultText(
                                text = item.name,
                                fontSize = 14
                            )
                            if (!item.customizationSummary.isNullOrBlank()) {
                                DefaultText(
                                    text = item.customizationSummary,
                                    fontSize = 12,
                                    fontColor = Grey
                                )
                            }
                        }
                        DefaultText(
                            text = "x${item.quantity}",
                            fontSize = 13,
                            fontColor = Grey
                        )
                        DefaultText(
                            text = "EGP ${(item.unitPrice * item.quantity).formatPrice()}",
                            fontSize = 14,
                            fontWeight = FontWeight.Medium,
                            fontColor = AccentGreen,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove ${item.name}",
                            tint = Red,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(22.dp)
                                .clickableNoRipple { cartStore.remove(item.id) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            DefaultButton(text = "Continue shopping", onClick = onContinueShopping)
            Spacer(Modifier.height(8.dp))
            DefaultButton(text = "Checkout", onClick = onCheckout)
        }
    }
}
