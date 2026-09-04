package com.spacca.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.spacca.app.data.ApiService
import com.spacca.app.data.pickImage
import com.spacca.app.data.model.UpdateProfileRequest
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.DefaultTextField
import com.spacca.app.ui.components.DefaultTopBar
import com.spacca.app.ui.components.SpaccaImage
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.DarkBorder
import com.spacca.app.ui.theme.LightGrey
import com.spacca.app.ui.theme.MediumGrey
import com.spacca.app.ui.theme.Red
import com.spacca.app.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit = {}
) {
    val api = koinInject<ApiService>()
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val me = api.me()
            if (me != null) {
                name = me.name ?: ""
                email = me.email ?: ""
                birthdate = me.birthdate ?: ""
                gender = me.gender ?: ""
                city = me.city ?: ""
                address = me.address ?: ""
                avatarUrl = me.avatarUrl
            }
        } catch (e: Exception) {
            error = e.message ?: "Could not load profile"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        DefaultTopBar(title = "Edit Profile", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            if (error != null) {
                DefaultText(text = error ?: "", fontSize = 12, fontColor = Red)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Avatar section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(DarkBorder)
                        .clickable {
                            if (!uploading) {
                                scope.launch {
                                    val imageBytes = pickImage()
                                    if (imageBytes != null) {
                                        uploading = true
                                        error = null
                                        try {
                                            val updated = api.uploadAvatar(imageBytes)
                                            if (updated != null) {
                                                avatarUrl = updated.avatarUrl
                                            }
                                        } catch (e: Exception) {
                                            error = e.message ?: "Could not upload photo"
                                        } finally {
                                            uploading = false
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uploading) {
                        DefaultText(text = "Uploading...", fontSize = 10, fontColor = LightGrey)
                    } else if (avatarUrl != null) {
                        SpaccaImage(
                            imageUrl = avatarUrl,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        DefaultText(text = "Add Photo", fontSize = 12, fontColor = LightGrey)
                    }
                }
            }

            DefaultText(text = "Name", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Full name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "Email", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email address"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "Birthdate", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = birthdate,
                onValueChange = { birthdate = it },
                placeholder = "DD/MM/YYYY"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "Gender", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            GenderSelector(
                selected = gender,
                options = listOf("Male", "Female", "Other"),
                onSelect = { gender = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "City", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = city,
                onValueChange = { city = it },
                placeholder = "City"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DefaultText(text = "Address", fontSize = 12, fontColor = LightGrey)
            Spacer(modifier = Modifier.height(6.dp))
            DefaultTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = "Address"
            )

            Spacer(modifier = Modifier.weight(1f))

            DefaultButton(
                text = if (saving) "Saving..." else "Save",
                onClick = {
                    scope.launch {
                        saving = true
                        error = null
                        try {
                            api.updateProfile(
                                UpdateProfileRequest(
                                    name = name.ifBlank { null },
                                    email = email.ifBlank { null },
                                    birthdate = birthdate.ifBlank { null },
                                    gender = gender.ifBlank { null },
                                    avatarUrl = avatarUrl?.ifBlank { null },
                                    city = city.ifBlank { null },
                                    address = address.ifBlank { null }
                                )
                            )
                            onSaved()
                        } catch (e: Exception) {
                            error = e.message ?: "Could not save profile"
                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun GenderSelector(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .background(DarkBorder)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) AccentGreen else DarkBorder)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                DefaultText(
                    text = option,
                    fontSize = 12,
                    fontColor = if (isSelected) BackgroundPrimary else LightGrey
                )
            }
        }
    }
}
