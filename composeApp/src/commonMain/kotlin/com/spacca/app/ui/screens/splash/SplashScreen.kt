package com.spacca.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey

/**
 * Branded splash / loading screen shown while the app determines the start
 * destination (logged-in vs PIN login vs onboarding). Navigation is handled
 * by AppNavHost; this screen is purely presentational.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DefaultText(
                text = "SPACCA",
                fontSize = 34,
                fontWeight = FontWeight.Bold
            )
            DefaultText(
                text = "Coffee & More",
                fontSize = 14,
                fontColor = LightGrey
            )
            CircularProgressIndicator(
                color = AccentGreen,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
