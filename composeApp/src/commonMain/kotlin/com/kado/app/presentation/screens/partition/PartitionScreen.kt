package com.kado.app.presentation.screens.partition

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.NumberSelector

@Composable
fun PartitionScreen(
    deckId: Long,
    onBack: () -> Unit,
    vm: PartitionViewModel = viewModel { PartitionViewModel(deckId) }
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(uiState.isDone) {
        if (uiState.isDone) onBack()
    }

    Scaffold(
        topBar = { KadoTopBar(title = "Partition Deck", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mode toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.mode == PartitionMode.Batch,
                        onClick = { vm.setMode(PartitionMode.Batch) },
                        label = { Text("Batch") }
                    )
                    FilterChip(
                        selected = uiState.mode == PartitionMode.Manual,
                        onClick = { vm.setMode(PartitionMode.Manual) },
                        label = { Text("Manual") }
                    )
                }
            }

            if (uiState.hasExistingPartitions) {
                item {
                    Text(
                        "Existing partitions will be replaced.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when (uiState.mode) {
                PartitionMode.Batch -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Cards per sub-deck", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            NumberSelector(
                                value = uiState.batchSize,
                                onValueChange = vm::setBatchSize,
                                range = 1..maxOf(1, uiState.cards.size)
                            )
                            Spacer(Modifier.height(12.dp))
                            val count = vm.subDeckCount
                            val last = vm.lastSubDeckSize
                            if (count > 0) {
                                Text(
                                    "$count sub-decks of ${uiState.batchSize} cards" +
                                            if (last != uiState.batchSize) " (last has $last)" else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = vm::applyBatchPartition,
                                enabled = !uiState.isSaving && uiState.cards.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (uiState.isSaving) "Partitioning..." else "Partition")
                            }
                        }
                    }
                }
                PartitionMode.Manual -> {
                    items(uiState.cards, key = { it.id }) { card ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = card.front,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                NumberSelector(
                                    value = uiState.assignments[card.id] ?: 0,
                                    onValueChange = { vm.setCardAssignment(card.id, it) },
                                    range = 0..maxOf(0, uiState.cards.size / 2)
                                )
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = vm::applyManualPartition,
                            enabled = !uiState.isSaving && uiState.assignments.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (uiState.isSaving) "Saving..." else "Save")
                        }
                    }
                }
            }
        }
    }
}
