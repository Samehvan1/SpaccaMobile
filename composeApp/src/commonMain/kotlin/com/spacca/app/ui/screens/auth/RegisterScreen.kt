package com.spacca.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.LightGrey

/**
 * Full registration form (feature parity with the native Android app).
 * Collects profile details before proceeding to OTP / PIN setup.
 */
@Composable
fun RegisterScreen(
    phone: String,
    onBack: () -> Unit,
    onContinue: (firstName: String, lastName: String, email: String, gender: String, dateOfBirth: String, referralCode: String) -> Unit,
    onTerms: () -> Unit = {},
    onPrivacy: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Register", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            DefaultText(
                text = "Nice to meet you",
                fontSize = 20,
                fontWeight = FontWeight.SemiBold
            )
            DefaultText(
                text = "Sign up to enjoy your coffee",
                fontSize = 12,
                fontColor = LightGrey,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            DefaultTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = "First Name",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            DefaultTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = "Last Name",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            DefaultTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Male", "Female").forEach { option ->
                    val selected = gender == option
                    DefaultText(
                        text = option,
                        fontSize = 14,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) AccentGreen.copy(alpha = 0.2f) else DarkBorder)
                            .clickableNoRipple { gender = option }
                            .padding(vertical = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DefaultTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                placeholder = "Date of Birth",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            DefaultTextField(
                value = referralCode,
                onValueChange = { referralCode = it },
                placeholder = "Referral Code (optional)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            DefaultButton(
                text = "Sign Up",
                enabled = firstName.length >= 3,
                onClick = {
                    onContinue(firstName, lastName, email, gender, dateOfBirth, referralCode)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            DefaultText(
                text = "By creating a new account you agree to",
                fontSize = 12,
                fontColor = LightGrey,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DefaultText(
                    text = "Terms of Service",
                    fontSize = 11,
                    fontColor = LightGrey,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickableNoRipple(onClick = onTerms)
                )
                DefaultText(text = "and", fontSize = 12, fontColor = LightGrey)
                DefaultText(
                    text = "Privacy Policy",
                    fontSize = 12,
                    fontColor = LightGrey,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickableNoRipple(onClick = onPrivacy)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
