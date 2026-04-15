package com.kado.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.kado.app.presentation.localization.S

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
                    isDone -> S().importComplete
                    isError -> S().importFailed
                    else -> S().importing
                }
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when {
                    isError -> {
                        Text(
                            progress.error ?: S().unknownError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    isDone -> {
                        Text(S().importedCards(progress.cardCount, progress.deckName))
                    }
                    else -> {
                        Text(
                            when (progress.phase) {
                                ImportPhase.Extracting -> S().extracting
                                ImportPhase.Parsing -> S().readingAnkiDb
                                ImportPhase.Inserting -> S().importingCards(progress.cardCount)
                                else -> S().processing
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
                    Text(if (isDone) S().done else S().dismiss)
                }
            }
        }
    )
}
