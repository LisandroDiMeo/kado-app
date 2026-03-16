package com.kado.app.presentation.screens.deck_edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.presentation.components.ConfirmDialog
import com.kado.app.presentation.components.KadoTopBar

@Composable
fun DeckEditScreen(
    deckId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit = {},
    vm: DeckEditViewModel = viewModel { DeckEditViewModel(deckId) }
) {
    val uiState by vm.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Deck",
            message = "Are you sure you want to delete this deck? All cards and progress will be permanently lost.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteDialog = false
                vm.deleteDeck()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = if (uiState.isNew) "New Deck" else "Edit Deck",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Deck Name", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = vm::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Japanese N5") },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Text("Daily New Card Limit", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.dailyLimit,
                onValueChange = vm::onDailyLimitChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.name.isNotBlank() && !uiState.isLoading
            ) {
                Text(if (uiState.isNew) "Create Deck" else "Save Changes")
            }
            if (!uiState.isNew) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Deck")
                }
            }
        }
    }
}
