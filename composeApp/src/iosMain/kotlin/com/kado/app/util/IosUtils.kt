package com.kado.app.util

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene

internal fun topViewController(): UIViewController? {
    val windowScene = UIApplication.sharedApplication.connectedScenes
        .firstOrNull { it is UIWindowScene } as? UIWindowScene
    return windowScene?.keyWindow?.rootViewController
}
