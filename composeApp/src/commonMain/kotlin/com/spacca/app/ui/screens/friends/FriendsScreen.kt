package com.spacca.app.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacca.app.data.ApiService
import com.spacca.app.data.model.Friend
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultEmptyState
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.Red
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun FriendsScreen(
    onBack: () -> Unit
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var phoneInput by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                friends = api.friends()
            } catch (e: Exception) {
                error = e.message ?: "Could not load friends"
            } finally {
                loading = false
            }
        }
    }

    load()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Friends", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Add friend section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DefaultTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    placeholder = "Friend's phone number",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DefaultButton(
                    text = "Add",
                    onClick = {
                        if (phoneInput.isNotBlank()) {
                            scope.launch {
                                try {
                                    api.addFriend(phoneInput)
                                    friends = api.friends()
                                    phoneInput = ""
                                } catch (e: Exception) {
                                    error = e.message ?: "Could not add friend"
                                }
                            }
                        }
                    },
                    modifier = Modifier.width(70.dp)
                )
            }

            if (error != null) {
                DefaultText(
                    text = error ?: "",
                    fontSize = 12,
                    fontColor = Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
                com.spacca.app.ui.components.DefaultButton(
                    text = "Retry",
                    onClick = { load() },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        DefaultText(text = "Loading...", fontSize = 14, fontColor = LightGrey)
                    }
                }
                friends.isEmpty() -> {
                    DefaultEmptyState(
                        title = "No friends yet",
                        body = "Add friends by their phone number",
                        icon = Icons.Filled.PersonAdd
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(friends) { friend ->
                            FriendCard(
                                name = friend.friendName ?: "Friend",
                                phone = friend.friendPhone ?: "",
                                onRemove = {
                                    scope.launch {
                                        try {
                                            api.removeFriend(friend.id)
                                            friends = friends.filterNot { it.id == friend.id }
                                        } catch (e: Exception) {
                                            // ignore
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendCard(
    name: String,
    phone: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkBorder)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            DefaultText(
                text = name,
                fontSize = 14,
                fontWeight = FontWeight.Medium
            )
            DefaultText(
                text = phone,
                fontSize = 12,
                fontColor = LightGrey,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        DefaultText(
            text = "Remove",
            fontSize = 12,
            fontColor = Red,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickableNoRipple(onClick = onRemove)
        )
    }
}
