package com.kado.app.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.data.importer.ImportPhase
import com.kado.app.data.importer.ImportProgress
import com.kado.app.di.AppDependencies
import com.kado.app.presentation.components.ConfirmDialog
import com.kado.app.presentation.components.DeckCard
import com.kado.app.presentation.components.EmptyState
import com.kado.app.presentation.components.ExpandableFab
import com.kado.app.presentation.components.ExpandableFabScrim
import com.kado.app.presentation.components.ImportProgressDialog
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.LoadingState
import com.kado.app.presentation.components.rememberApkgPickerLauncher
import com.kado.app.presentation.localization.S
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onDeckClick: (Long) -> Unit,
    onCreateDeck: () -> Unit,
    onConnectionClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    vm: HomeViewModel = viewModel { HomeViewModel() }
) {
    val uiState by vm.uiState.collectAsState()
    var deckToDelete by remember { mutableStateOf<Long?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }
    var importProgress by remember { mutableStateOf<ImportProgress?>(null) }

    val scope = rememberCoroutineScope()
    val importer = remember { AppDependencies.apkgImporter }

    val launchPicker = rememberApkgPickerLauncher { bytes ->
        if (bytes != null) {
            importProgress = ImportProgress(ImportPhase.Extracting, 0f)
            scope.launch {
                try {
                    importer.import(bytes) { progress ->
                        importProgress = progress
                    }
                } catch (_: Exception) {
                    // Error already reported via onProgress callback
                }
            }
        }
    }

    deckToDelete?.let { id ->
        ConfirmDialog(
            title = S().deleteDeck,
            message = S().deleteDeckMessage,
            confirmLabel = S().delete,
            onConfirm = {
                vm.deleteDeck(id)
                deckToDelete = null
            },
            onDismiss = { deckToDelete = null }
        )
    }

    importProgress?.let { progress ->
        ImportProgressDialog(
            progress = progress,
            onDismiss = { importProgress = null }
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = S().appName,
                actions = {
                    IconButton(onClick = onHelpClick) {
                        Text("❓", style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onConnectionClick) {
                        Text("🛜", style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Text("⚙️", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        },
        floatingActionButton = {
            ExpandableFab(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                onCreateDeck = {
                    fabExpanded = false
                    onCreateDeck()
                },
                onImportApkg = {
                    fabExpanded = false
                    launchPicker()
                }
            )
        }
    ) { padding ->
        Box {
            when {
                uiState.isLoading -> LoadingState(Modifier.padding(padding))
                uiState.decks.isEmpty() -> EmptyState(
                    title = S().noDecksTitle,
                    subtitle = S().noDecksSubtitle,
                    modifier = Modifier.padding(padding)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.decks, key = { it.deck.id }) { summary ->
                        DeckCard(
                            summary = summary,
                            onClick = { onDeckClick(summary.deck.id) }
                        )
                    }
                }
            }

            ExpandableFabScrim(
                visible = fabExpanded,
                onDismiss = { fabExpanded = false }
            )
        }
    }
}
