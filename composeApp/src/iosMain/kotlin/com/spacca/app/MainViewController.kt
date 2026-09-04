package com.spacca.app

import androidx.compose.ui.window.ComposeUIViewController
import com.spacca.app.data.PlatformContextHolder
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val vc = ComposeUIViewController { App() }
    PlatformContextHolder.rootViewController = vc
    return vc
}
