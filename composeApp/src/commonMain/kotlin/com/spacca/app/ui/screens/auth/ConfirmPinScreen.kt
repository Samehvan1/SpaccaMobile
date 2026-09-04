package com.spacca.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red

/**
 * Separate screen to confirm the PIN the user just created (feature parity
 * with the native Android ConfirmPinCodeScreen). The KMP app previously
 * combined create + confirm into a single PinScreen.
 */
@Composable
fun ConfirmPinScreen(
    phone: String,
    pin: String,
    onBack: () -> Unit,
    onConfirmed: () -> Unit
) {
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Confirm PIN", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            DefaultText(
                text = "Re-enter your PIN code",
                fontSize = 20,
                fontWeight = FontWeight.Bold
            )
            DefaultText(
                text = "Enter your PIN code again to continue",
                fontSize = 11,
                fontColor = LightGrey,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            DefaultTextField(
                value = confirmPin,
                onValueChange = { confirmPin = it },
                placeholder = "Confirm PIN",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                DefaultText(
                    text = error ?: "",
                    fontSize = 12,
                    fontColor = Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DefaultButton(
                text = "Confirm",
                enabled = confirmPin.length >= 4,
                onClick = {
                    if (confirmPin == pin) {
                        onConfirmed()
                    } else {
                        error = "PINs do not match"
                    }
                }
            )
        }
    }
}
