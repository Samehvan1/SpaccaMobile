package com.spacca.app.ui.screens.profile

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.MobileCustomer
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.clickableNoRipple
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
fun ProfileScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {},
    onChangePhone: () -> Unit = {},
    onChangePin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var customer by remember { mutableStateOf<MobileCustomer?>(null) }
    var loading by remember { mutableStateOf(true) }
    var confirmAction by remember { mutableStateOf<String?>(null) } // "deactivate" | "delete"
    var actionError by remember { mutableStateOf<String?>(null) }
    var actionBusy by remember { mutableStateOf(false) }

    scope.launch {
        loading = true
        try {
            customer = api.me()
        } catch (e: Exception) {
            // keep null
        } finally {
            loading = false
        }
    }

    val name = customer?.name ?: "SPACCA Member"
    val phone = customer?.phone ?: ""
    val tier = customer?.loyaltyTier ?: "Member"
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
        .ifBlank { "SP" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Profile", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DarkBorder),
                contentAlignment = Alignment.Center
            ) {
                DefaultText(text = initials, fontSize = 28, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            DefaultText(text = name, fontSize = 20, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(4.dp))

            // Phone
            DefaultText(text = phone, fontSize = 14, fontColor = LightGrey)

            Spacer(modifier = Modifier.height(8.dp))

            // Loyalty tier badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Green.copy(alpha = 0.2f))
                    .border(1.dp, Green, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                DefaultText(
                    text = "$tier Member",
                    fontSize = 12,
                    fontColor = Green,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBorder)
            ) {
                MenuItemRow(
                    icon = Icons.Filled.Edit,
                    label = "Edit Profile",
                    onClick = onEditProfile
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.Filled.Phone,
                    label = "Change Phone",
                    onClick = onChangePhone
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.Filled.Lock,
                    label = "Change PIN",
                    onClick = onChangePin
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    label = "Deactivate Account",
                    onClick = { confirmAction = "deactivate" }
                )
                MenuDivider()
                MenuItemRow(
                    icon = Icons.Filled.Delete,
                    label = "Delete Account",
                    labelColor = Red,
                    iconTint = Red,
                    onClick = { confirmAction = "delete" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DefaultButton(
                text = "Log Out",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Confirmation dialog for deactivate / delete
    val action = confirmAction
    if (action != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!actionBusy) confirmAction = null },
            containerColor = DarkBorder,
            title = {
                DefaultText(
                    text = if (action == "deactivate") "Deactivate Account?" else "Delete Account?",
                    fontSize = 18,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    DefaultText(
                        text = if (action == "deactivate")
                            "Your account will be temporarily deactivated. You can reactivate later."
                        else
                            "This permanently deletes your account and all data. This cannot be undone.",
                        fontSize = 14,
                        fontColor = LightGrey,
                        lineHeight = 20
                    )
                    if (actionError != null) {
                        DefaultText(
                            text = actionError ?: "",
                            fontSize = 13,
                            fontColor = Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                DefaultButton(
                    text = if (actionBusy) "Working..." else "Confirm",
                    variant = com.spacca.app.ui.components.ButtonVariant.DESTRUCTIVE,
                    onClick = {
                        scope.launch {
                            actionBusy = true
                            actionError = null
                            try {
                                if (action == "deactivate") {
                                    api.deactivate(com.spacca.app.data.model.DeactivateRequest(pin = ""))
                                } else {
                                    api.deleteAccount()
                                }
                                confirmAction = null
                                onLogout()
                            } catch (e: Exception) {
                                actionError = e.message ?: "Action failed"
                            } finally {
                                actionBusy = false
                            }
                        }
                    }
                )
            },
            dismissButton = {
                DefaultButton(
                    text = "Cancel",
                    variant = com.spacca.app.ui.components.ButtonVariant.SECONDARY,
                    onClick = { if (!actionBusy) confirmAction = null }
                )
            }
        )
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    label: String,
    labelColor: Color = White,
    iconTint: Color = LightGrey,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DefaultText(
            text = label,
            fontSize = 14,
            fontColor = labelColor,
            modifier = Modifier.weight(1f)
        )
        // chevron placeholder
        DefaultText(text = ">", fontSize = 16, fontColor = MediumGrey)
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(color = Color(0xFF4A4A4A), thickness = 0.5.dp)
}
