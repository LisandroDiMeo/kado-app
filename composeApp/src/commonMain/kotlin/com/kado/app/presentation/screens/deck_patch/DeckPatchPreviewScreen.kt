package com.kado.app.presentation.screens.deck_patch

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.domain.model.DeckPatchPreviewItem
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.LoadingState
import com.kado.app.presentation.localization.S

@Composable
fun DeckPatchPreviewScreen(
    sessionId: String,
    deckId: Long,
    onDone: () -> Unit,
    vm: DeckPatchPreviewViewModel = viewModel { DeckPatchPreviewViewModel(sessionId, deckId) }
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(uiState.isApplied) {
        if (uiState.isApplied) onDone()
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = uiState.deckName.ifBlank { S().updateDeckTitle },
                onBack = { vm.discard() }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        uiState.error?.let { code ->
            val msg = when (code) {
                PatchPreviewError.SessionMissing -> S().patchSessionMissing
                PatchPreviewError.ApplyFailed -> uiState.errorDetail ?: S().patchParseFailed
            }
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text(msg, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDone) { Text(S().patchClose) }
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    S().patchPreviewIntro,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Added section
            item {
                SectionHeader(
                    title = S().patchAddedSection(uiState.counts.added),
                    expanded = uiState.expandedSection == DeckPatchPreviewUiState.Section.Added,
                    onClick = { vm.expandSection(DeckPatchPreviewUiState.Section.Added) },
                    enabled = uiState.counts.added > 0
                )
            }
            if (uiState.expandedSection == DeckPatchPreviewUiState.Section.Added) {
                items(uiState.added) { item ->
                    AddedCardRow(item)
                }
                if (uiState.added.size < uiState.counts.added) {
                    item {
                        Text(
                            S().patchMoreItems(uiState.counts.added - uiState.added.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Modified section
            item {
                SectionHeader(
                    title = S().patchModifiedSection(uiState.counts.modified),
                    expanded = uiState.expandedSection == DeckPatchPreviewUiState.Section.Modified,
                    onClick = { vm.expandSection(DeckPatchPreviewUiState.Section.Modified) },
                    enabled = uiState.counts.modified > 0
                )
            }
            if (uiState.expandedSection == DeckPatchPreviewUiState.Section.Modified) {
                items(uiState.modified) { item ->
                    ModifiedCardRow(item)
                }
                if (uiState.modified.size < uiState.counts.modified) {
                    item {
                        Text(
                            S().patchMoreItems(uiState.counts.modified - uiState.modified.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Removed section
            item {
                SectionHeader(
                    title = S().patchRemovedSection(uiState.counts.removed),
                    expanded = uiState.expandedSection == DeckPatchPreviewUiState.Section.Removed,
                    onClick = { vm.expandSection(DeckPatchPreviewUiState.Section.Removed) },
                    enabled = uiState.counts.removed > 0
                )
                if (uiState.counts.removed > 0 &&
                    uiState.expandedSection == DeckPatchPreviewUiState.Section.Removed
                ) {
                    Text(
                        S().patchKeepHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (uiState.expandedSection == DeckPatchPreviewUiState.Section.Removed) {
                items(uiState.removed) { item ->
                    RemovedCardRow(
                        item = item,
                        keep = item.cardId in uiState.keepRemovedIds,
                        onToggleKeep = { vm.toggleKeep(item.cardId) }
                    )
                }
                if (uiState.removed.size < uiState.counts.removed) {
                    item {
                        OutlinedButton(onClick = { vm.loadMoreRemoved() }, modifier = Modifier.fillMaxWidth()) {
                            Text(S().patchLoadMoreLeft(uiState.counts.removed - uiState.removed.size))
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                if (uiState.keepRemovedIds.isNotEmpty()) {
                    Text(
                        S().patchKeptCount(uiState.keepRemovedIds.size),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = { vm.apply() },
                    enabled = !uiState.isApplying && uiState.counts.totalChanges > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isApplying) S().patchApplying else S().patchApply)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { vm.discard() },
                    enabled = !uiState.isApplying,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(S().patchDiscard) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, expanded: Boolean, onClick: () -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (enabled) Text(if (expanded) "▾" else "▸")
    }
}

@Composable
private fun AddedCardRow(item: DeckPatchPreviewItem.Added) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                item.front,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                item.back,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModifiedCardRow(item: DeckPatchPreviewItem.Modified) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(S().patchBefore, style = MaterialTheme.typography.labelSmall)
            Text(
                "${item.oldFront} / ${item.oldBack}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(S().patchAfter, style = MaterialTheme.typography.labelSmall)
            Text(
                "${item.newFront} / ${item.newBack}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RemovedCardRow(
    item: DeckPatchPreviewItem.Removed,
    keep: Boolean,
    onToggleKeep: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleKeep),
        colors = CardDefaults.cardColors(
            containerColor = if (keep) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (keep) "☑" else "☐", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(0.dp))
            Column(Modifier.padding(start = 8.dp).fillMaxWidth()) {
                Text(
                    item.front,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.back,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (keep) S().patchWillKeep else S().patchWillDelete,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
