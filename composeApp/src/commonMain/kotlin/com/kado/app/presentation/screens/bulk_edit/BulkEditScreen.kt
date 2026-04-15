package com.kado.app.presentation.screens.bulk_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.domain.usecase.CardChange
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.localization.S

@Composable
fun BulkEditScreen(
    deckId: Long,
    onBack: () -> Unit,
    vm: BulkEditViewModel = viewModel { BulkEditViewModel(deckId) }
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onBack()
    }

    when (uiState.phase) {
        BulkEditPhase.Editing -> EditingPhase(uiState, vm, onBack)
        BulkEditPhase.Preview -> PreviewPhase(uiState, vm)
    }
}

@Composable
private fun EditingPhase(
    uiState: BulkEditUiState,
    vm: BulkEditViewModel,
    onBack: () -> Unit
) {
    if (uiState.showSaveRuleDialog) {
        SaveRuleDialog(
            onSave = vm::saveRule,
            onDismiss = vm::dismissSaveRuleDialog
        )
    }

    if (uiState.showSavedRulesDialog) {
        SavedRulesDialog(
            rules = uiState.savedRules,
            onLoad = vm::loadRule,
            onDelete = vm::deleteRule,
            onDismiss = vm::dismissSavedRulesDialog
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = S().bulkEdit,
                onBack = onBack,
                actions = {
                    IconButton(onClick = vm::showSavedRulesDialog) {
                        Text("\u2699\uFE0F", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Front Rule
            item {
                RuleSection(
                    title = S().cardFrontRule,
                    find = uiState.frontFind,
                    replace = uiState.frontReplace,
                    isRegex = uiState.frontIsRegex,
                    isEnabled = uiState.frontEnabled,
                    onFindChange = vm::onFrontFindChange,
                    onReplaceChange = vm::onFrontReplaceChange,
                    onIsRegexChange = vm::onFrontIsRegexChange,
                    onEnabledChange = vm::onFrontEnabledChange
                )
            }

            // Card Back Rule
            item {
                RuleSection(
                    title = S().cardBackRule,
                    find = uiState.backFind,
                    replace = uiState.backReplace,
                    isRegex = uiState.backIsRegex,
                    isEnabled = uiState.backEnabled,
                    onFindChange = vm::onBackFindChange,
                    onReplaceChange = vm::onBackReplaceChange,
                    onIsRegexChange = vm::onBackIsRegexChange,
                    onEnabledChange = vm::onBackEnabledChange
                )
            }

            // Error messages
            if (uiState.regexError != null) {
                item {
                    Text(
                        "${S().invalidRegex}: ${uiState.regexError}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (uiState.noChangesFound) {
                item {
                    Text(
                        S().noChangesFound,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Buttons
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = vm::previewChanges,
                    enabled = !uiState.isProcessing && hasInput(uiState),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(S().previewChanges)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = vm::showSaveRuleDialog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(S().saveRule)
                }
            }
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    find: String,
    replace: String,
    isRegex: Boolean,
    isEnabled: Boolean,
    onFindChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onIsRegexChange: (Boolean) -> Unit,
    onEnabledChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Switch(checked = isEnabled, onCheckedChange = onEnabledChange)
            }
            if (isEnabled) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = find,
                    onValueChange = onFindChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(S().findPattern) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = replace,
                    onValueChange = onReplaceChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(S().replacePattern) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRegex, onCheckedChange = onIsRegexChange)
                    Text(S().isRegex, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun PreviewPhase(
    uiState: BulkEditUiState,
    vm: BulkEditViewModel
) {
    Scaffold(
        topBar = {
            KadoTopBar(
                title = S().previewChanges,
                onBack = vm::backToEditing
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Header with count and select/deselect
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    S().cardsAffected(uiState.changes.size),
                    style = MaterialTheme.typography.titleSmall
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(onClick = vm::selectAll) { Text(S().selectAll) }
                    TextButton(onClick = vm::deselectAll) { Text(S().deselectAll) }
                }
            }

            // Card list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.changes, key = { it.card.id }) { change ->
                    CardChangeItem(
                        change = change,
                        onToggle = { vm.toggleCardSelection(change.card.id) }
                    )
                }
            }

            // Commit button
            Button(
                onClick = vm::commitChanges,
                enabled = !uiState.isProcessing && uiState.changes.any { it.isSelected },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).width(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(S().commitChanges)
                }
            }
        }
    }
}

@Composable
private fun CardChangeItem(
    change: CardChange,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Front change
                if (change.card.front.rawText != change.newFront) {
                    Text(
                        "${S().front}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        change.card.front.rawText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        change.newFront,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Back change
                if (change.card.back.rawText != change.newBack) {
                    if (change.card.front.rawText != change.newFront) {
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        "${S().backSide}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        change.card.back.rawText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        change.newBack,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Checkbox(
                checked = change.isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun SaveRuleDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(S().saveRule) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(S().ruleName) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text(S().save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(S().cancel)
            }
        }
    )
}

@Composable
private fun SavedRulesDialog(
    rules: List<com.kado.app.domain.model.BulkEditRule>,
    onLoad: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(S().savedRules) },
        text = {
            if (rules.isEmpty()) {
                Text(S().noSavedRules, style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(rules, key = { it.id }) { rule ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onLoad(rule.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                rule.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onDelete(rule.id) }) {
                                Text(S().delete, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(S().cancel)
            }
        }
    )
}

private fun hasInput(uiState: BulkEditUiState): Boolean = (uiState.frontEnabled && uiState.frontFind.isNotEmpty()) ||
    (uiState.backEnabled && uiState.backFind.isNotEmpty())
