package com.kado.app.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.kado.app.presentation.localization.S

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val resolvedConfirmLabel = confirmLabel.ifEmpty { S().confirm }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(resolvedConfirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(S().cancel)
            }
        }
    )
}
