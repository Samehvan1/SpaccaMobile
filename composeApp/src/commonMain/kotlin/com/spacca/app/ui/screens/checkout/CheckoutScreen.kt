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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.spacca.app.data.SessionStore
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
import com.spacca.app.util.formatTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private data class PaymentMethod(val label: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPlaceOrder: (branchId: Int, paymentMethod: String, pickupTime: String?) -> Unit,
    onRequireLogin: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val session = koinInject<SessionStore>()
    val scope = rememberCoroutineScope()
    var branches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var loadingBranches by remember { mutableStateOf(true) }
    var branchError by remember { mutableStateOf<String?>(null) }

    // Loyalty points for the "My Points" payment option.
    var points by remember { mutableStateOf(0) }

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

    // Load branches and loyalty points once when the screen first appears.
    // Calling these directly in the composable body would re-fire on every
    // recomposition, keeping the screen stuck on "Loading branches...".
    LaunchedEffect(Unit) {
        loadBranches()
        if (session.isLoggedIn.value) {
            try {
                points = api.points().points ?: 0
            } catch (_: Exception) {
                points = 0
            }
        }
    }

    // Pickup time options: ASAP (default) or schedule a custom time.
    var selectedTimeOption by remember { mutableIntStateOf(0) } // 0 = ASAP, 1 = Schedule
    val timePickerState = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
    var scheduledTime by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    // Payment methods: Cash, Card, My Points (only if the customer has points).
    val hasPoints = points > 0
    val paymentMethods = remember(hasPoints, points) {
        buildList {
            add(PaymentMethod("Cash", "cash"))
            add(PaymentMethod("Card", "card"))
            if (hasPoints) add(PaymentMethod("My Points ($points pts)", "points"))
        }
    }
    var selectedPayment by remember { mutableIntStateOf(0) }

    var selectedBranch by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Checkout", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
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

            val timeOptions = listOf("ASAP (~15 min)", "Schedule for later")
            timeOptions.forEachIndexed { index, label ->
                val isSelected = selectedTimeOption == index
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
                        .clickableNoRipple {
                            selectedTimeOption = index
                            timeError = null
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DefaultText(
                        text = label,
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

            if (selectedTimeOption == 1) {
                Spacer(modifier = Modifier.height(8.dp))
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(8.dp))
                DefaultText(
                    text = "Selected: ${formatTime(timePickerState.hour, timePickerState.minute)}",
                    fontSize = 14,
                    fontColor = AccentGreen,
                    fontWeight = FontWeight.Medium
                )
                if (timeError != null) {
                    DefaultText(
                        text = timeError ?: "",
                        fontSize = 12,
                        fontColor = Red,
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                        text = method.label,
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

            if (!hasPoints) {
                DefaultText(
                    text = "My Points is available once you have loyalty points.",
                    fontSize = 12,
                    fontColor = LightGrey,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- Place order button (pinned at bottom, outside scroll) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DefaultButton(
                text = "Pay & Place order",
                onClick = {
                    if (!session.isLoggedIn.value) {
                        onRequireLogin()
                        return@DefaultButton
                    }

                    // Validate scheduled time is in the future (within 24h window).
                    if (selectedTimeOption == 1) {
                        val now = kotlinx.datetime.Clock.System.now()
                            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                        val picked = kotlinx.datetime.LocalDateTime(
                            year = now.year,
                            monthNumber = now.monthNumber,
                            dayOfMonth = now.dayOfMonth,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            second = 0,
                            nanosecond = 0
                        )
                        if (!(picked > now)) {
                            timeError = "Please pick a future time"
                            return@DefaultButton
                        }
                        scheduledTime = formatTime(timePickerState.hour, timePickerState.minute)
                    } else {
                        scheduledTime = null
                    }

                    val branchId = branches.getOrNull(selectedBranch)?.id ?: 0
                    val payment = paymentMethods.getOrNull(selectedPayment)?.value ?: "cash"
                    onPlaceOrder(branchId, payment, scheduledTime)
                }
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
