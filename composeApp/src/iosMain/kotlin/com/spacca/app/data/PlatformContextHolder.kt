package com.spacca.app.data

import platform.UIKit.UIViewController

/**
 * Holds a reference to the root UIViewController so that platform-specific
 * features (image picker, share sheets, etc.) can present UIKit view controllers
 * from within Compose Multiplatform.
 */
object PlatformContextHolder {
    var rootViewController: UIViewController? = null
}
