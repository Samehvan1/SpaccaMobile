package com.spacca.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.ApiService
import com.spacca.app.data.SessionStore
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginPinScreen(
    phone: String,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val api = koinInject<ApiService>()
    val session = koinInject<SessionStore>()
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Enter PIN", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            DefaultText(
                text = "Welcome back!",
                fontSize = 22,
                fontWeight = FontWeight.Bold
            )
            DefaultText(
                text = "Enter your 4-digit PIN to sign in",
                fontSize = 14,
                fontColor = LightGrey,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(32.dp))
            DefaultTextField(
                value = pin,
                onValueChange = { pin = it },
                placeholder = "Enter PIN",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
            Spacer(Modifier.height(24.dp))
            DefaultButton(
                text = if (loading) "Signing in..." else "Sign In",
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            val resp = api.login(phone, pin)
                            session.setSession(resp.customer?.id, phone, true)
                            onDone()
                        } catch (e: Exception) {
                            error = e.message ?: "Invalid PIN"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = pin.length >= 4 && !loading
            )
        }
    }
}
