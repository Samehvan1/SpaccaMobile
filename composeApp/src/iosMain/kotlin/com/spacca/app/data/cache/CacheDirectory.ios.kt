package com.spacca.app.data.cache

import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSFileManager
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun cacheDirectory(): Path {
    val caches = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    ) ?: error("Unable to locate caches directory")
    return (caches.path ?: error("Caches directory has no path")).toPath()
}
