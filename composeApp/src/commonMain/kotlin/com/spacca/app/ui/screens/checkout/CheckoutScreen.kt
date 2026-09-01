package com.spacca.app.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.spacca.app.data.model.Branch
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.BackgroundSecondary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class TimeSlot(val label: String)
private data class PaymentMethod(val name: String)

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPlaceOrder: (branchId: Int, paymentMethod: String) -> Unit
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var branches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var loadingBranches by remember { mutableStateOf(true) }
    var branchError by remember { mutableStateOf<String?>(null) }

    fun loadBranches() {
        scope.launch {
            loadingBranches = true
            branchError = null
            try {
                branches = api.branches()
            } catch (e: Exception) {
                branchError = e.message ?: "Could not load branches"
            } finally {
                loadingBranches = false
            }
        }
    }

    loadBranches()

    val timeSlots = remember {
        listOf(
            TimeSlot("ASAP (~15 min)"),
            TimeSlot("12:00 PM - 12:15 PM"),
            TimeSlot("12:15 PM - 12:30 PM"),
            TimeSlot("12:30 PM - 12:45 PM"),
            TimeSlot("1:00 PM - 1:15 PM")
        )
    }
    val paymentMethods = remember {
        listOf(
            PaymentMethod("SPACCA Pay"),
            PaymentMethod("Credit / Debit Card"),
            PaymentMethod("Cash")
        )
    }

    var selectedBranch by remember { mutableIntStateOf(0) }
    var selectedTimeSlot by remember { mutableIntStateOf(0) }
    var selectedPayment by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Checkout", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- Pickup Branch ---
            SectionHeader(icon = Icons.Filled.LocationOn, title = "Pickup Branch")

            when {
                loadingBranches -> {
                    DefaultText(
                        text = "Loading branches...",
                        fontSize = 13,
                        fontColor = LightGrey,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                branchError != null -> {
                    DefaultText(
                        text = branchError ?: "",
                        fontSize = 13,
                        fontColor = Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    com.spacca.app.ui.components.DefaultButton(
                        text = "Retry",
                        onClick = { loadBranches() },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                branches.isEmpty() -> {
                    DefaultText(
                        text = "No branches available",
                        fontSize = 13,
                        fontColor = LightGrey,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                else -> {
                    branches.forEachIndexed { index, branch ->
                        val isSelected = selectedBranch == index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BackgroundSecondary else DarkBorder)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AccentGreen else DarkBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickableNoRipple { selectedBranch = index }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                DefaultText(
                                    text = branch.name ?: "Branch",
                                    fontSize = 14,
                                    fontWeight = FontWeight.Medium
                                )
                                DefaultText(
                                    text = branch.address ?: "",
                                    fontSize = 12,
                                    fontColor = LightGrey,
                                    lineHeight = 16
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Pickup Time ---
            SectionHeader(icon = Icons.Filled.Schedule, title = "Pickup Time")

            timeSlots.forEachIndexed { index, slot ->
                val isSelected = selectedTimeSlot == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BackgroundSecondary else DarkBorder)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AccentGreen else DarkBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickableNoRipple { selectedTimeSlot = index }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DefaultText(
                        text = slot.label,
                        fontSize = 14,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Payment Method ---
            SectionHeader(icon = Icons.Filled.Payment, title = "Payment Method")

            paymentMethods.forEachIndexed { index, method ->
                val isSelected = selectedPayment == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BackgroundSecondary else DarkBorder)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AccentGreen else DarkBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickableNoRipple { selectedPayment = index }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DefaultText(
                        text = method.name,
                        fontSize = 14,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = AccentGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Place order button
            DefaultButton(
                text = "Pay & Place order",
                onClick = {
                    val branchId = branches.getOrNull(selectedBranch)?.id ?: 0
                    val payment = paymentMethods.getOrNull(selectedPayment)?.name ?: "Cash"
                    onPlaceOrder(branchId, payment)
                },
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        DefaultText(
            text = title,
            fontSize = 16,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}
