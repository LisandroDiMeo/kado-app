package com.kado.app.presentation.screens.deck_edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.domain.srs.SchedulerType
import com.kado.app.presentation.components.ConfirmDialog
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.localization.S

@Composable
private fun FieldError.toLocalizedString(): String = when (this) {
    FieldError.RetentionRange -> S().errorRetentionRange
    FieldError.InvalidNumber -> S().errorInvalidNumber
    FieldError.PositiveInteger -> S().errorPositiveInteger
    FieldError.InvalidStepsFormat -> S().errorInvalidStepsFormat
}

@Composable
fun DeckEditScreen(
    deckId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit = {},
    onAlgorithmDetail: (algorithmId: String, focusParameter: String) -> Unit = { _, _ -> },
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
            title = S().deleteDeck,
            message = S().deleteDeckEditMessage,
            confirmLabel = S().delete,
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
                title = if (uiState.isNew) S().newDeck else S().editDeck,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(S().deckName, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = vm::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(S().deckNamePlaceholder) },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Text(S().dailyNewCardLimit, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.dailyLimit,
                onValueChange = vm::onDailyLimitChange,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.dailyLimitError != null,
                supportingText = uiState.dailyLimitError?.let { error ->
                    { Text(error.toLocalizedString()) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            // Scheduler Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(S().scheduler, style = MaterialTheme.typography.titleMedium)
                val algorithmId = when (uiState.schedulerType) {
                    SchedulerType.SM2 -> "sm2"
                    SchedulerType.FSRS -> "fsrs"
                }
                Text(
                    "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onAlgorithmDetail(algorithmId, "") }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SchedulerType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.schedulerType == type,
                        onClick = { vm.onSchedulerTypeChange(type) },
                        label = {
                            Text(
                                when (type) {
                                    SchedulerType.SM2 -> S().schedulerSm2
                                    SchedulerType.FSRS -> S().schedulerFsrs
                                }
                            )
                        }
                    )
                }
            }

            // FSRS Settings
            AnimatedVisibility(
                visible = uiState.schedulerType == SchedulerType.FSRS,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))

                    // Desired Retention
                    OutlinedTextField(
                        value = uiState.desiredRetention,
                        onValueChange = vm::onDesiredRetentionChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(S().desiredRetention) },
                        isError = uiState.desiredRetentionError != null,
                        supportingText = {
                            val error = uiState.desiredRetentionError
                            if (error != null) {
                                Text(error.toLocalizedString())
                            } else {
                                Text(S().desiredRetentionHint)
                            }
                        },
                        trailingIcon = {
                            Text("?", color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onAlgorithmDetail("fsrs", "desiredRetention") }.padding(8.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    // Learning Steps
                    OutlinedTextField(
                        value = uiState.learningSteps,
                        onValueChange = vm::onLearningStepsChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(S().learningSteps) },
                        isError = uiState.learningStepsError != null,
                        supportingText = {
                            val error = uiState.learningStepsError
                            if (error != null) {
                                Text(error.toLocalizedString())
                            } else {
                                Text(S().learningStepsHint)
                            }
                        },
                        trailingIcon = {
                            Text("?", color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onAlgorithmDetail("fsrs", "learningSteps") }.padding(8.dp))
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    // Relearning Steps
                    OutlinedTextField(
                        value = uiState.relearningSteps,
                        onValueChange = vm::onRelearningStepsChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(S().relearningSteps) },
                        isError = uiState.relearningStepsError != null,
                        supportingText = {
                            val error = uiState.relearningStepsError
                            if (error != null) {
                                Text(error.toLocalizedString())
                            } else {
                                Text(S().relearningStepsHint)
                            }
                        },
                        trailingIcon = {
                            Text("?", color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onAlgorithmDetail("fsrs", "relearningSteps") }.padding(8.dp))
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    // Maximum Interval
                    OutlinedTextField(
                        value = uiState.maxInterval,
                        onValueChange = vm::onMaxIntervalChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(S().maxInterval) },
                        isError = uiState.maxIntervalError != null,
                        supportingText = {
                            val error = uiState.maxIntervalError
                            if (error != null) {
                                Text(error.toLocalizedString())
                            } else {
                                Text(S().maxIntervalHint)
                            }
                        },
                        trailingIcon = {
                            Text("?", color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onAlgorithmDetail("fsrs", "maxInterval") }.padding(8.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))

                    // Enable Fuzzing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(S().enableFuzzing, style = MaterialTheme.typography.bodyLarge)
                                Text(" ?", color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { onAlgorithmDetail("fsrs", "enableFuzzing") })
                            }
                            Text(
                                S().enableFuzzingHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.enableFuzzing,
                            onCheckedChange = { vm.onFuzzingToggle() }
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Reset to Defaults
                    OutlinedButton(
                        onClick = vm::resetFsrsDefaults,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(S().resetToDefaults)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.name.isNotBlank() && !uiState.isLoading && !uiState.hasValidationErrors
            ) {
                Text(if (uiState.isNew) S().createDeck else S().saveChanges)
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
                    Text(S().deleteDeckButton)
                }
            }
        }
    }
}
