package com.kado.app.presentation.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onResult: (ByteArray?) -> Unit): () -> Unit
