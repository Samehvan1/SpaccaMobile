package com.spacca.app.ui.screens.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToLong
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.NutritionHistoryBucket
import com.spacca.app.data.model.NutritionHistoryResponse
import com.spacca.app.data.model.NutritionSummaryResponse
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultEmptyState
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

@Composable
fun NutritionHistoryScreen(
    onBack: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()

    var summary by remember { mutableStateOf<NutritionSummaryResponse?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var history by remember { mutableStateOf<NutritionHistoryResponse?>(null) }
    var loadingHistory by remember { mutableStateOf(true) }
    var loadingSummary by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val periods = listOf("day", "week", "month")
    val tabLabels = listOf("Day", "Week", "Month")

    fun loadSummary() {
        scope.launch {
            loadingSummary = true
            try {
                summary = api.nutritionSummary()
            } catch (_: Exception) {
                // Summary is non-critical; silently fail
            } finally {
                loadingSummary = false
            }
        }
    }

    fun loadHistory(period: String) {
        scope.launch {
            loadingHistory = true
            error = null
            try {
                history = api.nutritionHistory(period)
            } catch (e: Exception) {
                error = e.message ?: "Could not load nutrition history"
            } finally {
                loadingHistory = false
            }
        }
    }

    fun loadAll() {
        loadSummary()
        loadHistory(periods[selectedTab])
    }

    LaunchedEffect(Unit) {
        loadAll()
    }

    LaunchedEffect(selectedTab) {
        loadHistory(periods[selectedTab])
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Nutrition History", onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today summary header card
            if (loadingSummary) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBorder)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentGreen,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            } else if (summary != null) {
                item {
                    TodaySummaryCard(
                        summary = summary!!,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // Tab row
            item {
                TabRow(
                    labels = tabLabels,
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                    modifier = Modifier.padding(top = if (loadingSummary || summary != null) 4.dp else 12.dp)
                )
            }

            // History content
            when {
                loadingHistory -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AccentGreen,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    }
                }
                error != null -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DefaultText(
                                text = error ?: "",
                                fontSize = 14,
                                fontColor = Red
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DefaultButton(
                                text = "Retry",
                                onClick = { loadHistory(periods[selectedTab]) }
                            )
                        }
                    }
                }
                history?.series.isNullOrEmpty() -> {
                    item {
                        DefaultEmptyState(
                            title = "No nutrition data yet",
                            body = "Your nutrition history will appear here once you start ordering",
                            icon = Icons.Filled.LocalCafe,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                else -> {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(
                        items = history!!.series,
                        key = { it.period ?: it.start ?: it.hashCode().toString() }
                    ) { bucket ->
                        BucketCard(bucket = bucket, periodType = periods[selectedTab])
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Today Summary Card ──────────────────────────────────────────────────────

@Composable
private fun TodaySummaryCard(
    summary: NutritionSummaryResponse,
    modifier: Modifier = Modifier
) {
    val today = summary.today
    val goals = summary.goals
    val calorieGoal = goals?.dailyCalorieGoal
    val currentCalories = today?.calories ?: 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .padding(16.dp)
    ) {
        DefaultText(
            text = "Today's Intake",
            fontSize = 16,
            fontWeight = FontWeight.SemiBold,
            fontColor = White
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (today != null) {
            // Calories with progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DefaultText(
                    text = "Calories",
                    fontSize = 13,
                    fontColor = LightGrey
                )
                DefaultText(
                    text = formatNutrientValue(currentCalories) +
                            if (calorieGoal != null) " / ${calorieGoal} kcal" else " kcal",
                    fontSize = 13,
                    fontWeight = FontWeight.Medium,
                    fontColor = AccentGreen
                )
            }

            if (calorieGoal != null && calorieGoal > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (currentCalories / calorieGoal).coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentGreen,
                    trackColor = BackgroundSecondary,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nutrient grid — only show values > 0
            val nutrientPairs = listOf(
                "Protein" to today.protein,
                "Carbs" to today.totalCarbs,
                "Sugars" to today.totalSugars,
                "Fat" to today.totalFat,
                "Caffeine" to today.caffeine,
                "Sodium" to today.sodium,
            )

            val visibleNutrients = nutrientPairs.filter { (it.second ?: 0.0) > 0.0 }

            if (visibleNutrients.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleNutrients.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { (label, value) ->
                                NutrientItem(
                                    label = label,
                                    value = value ?: 0.0,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty slot if odd count
                            if (row.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (today.itemsCount != null && today.itemsCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                DefaultText(
                    text = "${today.itemsCount} item${if (today.itemsCount != 1) "s" else ""} consumed today",
                    fontSize = 12,
                    fontColor = MediumGrey
                )
            }
        } else {
            DefaultText(
                text = "No data for today yet",
                fontSize = 13,
                fontColor = MediumGrey,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

// ── Nutrient mini-item ──────────────────────────────────────────────────────

@Composable
private fun NutrientItem(
    label: String,
    value: Double,
    modifier: Modifier = Modifier,
    unit: String = "g"
) {
    Column(modifier = modifier) {
        DefaultText(
            text = label,
            fontSize = 11,
            fontColor = MediumGrey
        )
        DefaultText(
            text = "${formatNutrientValue(value)}$unit",
            fontSize = 14,
            fontWeight = FontWeight.Medium,
            fontColor = White
        )
    }
}

// ── Tab Row ─────────────────────────────────────────────────────────────────

@Composable
private fun TabRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBorder)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (isSelected) Modifier.background(AccentGreen)
                        else Modifier.background(BackgroundSecondary)
                    )
                    .clickableNoRipple { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                DefaultText(
                    text = label,
                    fontSize = 13,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontColor = if (isSelected) BackgroundPrimary else LightGrey
                )
            }
        }
    }
}

// ── Bucket Card ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BucketCard(
    bucket: NutritionHistoryBucket,
    periodType: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .padding(16.dp)
    ) {
        // Header row: period + item count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultText(
                text = formatPeriodLabel(bucket.period, bucket.start, periodType),
                fontSize = 15,
                fontWeight = FontWeight.SemiBold,
                fontColor = White
            )
            bucket.itemsCount?.let { count ->
                DefaultText(
                    text = "$count item${if (count != 1) "s" else ""}",
                    fontSize = 12,
                    fontColor = MediumGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Nutrition totals
        val totals = bucket.totals
        if (totals != null) {
            val nutrientPairs = listOf(
                "Calories" to totals.calories,
                "Protein" to totals.protein,
                "Carbs" to totals.totalCarbs,
                "Sugars" to totals.totalSugars,
                "Fat" to totals.totalFat,
                "Caffeine" to totals.caffeine,
                "Sodium" to totals.sodium,
            )

            val visibleNutrients = nutrientPairs.filter { (it.second ?: 0.0) > 0.0 }

            if (visibleNutrients.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    visibleNutrients.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { (label, value) ->
                                val unit = if (label == "Calories") " kcal" else "g"
                                NutrientItem(
                                    label = label,
                                    value = value ?: 0.0,
                                    unit = unit,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                DefaultText(
                    text = "No nutrition data",
                    fontSize = 12,
                    fontColor = MediumGrey
                )
            }
        } else {
            DefaultText(
                text = "No nutrition data",
                fontSize = 12,
                fontColor = MediumGrey
            )
        }

        // Drinks consumed
        if (bucket.drinks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.LocalCafe,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                DefaultText(
                    text = "Drinks",
                    fontSize = 11,
                    fontWeight = FontWeight.Medium,
                    fontColor = MediumGrey
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                bucket.drinks.forEach { (drinkName, count) ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BackgroundSecondary)
                            .border(1.dp, MediumGrey.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DefaultText(
                            text = drinkName,
                            fontSize = 11,
                            fontColor = Grey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (count > 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                            DefaultText(
                                text = "×$count",
                                fontSize = 11,
                                fontWeight = FontWeight.SemiBold,
                                fontColor = AccentGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun formatNutrientValue(value: Double): String {
    return if (value == value.roundToLong().toDouble()) {
        value.roundToLong().toString()
    } else {
        // Cross-platform 1-decimal formatting (no JVM String.format)
        val rounded = (value * 10).roundToLong()
        val whole = rounded / 10
        val frac = kotlin.math.abs((rounded % 10).toInt())
        "$whole.$frac"
    }
}

private fun formatPeriodLabel(period: String?, start: String?, periodType: String): String {
    // Try to parse a friendly label from the raw period string
    val raw = period ?: start ?: ""
    if (raw.isBlank()) return "Unknown period"

    return when (periodType) {
        "day" -> {
            // Expecting something like "2026-09-08" or just a date string
            val parts = raw.split("-")
            if (parts.size == 3) {
                val months = listOf(
                    "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                )
                val monthIdx = parts[1].toIntOrNull() ?: 0
                val monthName = if (monthIdx in months.indices) months[monthIdx] else parts[1]
                "${parts[2].toIntOrNull()?.toString() ?: parts[2]} $monthName ${parts[0]}"
            } else {
                raw
            }
        }
        "week" -> {
            // Expecting something like "2026-W37"
            val parts = raw.split("-W")
            if (parts.size == 2) {
                "Week ${parts[1]}, ${parts[0]}"
            } else {
                raw
            }
        }
        "month" -> {
            // Expecting something like "2026-09"
            val parts = raw.split("-")
            if (parts.size >= 2) {
                val months = listOf(
                    "", "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                val monthIdx = parts[1].toIntOrNull() ?: 0
                val monthName = if (monthIdx in months.indices) months[monthIdx] else parts[1]
                "$monthName ${parts[0]}"
            } else {
                raw
            }
        }
        else -> raw
    }
}
