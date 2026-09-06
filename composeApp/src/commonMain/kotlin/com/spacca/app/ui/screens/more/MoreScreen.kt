package com.spacca.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.data.ApiConfig
import com.spacca.app.data.BranchStore
import com.spacca.app.data.EnvironmentStore
import com.spacca.app.data.location.LocationStore
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.Grey
import org.koin.compose.koinInject

private data class MoreItem(
    val label: String,
    val icon: ImageVector
)

private val moreItems = listOf(
    MoreItem("My Profile", Icons.Filled.Person),
    MoreItem("My Orders", Icons.AutoMirrored.Filled.ReceiptLong),
    MoreItem("My Points", Icons.Filled.Star),
    MoreItem("My Favorites", Icons.Filled.Favorite),
    MoreItem("My Customized Drinks", Icons.Filled.Bookmark),
    MoreItem("Terms & Conditions", Icons.Filled.Description),
    MoreItem("Privacy Policy", Icons.Filled.PrivacyTip),
    MoreItem("API Server", Icons.Filled.Dns)
)

@Composable
fun MoreScreen(
    onProfile: () -> Unit = {},
    onOrders: () -> Unit = {},
    onPoints: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onSavedCustomizedProducts: () -> Unit = {},
    onTerms: () -> Unit = {},
    onPrivacy: () -> Unit = {}
) {
    val actions = mapOf(
        "My Profile" to onProfile,
        "My Orders" to onOrders,
        "My Points" to onPoints,
        "My Favorites" to onFavorites,
        "My Customized Drinks" to onSavedCustomizedProducts,
        "Terms & Conditions" to onTerms,
        "Privacy Policy" to onPrivacy
    )

    // Re-capture the current location whenever the More/menu tab is opened.
    val locationStore = koinInject<LocationStore>()
    val branchStore = koinInject<BranchStore>()
    val environmentStore = koinInject<EnvironmentStore>()
    LaunchedEffect(Unit) {
        branchStore.load()
        locationStore.refresh()
    }

    var showApiDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp)
    ) {
        DefaultText(
            text = "More",
            fontSize = 20,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(moreItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBorder)
                        .clickableNoRipple {
                            if (item.label == "API Server") {
                                showApiDialog = true
                            } else {
                                actions[item.label]?.invoke()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    DefaultText(
                        text = item.label,
                        fontSize = 15,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Grey,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showApiDialog) {
        ApiServerDialog(
            environmentStore = environmentStore,
            onDismiss = { showApiDialog = false }
        )
    }
}

@Composable
private fun ApiServerDialog(
    environmentStore: EnvironmentStore,
    onDismiss: () -> Unit
) {
    val current = environmentStore.currentBaseUrl()
    var useVps by remember { mutableStateOf(environmentStore.isUsingVps()) }
    var localUrl by remember { mutableStateOf(
        if (environmentStore.isUsingVps()) ApiConfig.DEFAULT_LOCAL_URL else current
    ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundPrimary,
        title = {
            DefaultText(
                text = "API Server",
                fontSize = 18,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DefaultText(
                    text = "Current: $current",
                    fontSize = 13,
                    fontColor = Grey
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = useVps,
                        onClick = { useVps = true }
                    )
                    DefaultText(
                        text = "Production (VPS)",
                        fontSize = 15,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = !useVps,
                        onClick = { useVps = false }
                    )
                    DefaultText(
                        text = "Local server",
                        fontSize = 15,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (!useVps) {
                    OutlinedTextField(
                        value = localUrl,
                        onValueChange = { localUrl = it },
                        label = { DefaultText("Local base URL", fontSize = 13) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (useVps) {
                        environmentStore.useVps()
                    } else {
                        environmentStore.useLocal(localUrl)
                    }
                    onDismiss()
                }
            ) {
                DefaultText("Save", fontSize = 15, fontColor = AccentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                DefaultText("Cancel", fontSize = 15, fontColor = Grey)
            }
        }
    )
}
