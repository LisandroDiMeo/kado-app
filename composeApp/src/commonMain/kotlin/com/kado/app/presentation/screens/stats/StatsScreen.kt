package com.kado.app.presentation.screens.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.kado.app.presentation.components.LoadingState
import com.kado.app.presentation.components.StatBar

@Composable
fun StatsScreen(
    deckId: Long,
    onBack: () -> Unit,
    vm: StatsViewModel = viewModel { StatsViewModel(deckId) }
) {
    val uiState by vm.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ConfirmDialog(
            title = "Reset Progress",
            message = "All review progress for this deck will be erased. Cards will be treated as new.",
            confirmLabel = "Reset",
            onConfirm = {
                showResetDialog = false
                vm.resetProgress()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Stats Guide") },
            text = {
                Text(
                    "New — Cards you haven't studied yet.\n\n" +
                    "Learning — Cards you got wrong and are re-learning.\n\n" +
                    "Young — Cards you've reviewed, but with an interval under 21 days. " +
                    "After rating a card \"Good\" for the first time, it moves here with a 1-day interval.\n\n" +
                    "Mature — Cards with an interval of 21+ days. These are well-known.\n\n" +
                    "Due Now — Total cards ready for review right now (new + overdue).",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = "Statistics",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Text("?", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                uiState.deck?.name ?: "",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(24.dp))

            StatBar(
                label = "New",
                value = uiState.newCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.height(12.dp))

            StatBar(
                label = "Learning",
                value = uiState.learningCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))

            StatBar(
                label = "Young",
                value = uiState.youngCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            StatBar(
                label = "Mature",
                value = uiState.matureCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "Due Now: ${uiState.dueNow}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text("Total: ${uiState.totalCards} cards", style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Progress", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
