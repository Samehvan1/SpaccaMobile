package com.spacca.app.data.cache

import android.content.Context
import okio.Path
import okio.Path.Companion.toPath

// Holds the application context so common code can resolve platform paths.
// Initialized once from MainActivity.onCreate.
object AndroidContextHolder {
    @Volatile
    var context: Context? = null

    // Set by MainActivity after the runtime location permission request completes.
    @Volatile
    var locationPermissionGranted: Boolean = false
}

actual fun cacheDirectory(): Path {
    val ctx = AndroidContextHolder.context
        ?: throw IllegalStateException("AndroidContextHolder.context not initialized")
    return ctx.cacheDir.absolutePath.toPath()
}
