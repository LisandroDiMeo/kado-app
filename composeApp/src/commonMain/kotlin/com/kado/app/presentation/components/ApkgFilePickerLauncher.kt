package com.kado.app.presentation.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberApkgPickerLauncher(onResult: (ByteArray?) -> Unit): () -> Unit
