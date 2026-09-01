package com.spacca.app.data.cache

import okio.Path

/** Returns the platform-specific directory used to persist cache files. */
expect fun cacheDirectory(): Path
