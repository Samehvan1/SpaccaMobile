package com.spacca.app.data

/** Opens the platform image picker and returns the selected image bytes, or null if cancelled. */
expect suspend fun pickImage(): ByteArray?
