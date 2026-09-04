package com.spacca.app.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.components.ButtonVariant
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey

private val deleteWarnings = listOf(
    "Permanently delete your profile and personal data",
    "Remove all your orders and order history",
    "Remove your saved drinks, favorites and friends",
    "Forfeit any remaining loyalty points"
)

/**
 * Delete-profile confirmation screen with warning bullet points and dual
 * Keep / Delete buttons (feature parity with the native Android
 * DeleteProfileScreen).
 */
@Composable
fun DeleteProfileScreen(
    reason: String,
    onBack: () -> Unit,
    onKeep: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Delete Profile", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                DefaultText(
                    text = "Are you sure you want to delete your account?",
                    fontSize = 16,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))

                DefaultText(
                    text = "Deleting your account will:",
                    fontSize = 14,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                deleteWarnings.forEach { warning ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DefaultText(
                            text = "•",
                            fontSize = 13,
                            fontColor = LightGrey,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        DefaultText(
                            text = warning,
                            fontSize = 13,
                            fontColor = LightGrey,
                            lineHeight = 20
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                DefaultText(
                    text = "If you would like to continue using our services in the future, consider keeping your account instead.",
                    fontSize = 14
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DefaultButton(
                    text = "Keep My Profile",
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.weight(1f),
                    onClick = onKeep
                )
                DefaultButton(
                    text = "Delete",
                    variant = ButtonVariant.DESTRUCTIVE,
                    modifier = Modifier.weight(1f),
                    onClick = onDelete
                )
            }
        }
    }
}
