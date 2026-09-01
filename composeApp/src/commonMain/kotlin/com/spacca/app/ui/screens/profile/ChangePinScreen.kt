package com.spacca.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.ChangePinRequest
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ChangePinScreen(
    onBack: () -> Unit,
    onDone: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val pinKeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Change PIN", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            DefaultText(text = "Current PIN", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = currentPin,
                onValueChange = { currentPin = it },
                placeholder = "Enter current PIN",
                keyboardOptions = pinKeyboardOptions
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "New PIN", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = newPin,
                onValueChange = { newPin = it },
                placeholder = "Enter new PIN",
                keyboardOptions = pinKeyboardOptions
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "Confirm New PIN", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = confirmPin,
                onValueChange = { confirmPin = it },
                placeholder = "Confirm new PIN",
                keyboardOptions = pinKeyboardOptions
            )

            if (error != null) {
                DefaultText(
                    text = error ?: "",
                    fontSize = 12,
                    fontColor = Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            DefaultButton(
                text = if (loading) "Updating..." else "Update",
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            if (newPin != confirmPin) {
                                error = "New PINs do not match"
                            } else {
                                api.changePin(ChangePinRequest(currentPin, newPin))
                                onDone()
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Could not update PIN"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = currentPin.length >= 4 && newPin.length >= 4 && confirmPin.length >= 4 && !loading,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
