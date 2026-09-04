package com.spacca.app.ui.screens.order

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.spacca.app.data.CartStore
import com.spacca.app.data.model.Discount
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OrderSummaryScreen(
    onBack: () -> Unit,
    onConfirm: (discountCode: String?) -> Unit,
    onError: (String) -> Unit = {}
) {
    val cartStore = koinInject<CartStore>()
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    val lineItems by cartStore.lines.collectAsState()

    val subtotal = lineItems.sumOf { it.unitPrice * it.quantity }
    val totalItemCount = lineItems.sumOf { it.quantity }

    // Available discounts (customer-linked) + discount code state.
    var availableDiscounts by remember { mutableStateOf<List<Discount>>(emptyList()) }
    var discountCode by remember { mutableStateOf("") }
    var appliedDiscount by remember { mutableStateOf<Discount?>(null) }
    var discountError by remember { mutableStateOf<String?>(null) }
    var applying by remember { mutableStateOf(false) }

    // Load the customer's available discounts once. The backend auto-applies the
    // customer's linked discount (e.g. STAFF50), so surface it here as the default.
    LaunchedEffect(Unit) {
        try {
            availableDiscounts = api.availableDiscounts()
            appliedDiscount = availableDiscounts.firstOrNull()
        } catch (_: Exception) {
            // Not logged in or fetch failed - no linked discount to show.
        }
    }

    // Local discount preview (the backend computes the authoritative value).
    // Products are tax-inclusive (14% VAT); discounts are NOT tax-included, so
    // percentage discounts are computed on the ex-tax (net) subtotal unless the
    // discount is explicitly marked taxable. Fixed discounts are already net values.
    val discountAmount = remember(appliedDiscount, subtotal, totalItemCount) {
        val d = appliedDiscount ?: return@remember 0.0
        val isTaxable = d.isTaxable ?: false
        val baseForCalc = if (isTaxable) subtotal else subtotal / 1.14
        when (d.type) {
            "percentage" -> baseForCalc * (d.value ?: 0.0) / 100
            "fixed" -> minOf(d.value ?: 0.0, subtotal)
            "fixed_per_item" -> minOf((d.value ?: 0.0) * totalItemCount, subtotal)
            else -> 0.0
        }
    }
    val taxes = subtotal * 0.14
    val total = subtotal + taxes - discountAmount

    fun applyCode() {
        val code = discountCode.trim()
        if (code.isEmpty()) return
        scope.launch {
            applying = true
            discountError = null
            try {
                val res = api.validateDiscount(code)
                if (res.valid && res.discount != null) {
                    appliedDiscount = res.discount
                } else {
                    discountError = res.error ?: "Invalid discount code"
                }
            } catch (e: Exception) {
                discountError = e.message ?: "Could not validate discount"
            } finally {
                applying = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Order Summary", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
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

            // Discount card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBorder)
                    .padding(14.dp)
            ) {
                DefaultText(
                    text = "Discount",
                    fontSize = 14,
                    fontWeight = FontWeight.SemiBold,
                    fontColor = Grey
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Customer-linked discount indicator
                val linked = availableDiscounts.firstOrNull()
                if (linked != null) {
                    DefaultText(
                        text = "You have ${linked.code} (${discountLabel(linked)}) applied",
                        fontSize = 12,
                        fontColor = AccentGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Discount code input + apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DefaultTextField(
                        value = discountCode,
                        onValueChange = { discountCode = it.uppercase() },
                        modifier = Modifier.weight(1f),
                        placeholder = "Enter discount code",
                        isError = discountError != null
                    )
                    DefaultButton(
                        text = "Apply",
                        onClick = { applyCode() },
                        modifier = Modifier.width(88.dp),
                        enabled = !applying
                    )
                }

                if (discountError != null) {
                    DefaultText(
                        text = discountError ?: "",
                        fontSize = 12,
                        fontColor = Red,
                        modifier = Modifier.padding(top = 6.dp)
                    )
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
                SummaryRow(label = "Discount", value = "- EGP ${"%.2f".format(discountAmount)}", valueColor = AccentGreen)

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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Place order button (pinned at bottom, outside scroll)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DefaultButton(
                text = "Pay & Place order",
                onClick = { onConfirm(discountCode.trim().ifEmpty { null }) }
            )
        }
    }
}

private fun discountLabel(d: Discount): String {
    val v = d.value ?: 0.0
    return when (d.type) {
        "percentage" -> "${v.toInt()}% off"
        "fixed" -> "EGP ${"%.2f".format(v)} off"
        "fixed_per_item" -> "EGP ${"%.2f".format(v)} per item"
        else -> d.code ?: ""
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
