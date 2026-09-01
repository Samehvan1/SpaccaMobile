package com.spacca.app.data

/**
 * Resolves a backend image path (which may be relative, e.g. "/uploads/foo.png")
 * against the configured API base URL so it can be loaded by the image loader.
 *
 * Returns null when the value is blank (callers should fall back to a placeholder).
 */
fun resolveImageUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    val trimmed = path.trim()
    // Already an absolute URL (http/https) — use as-is.
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    // Relative path — join with the base URL, stripping any trailing slash.
    val base = ApiConfig.BASE_URL.trimEnd('/')
    return if (trimmed.startsWith("/")) "$base$trimmed" else "$base/$trimmed"
}
