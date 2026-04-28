package com.kado.app.presentation.components

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS does not have a system "back" gesture analogous to Android's. The contextual top bar's
    // back button still calls clearSelection, so this is a no-op on iOS.
}
