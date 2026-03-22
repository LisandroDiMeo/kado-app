package com.kado.app.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberImagePickerLauncher(onResult: (ByteArray?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            onResult(bytes)
        }
    }

    return { launcher.launch("image/*") }
}
