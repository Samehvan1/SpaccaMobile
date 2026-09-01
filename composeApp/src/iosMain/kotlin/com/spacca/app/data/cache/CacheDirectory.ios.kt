package com.spacca.app.data.cache

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSFileManager
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSUserDomainMask

actual fun cacheDirectory(): Path {
    val caches = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return caches.path.toPath()
}
