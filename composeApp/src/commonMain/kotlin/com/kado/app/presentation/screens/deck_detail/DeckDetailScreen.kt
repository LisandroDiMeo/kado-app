package com.kado.app.presentation.screens.deck_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.presentation.components.ConfirmDialog
import com.kado.app.presentation.components.EmptyState
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.LoadingState

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
    onReviewSubDeck: (Int) -> Unit = {},
    onTransferSubDeck: (Int) -> Unit = {},
    vm: DeckDetailViewModel = viewModel { DeckDetailViewModel(deckId) }
) {
    val uiState by vm.uiState.collectAsState()

    LifecycleResumeEffect(Unit) {
        vm.refresh()
        onPauseOrDispose {}
    }

    var removeSubDeckConfirm by remember { mutableStateOf<Int?>(null) }
    var cloneSubDeckConfirm by remember { mutableStateOf<Int?>(null) }

    removeSubDeckConfirm?.let { index ->
        ConfirmDialog(
            title = "Remove Sub-deck",
            message = "Remove this sub-deck? Cards will become unassigned.",
            confirmLabel = "Remove",
            onConfirm = { vm.removeSubDeck(index); removeSubDeckConfirm = null },
            onDismiss = { removeSubDeckConfirm = null }
        )
    }

    cloneSubDeckConfirm?.let { index ->
        ConfirmDialog(
            title = "Clone Sub-deck",
            message = "Create a new independent deck from this sub-deck?",
            confirmLabel = "Clone",
            onConfirm = { vm.cloneSubDeck(index); cloneSubDeckConfirm = null },
            onDismiss = { cloneSubDeckConfirm = null }
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = uiState.deck?.name ?: "Deck",
                onBack = onBack,
                actions = {
                    androidx.compose.material3.IconButton(onClick = onEditDeck) {
                        Text("Edit", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReview,
                        modifier = Modifier.weight(1f),
                        enabled = uiState.dueCount > 0 || uiState.newCount > 0
                    ) {
                        Text("Review (${uiState.dueCount})")
                    }
                    OutlinedButton(
                        onClick = onStats,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stats")
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onTransfer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Transfer to Device")
                }
            }

            item {
                OutlinedButton(
                    onClick = onPartition,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Partition Deck")
                }
            }

            // Sub-decks section
            if (uiState.hasPartitions) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sub-decks",
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
                                    "Part ${displayIndex + 1}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "${subDeck.cardCount} cards | ${subDeck.dueCount} due | ${subDeck.newCount} new",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onReviewSubDeck(subDeck.index) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = subDeck.dueCount > 0 || subDeck.newCount > 0
                            ) {
                                Text("Review")
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(modifier = Modifier.weight(1f), onClick = { onTransferSubDeck(subDeck.index) }) {
                                    Text("Send")
                                }
                                OutlinedButton(modifier = Modifier.weight(1f), onClick = { cloneSubDeckConfirm = subDeck.index }) {
                                    Text("Clone")
                                }
                                OutlinedButton(modifier = Modifier.weight(1f), onClick = { removeSubDeckConfirm = subDeck.index }) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${uiState.cards.size} cards",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.cards.isEmpty()) {
                item {
                    EmptyState(
                        title = "No cards yet",
                        subtitle = "Tap + to add your first card"
                    )
                }
            } else {
                items(uiState.cards, key = { it.id }) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEditCard(card.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = card.front,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = card.back,
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
