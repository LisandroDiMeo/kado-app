package com.kado.app.presentation.screens.transfer

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.localization.S

@Composable
fun TransferScreen(
    deckId: Long,
    subDeckIndex: Int? = null,
    onBack: () -> Unit,
    vm: TransferViewModel = viewModel { TransferViewModel(deckId, subDeckIndex) }
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = { KadoTopBar(title = S().transfer, onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(uiState.deckName, style = MaterialTheme.typography.titleMedium)
                        Text(S().cardsCount(uiState.cardCount), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                when (uiState.transferState) {
                    is TransferState.Idle -> {
                        Button(
                            onClick = vm::upload,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.cardCount > 0
                        ) {
                            Text(S().uploadToDevice)
                        }
                    }
                    is TransferState.Uploading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Text("  ${S().uploading}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    is TransferState.Success -> {
                        Text(
                            S().uploadSuccessful,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    is TransferState.Error -> {
                        Text(
                            (uiState.transferState as TransferState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = vm::upload, modifier = Modifier.fillMaxWidth()) {
                            Text(S().retry)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(S().deviceDecks, style = MaterialTheme.typography.titleSmall)
                    Text(
                        S().freeMb(uiState.freeMb.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.isLoadingDevice) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                itemsIndexed(uiState.deviceDecks) { index, deck ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(deck.name, style = MaterialTheme.typography.bodyMedium)
                                Text(S().cardsCount(deck.cards), style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { vm.deleteDeviceDeck(index) }) {
                                Text(S().delete, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
