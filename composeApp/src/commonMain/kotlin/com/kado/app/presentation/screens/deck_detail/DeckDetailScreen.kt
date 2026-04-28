package com.kado.app.presentation.screens.deck_detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.kado.app.presentation.components.ConfirmDialog
import com.kado.app.presentation.components.EmptyState
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.LoadingState
import com.kado.app.presentation.components.PlatformBackHandler
import com.kado.app.presentation.components.rememberApkgPickerLauncher
import com.kado.app.presentation.localization.S

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
    onBack: () -> Unit,
    onEditDeck: () -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (Long) -> Unit,
    onReview: () -> Unit,
    onStats: () -> Unit,
    onTransfer: () -> Unit,
    onPartition: () -> Unit = {},
    onBulkEdit: () -> Unit = {},
    onPatchReady: (sessionId: String) -> Unit = {},
    onReviewSubDeck: (Int) -> Unit = {},
    onTransferSubDeck: (Int) -> Unit = {},
    vm: DeckDetailViewModel = viewModel { DeckDetailViewModel(deckId) }
) {
    val uiState by vm.uiState.collectAsState()
    val pagedCards = vm.pagedCards.collectAsLazyPagingItems()

    LifecycleResumeEffect(Unit) {
        vm.refresh()
        onPauseOrDispose {}
    }

    var removeSubDeckConfirm by remember { mutableStateOf<Int?>(null) }
    var cloneSubDeckConfirm by remember { mutableStateOf<Int?>(null) }
    var showReversedDeckConfirm by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteSelectedConfirm by remember { mutableStateOf(false) }

    PlatformBackHandler(enabled = uiState.selectionMode) { vm.clearSelection() }

    val updatePicker = rememberApkgPickerLauncher { bytes ->
        if (bytes != null) vm.startUpdate(bytes)
    }
    val backfillPicker = rememberApkgPickerLauncher { bytes ->
        if (bytes != null) vm.runBackfill(bytes)
    }

    when (val phase = uiState.patchPhase) {
        PatchPhase.ParsingApkg -> PatchSpinnerDialog(text = S().patchParsing)
        PatchPhase.RunningBackfill -> PatchSpinnerDialog(text = S().patchBackfilling)
        is PatchPhase.NeedsBackfill -> ConfirmDialog(
            title = S().updateFromApkg,
            message = S().patchNeedsBackfillMessage(phase.nullGuidCount),
            confirmLabel = S().patchReimportOriginal,
            onConfirm = {
                vm.dismissPatchPhase()
                backfillPicker()
            },
            onDismiss = { vm.dismissPatchPhase() }
        )
        is PatchPhase.BackfillDone -> ConfirmDialog(
            title = S().updateFromApkg,
            message = S().patchBackfillResult(phase.result.matched, phase.result.unmatched),
            confirmLabel = if (phase.result.unmatched == 0) S().patchContinue else S().confirm,
            onConfirm = {
                vm.dismissPatchPhase()
                if (phase.result.unmatched == 0) updatePicker()
            },
            onDismiss = { vm.dismissPatchPhase() }
        )
        is PatchPhase.NoChanges -> ConfirmDialog(
            title = S().patchNoChangesTitle,
            message = S().patchNoChangesMessage,
            confirmLabel = S().patchContinue,
            onConfirm = { vm.dismissPatchPhase() },
            onDismiss = { vm.dismissPatchPhase() }
        )
        is PatchPhase.Error -> {
            val msg = when (phase.code) {
                PatchErrorCode.InvalidApkg -> S().patchInvalidApkg
                PatchErrorCode.ReadDbFailed -> S().patchReadDbFailed
                PatchErrorCode.NoCards -> S().patchNoCards
                PatchErrorCode.ParseFailed -> phase.cause ?: S().patchParseFailed
            }
            ConfirmDialog(
                title = S().updateFromApkg,
                message = msg,
                confirmLabel = S().confirm,
                onConfirm = { vm.dismissPatchPhase() },
                onDismiss = { vm.dismissPatchPhase() }
            )
        }
        else -> Unit
    }

    LaunchedEffect(uiState.patchPhase) {
        val phase = uiState.patchPhase
        if (phase is PatchPhase.PatchReady) {
            onPatchReady(phase.handle.sessionId)
            vm.dismissPatchPhase()
        }
    }

    if (showDeleteSelectedConfirm) {
        val selectedCount = uiState.selectedCardIds.size
        ConfirmDialog(
            title = S().deleteSelected,
            message = S().deleteSelectedMessage(selectedCount),
            confirmLabel = S().delete,
            onConfirm = {
                vm.deleteSelected()
                showDeleteSelectedConfirm = false
            },
            onDismiss = { showDeleteSelectedConfirm = false }
        )
    }

    removeSubDeckConfirm?.let { index ->
        ConfirmDialog(
            title = S().removeSubDeck,
            message = S().removeSubDeckMessage,
            confirmLabel = S().remove,
            onConfirm = {
                vm.removeSubDeck(index)
                removeSubDeckConfirm = null
            },
            onDismiss = { removeSubDeckConfirm = null }
        )
    }

    cloneSubDeckConfirm?.let { index ->
        ConfirmDialog(
            title = S().cloneSubDeck,
            message = S().cloneSubDeckMessage,
            confirmLabel = S().clone,
            onConfirm = {
                vm.cloneSubDeck(index)
                cloneSubDeckConfirm = null
            },
            onDismiss = { cloneSubDeckConfirm = null }
        )
    }

    if (showReversedDeckConfirm) {
        ConfirmDialog(
            title = S().createReversedDeck,
            message = S().createReversedDeckMessage,
            confirmLabel = S().confirm,
            onConfirm = {
                vm.createReversedDeck()
                showReversedDeckConfirm = false
            },
            onDismiss = { showReversedDeckConfirm = false }
        )
    }

    Scaffold(
        topBar = {
            if (uiState.selectionMode) {
                KadoTopBar(
                    title = S().selectedCount(uiState.selectedCardIds.size),
                    onBack = { vm.clearSelection() },
                    actions = {
                        IconButton(
                            onClick = { showDeleteSelectedConfirm = true },
                            enabled = uiState.selectedCardIds.isNotEmpty()
                        ) {
                            Text("🗑", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            } else {
                KadoTopBar(
                    title = uiState.deck?.name ?: "Deck",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = onEditDeck) {
                            Text(S().edit, style = MaterialTheme.typography.labelSmall)
                        }
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Text("⚙️", style = MaterialTheme.typography.labelSmall)
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(S().selectCards) },
                                    onClick = {
                                        showMoreMenu = false
                                        vm.enterSelectionMode()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(S().updateFromApkg) },
                                    onClick = {
                                        showMoreMenu = false
                                        updatePicker()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(S().createReversedDeck) },
                                    onClick = {
                                        showMoreMenu = false
                                        showReversedDeckConfirm = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(S().partitionDeck) },
                                    onClick = {
                                        showMoreMenu = false
                                        onPartition()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(S().bulkEdit) },
                                    onClick = {
                                        showMoreMenu = false
                                        onBulkEdit()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(S().transferToDevice) },
                                    onClick = {
                                        showMoreMenu = false
                                        onTransfer()
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.selectionMode) {
                FloatingActionButton(
                    onClick = onAddCard,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "+",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReview,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        enabled = uiState.dueCount > 0 || uiState.newCount > 0
                    ) {
                        Text(
                            text = S().reviewCount(uiState.dueCount),
                            textAlign = TextAlign.Center
                        )
                    }
                    OutlinedButton(
                        onClick = onStats,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Text(S().stats)
                    }
                }
            }

            // Sub-decks section
            if (uiState.hasPartitions) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        S().subDecks,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(uiState.subDecks.size) { displayIndex ->
                    val subDeck = uiState.subDecks[displayIndex]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    S().partLabel(displayIndex),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    S().cardStats(subDeck.cardCount, subDeck.dueCount, subDeck.newCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onReviewSubDeck(subDeck.index) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = subDeck.dueCount > 0 || subDeck.newCount > 0
                            ) {
                                Text(S().review)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                                    onTransferSubDeck(subDeck.index)
                                }) {
                                    Text(S().send, style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                                    cloneSubDeckConfirm =
                                        subDeck.index
                                }) {
                                    Text(S().clone, style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                                    removeSubDeckConfirm =
                                        subDeck.index
                                }) {
                                    Text(S().remove, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    S().cardsCount(uiState.cardCount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.cardCount == 0) {
                item {
                    EmptyState(
                        title = S().noCardsTitle,
                        subtitle = S().noCardsSubtitle
                    )
                }
            } else {
                items(
                    count = pagedCards.itemCount,
                    key = pagedCards.itemKey { it.id }
                ) { index ->
                    val card = pagedCards[index] ?: return@items
                    val isSelected = card.id in uiState.selectedCardIds
                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {
                                    if (uiState.selectionMode) {
                                        vm.toggleSelection(card.id)
                                    } else {
                                        onEditCard(card.id)
                                    }
                                },
                                onLongClick = {
                                    if (uiState.selectionMode) {
                                        vm.toggleSelection(card.id)
                                    } else {
                                        vm.enterSelectionWith(card.id)
                                    }
                                }
                            ),
                        colors = CardDefaults.cardColors(containerColor = containerColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.selectionMode) {
                                Text(
                                    text = if (isSelected) "☑" else "☐",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = card.front.rawText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = card.back.rawText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatchSpinnerDialog(text: String) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = null,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Spacer(Modifier.width(16.dp))
                Text(text)
            }
        }
    )
}
