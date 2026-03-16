package com.kado.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kado.app.data.importer.ImportPhase
import com.kado.app.data.importer.ImportProgress

@Composable
fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit
) {
    val isDone = progress.phase == ImportPhase.Done
    val isError = progress.phase == ImportPhase.Error

    AlertDialog(
        onDismissRequest = {
            if (isDone || isError) onDismiss()
        },
        title = {
            Text(
                when {
                    isDone -> "Import Complete"
                    isError -> "Import Failed"
                    else -> "Importing..."
                }
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when {
                    isError -> {
                        Text(
                            progress.error ?: "An unknown error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    isDone -> {
                        Text("Imported ${progress.cardCount} cards into \"${progress.deckName}\"")
                    }
                    else -> {
                        Text(
                            when (progress.phase) {
                                ImportPhase.Extracting -> "Extracting APKG file..."
                                ImportPhase.Parsing -> "Reading Anki database..."
                                ImportPhase.Inserting -> "Importing ${progress.cardCount} cards..."
                                else -> "Processing..."
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progress.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (progress.deckName.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                progress.deckName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isDone || isError) {
                TextButton(onClick = onDismiss) {
                    Text(if (isDone) "Done" else "Dismiss")
                }
            }
        }
    )
}
