package com.spacca.app.ui.screens.profile

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.ChangePhoneRequest
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
fun ChangePhoneScreen(
    onBack: () -> Unit,
    onDone: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var newPhone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Change Phone", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            DefaultText(text = "New Phone Number", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = newPhone,
                onValueChange = { newPhone = it },
                placeholder = "+20 1XX XXX XXXX"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "OTP Code", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = otp,
                onValueChange = { otp = it },
                placeholder = "Enter OTP"
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
                            api.changePhone(ChangePhoneRequest(newPhone, otp))
                            onDone()
                        } catch (e: Exception) {
                            error = e.message ?: "Could not update phone"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = newPhone.isNotBlank() && otp.isNotBlank() && !loading,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
