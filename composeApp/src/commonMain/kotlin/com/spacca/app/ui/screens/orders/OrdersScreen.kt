package com.spacca.app.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.OrderSummary
import com.spacca.app.ui.components.DefaultEmptyState
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Green
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OrdersScreen(
    onBack: () -> Unit,
    onOrderClick: (Int) -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<OrderSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                orders = api.orders()
            } catch (e: Exception) {
                error = e.message ?: "Could not load orders"
            } finally {
                loading = false
            }
        }
    }

    load()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "My Orders", onBack = onBack)

        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DefaultText(text = "Loading...", fontSize = 14, fontColor = LightGrey)
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
            orders.isEmpty() -> {
                DefaultEmptyState(
                    title = "No orders yet",
                    body = "Your order history will appear here",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderSummary, onClick: () -> Unit) {
    val statusColor = when (order.status?.lowercase()) {
        "completed" -> Green
        "cancelled" -> Red
        else -> MediumGrey
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .clickableNoRipple(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultText(
                text = order.orderNumber ?: "#${order.id}",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold
            )
            DefaultText(
                text = order.status ?: "",
                fontSize = 12,
                fontColor = statusColor,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DefaultText(text = order.createdAt ?: "", fontSize = 12, fontColor = LightGrey)
            DefaultText(
                text = "EGP ${"%.2f".format(order.total ?: 0.0)}",
                fontSize = 14,
                fontColor = AccentGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
