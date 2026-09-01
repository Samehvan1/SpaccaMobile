package com.spacca.app.ui.screens.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacca.app.data.CartStore
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.White
import org.koin.compose.koinInject

@Composable
fun OrderSummaryScreen(
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val cartStore = koinInject<CartStore>()
    val lineItems by cartStore.lines.collectAsState()

    val subtotal = lineItems.sumOf { it.unitPrice * it.quantity }
    val taxes = subtotal * 0.14
    val discount = 0.0
    val total = subtotal + taxes - discount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Order Summary", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Line items card
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

                Spacer(modifier = Modifier.height(12.dp))

                lineItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DefaultText(
                            text = "${item.quantity}×  ${item.name}",
                            fontSize = 14,
                            modifier = Modifier.weight(1f)
                        )
                        DefaultText(
                            text = "EGP ${"%.2f".format(item.unitPrice * item.quantity)}",
                            fontSize = 14,
                            fontColor = LightGrey
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Totals card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBorder)
                    .padding(14.dp)
            ) {
                SummaryRow(label = "Subtotal", value = "EGP ${"%.2f".format(subtotal)}")
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(label = "Taxes (14%)", value = "EGP ${"%.2f".format(taxes)}")
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow(label = "Discount", value = "- EGP ${"%.2f".format(discount)}", valueColor = AccentGreen)

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(color = MediumGrey)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DefaultText(
                        text = "Total",
                        fontSize = 16,
                        fontWeight = FontWeight.Bold
                    )
                    DefaultText(
                        text = "EGP ${"%.2f".format(total)}",
                        fontSize = 16,
                        fontWeight = FontWeight.Bold,
                        fontColor = AccentGreen,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Place order button
            DefaultButton(
                text = "Pay & Place order",
                onClick = onConfirm,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DefaultText(
            text = label,
            fontSize = 14,
            fontColor = LightGrey
        )
        DefaultText(
            text = value,
            fontSize = 14,
            fontColor = valueColor,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
