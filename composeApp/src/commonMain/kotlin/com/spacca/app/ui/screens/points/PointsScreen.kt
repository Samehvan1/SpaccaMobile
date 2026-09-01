package com.spacca.app.ui.screens.points

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.ApiService
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
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
fun PointsScreen(
    onBack: () -> Unit
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var totalPoints by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                totalPoints = api.points().points ?: 0
            } catch (e: Exception) {
                error = e.message ?: "Could not load points"
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
        DefaultTopBar(title = "Points", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Points balance card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBorder)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DefaultText(
                    text = if (loading) "..." else totalPoints.toString(),
                    fontSize = 40,
                    fontWeight = FontWeight.Bold,
                    fontColor = AccentGreen
                )
                DefaultText(
                    text = "Total Points",
                    fontSize = 14,
                    fontColor = LightGrey,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (error != null) {
                    DefaultText(
                        text = error ?: "",
                        fontSize = 12,
                        fontColor = Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    com.spacca.app.ui.components.DefaultButton(
                        text = "Retry",
                        onClick = { load() },
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DefaultText(
                text = "Earn points on every order and redeem them for free drinks and rewards.",
                fontSize = 13,
                fontColor = LightGrey,
                lineHeight = 18
            )
        }
    }
}
