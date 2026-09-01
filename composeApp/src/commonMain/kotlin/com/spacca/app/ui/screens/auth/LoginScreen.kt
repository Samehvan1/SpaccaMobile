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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.ApiService
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.LightGrey
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onOtpSent: (String) -> Unit
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Sign in", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            DefaultText(
                text = "Welcome to SPACCA",
                fontSize = 22,
                fontWeight = FontWeight.Bold
            )
            DefaultText(
                text = "Please sign in with your phone number",
                fontSize = 14,
                fontColor = LightGrey,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(32.dp))
            DefaultTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Phone number",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                DefaultText(
                    text = error ?: "",
                    fontSize = 12,
                    fontColor = com.spacca.app.ui.theme.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            DefaultButton(
                text = if (loading) "Sending..." else "Continue",
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            api.requestOtp(phone)
                            onOtpSent(phone)
                        } catch (e: Exception) {
                            error = e.message ?: "Something went wrong"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = phone.isNotBlank() && !loading
            )
        }
    }
}
