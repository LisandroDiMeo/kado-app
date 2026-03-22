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
import com.kado.app.presentation.localization.S

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
            title = S().resetProgress,
            message = S().resetProgressMessage,
            confirmLabel = S().reset,
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
            title = { Text(S().statsGuide) },
            text = {
                Text(
                    S().statsGuideBody,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(S().gotIt)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = S().statistics,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Text("❓", style = MaterialTheme.typography.titleMedium)
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
                label = S().newLabel,
                value = uiState.newCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.height(12.dp))

            StatBar(
                label = S().learning,
                value = uiState.learningCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))

            StatBar(
                label = S().young,
                value = uiState.youngCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            StatBar(
                label = S().mature,
                value = uiState.matureCards,
                maxValue = uiState.totalCards,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(24.dp))
            Text(
                S().dueNow(uiState.dueNow),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(S().totalCards(uiState.totalCards), style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(S().resetProgress, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
