package com.spacca.app.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spacca.app.util.formatPrice
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.OrderDetail
import com.spacca.app.ui.components.ButtonVariant
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.Green
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OrderDetailsScreen(
    orderId: Int,
    onBack: () -> Unit,
    onCancelled: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var order by remember { mutableStateOf<OrderDetail?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var cancelling by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                order = api.orderDetail(orderId)
            } catch (e: Exception) {
                error = e.message ?: "Could not load order"
            } finally {
                loading = false
            }
        }
    }

    // Load once when the screen first appears. Calling this directly in the
    // composable body would re-fire on every recomposition, keeping the screen
    // stuck on "Loading...".
    LaunchedEffect(Unit) {
        load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Order Details", onBack = onBack)

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
            order == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DefaultText(text = "Order not found", fontSize = 14, fontColor = LightGrey)
                }
            }
            else -> {
                val o = order!!
                val currentStep = when (o.status?.lowercase()) {
                    "accepted" -> 0
                    "preparing" -> 1
                    "ready" -> 2
                    "completed" -> 3
                    else -> 0
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Order header
                    DefaultText(
                        text = "Order #${o.orderNumber ?: ""}",
                        fontSize = 18,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DefaultText(
                        text = o.createdAt ?: "",
                        fontSize = 13,
                        fontColor = LightGrey
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Status stepper ---
                    StatusStepper(currentStep = currentStep)

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Order items card ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBorder)
                            .padding(14.dp)
                    ) {
                        DefaultText(
                            text = "Items",
                            fontSize = 14,
                            fontWeight = FontWeight.SemiBold,
                            fontColor = Grey
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        o.items?.forEach { item ->
                            val name = "${item.quantity ?: 1}× ${item.drinkName ?: "Item"}"
                            val price = "EGP ${(item.lineTotal ?: 0.0).formatPrice()}"
                            OrderItemLine(name = name, price = price)
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MediumGrey)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DefaultText(text = "Total", fontSize = 14, fontWeight = FontWeight.Bold)
                            DefaultText(text = "EGP ${(o.total ?: 0.0).formatPrice()}", fontSize = 14, fontWeight = FontWeight.Bold, fontColor = AccentGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Details cards ---
                    InfoCard(
                        icon = Icons.Filled.LocationOn,
                        label = "Pickup Branch",
                        value = o.branchName ?: "Branch",
                        subvalue = null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoCard(
                        icon = Icons.Filled.Payment,
                        label = "Payment Method",
                        value = o.paymentMethod ?: "—",
                        subvalue = null
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons — placed right after the details cards so they
                    // are fully visible without scrolling to the bottom.
                    val isPending = o.status?.lowercase() == "pending"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isPending) {
                            DefaultButton(
                                text = if (cancelling) "Cancelling..." else "Cancel Order",
                                onClick = {
                                    scope.launch {
                                        cancelling = true
                                        try {
                                            api.cancelOrder(orderId)
                                            onCancelled()
                                        } catch (e: Exception) {
                                            error = e.message ?: "Could not cancel order"
                                        } finally {
                                            cancelling = false
                                        }
                                    }
                                },
                                variant = ButtonVariant.DESTRUCTIVE,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        DefaultButton(
                            text = "Reorder",
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusStepper(currentStep: Int) {
    val steps = listOf("Accepted", "Preparing", "Ready")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, label ->
            val isCompleted = index < currentStep
            val isActive = index == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when {
                                isCompleted -> Green
                                isActive -> AccentGreen
                                else -> DarkBorder
                            },
                            shape = CircleShape
                        )
                        .then(
                            if (!isCompleted && !isActive) {
                                Modifier.border(1.dp, MediumGrey, CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted || isActive) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = label,
                            tint = if (isCompleted) White else BackgroundPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                DefaultText(
                    text = label,
                    fontSize = 11,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                    fontColor = when {
                        isCompleted -> White
                        isActive -> AccentGreen
                        else -> MediumGrey
                    },
                    textAlign = TextAlign.Center
                )
            }

            // Connector line between steps
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(top = 15.dp)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) Green else MediumGrey
                        )
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subvalue: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            DefaultText(text = label, fontSize = 12, fontColor = Grey)
            Spacer(modifier = Modifier.height(2.dp))
            DefaultText(text = value, fontSize = 14, fontWeight = FontWeight.Medium)
            if (subvalue != null) {
                DefaultText(text = subvalue, fontSize = 12, fontColor = LightGrey, lineHeight = 16)
            }
        }
    }
}

@Composable
private fun OrderItemLine(name: String, price: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DefaultText(text = name, fontSize = 14, modifier = Modifier.weight(1f))
        DefaultText(text = price, fontSize = 14, fontColor = LightGrey)
    }
}
