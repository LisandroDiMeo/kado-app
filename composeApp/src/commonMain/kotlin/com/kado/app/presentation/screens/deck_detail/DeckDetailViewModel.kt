package com.kado.app.presentation.screens.deck_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kado.app.data.importer.ApkgParser
import com.kado.app.data.importer.ParsedCard
import com.kado.app.data.importer.ZipExtractor
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.BackfillResult
import com.kado.app.domain.model.Card
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.DeckPatchGate
import com.kado.app.domain.model.DeckPatchHandle
import com.kado.app.domain.model.SubDeckInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class PatchErrorCode { InvalidApkg, ReadDbFailed, NoCards, ParseFailed }

sealed interface PatchPhase {
    data object Idle : PatchPhase
    data object ParsingApkg : PatchPhase
    data class NeedsBackfill(val nullGuidCount: Int) : PatchPhase
    data object RunningBackfill : PatchPhase
    data class BackfillDone(val result: BackfillResult) : PatchPhase
    data object NoChanges : PatchPhase
    data class PatchReady(val handle: DeckPatchHandle) : PatchPhase
    data class Error(val code: PatchErrorCode, val cause: String? = null) : PatchPhase
}

data class DeckDetailUiState(
    val deck: Deck? = null,
    val cardCount: Int = 0,
    val dueCount: Int = 0,
    val newCount: Int = 0,
    val isLoading: Boolean = true,
    val subDecks: List<SubDeckInfo> = emptyList(),
    val hasPartitions: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedCardIds: Set<Long> = emptySet(),
    val patchPhase: PatchPhase = PatchPhase.Idle
)

class DeckDetailViewModel(private val deckId: Long) : ViewModel() {
    private val repository = AppDependencies.deckRepository

    private val _uiState = MutableStateFlow(DeckDetailUiState())
    val uiState: StateFlow<DeckDetailUiState> = _uiState

    val pagedCards: Flow<PagingData<Card>> = repository.observeCardsPaged(deckId)
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(deck = repository.getDeck(deckId))
        }
        repository.observeCardCount(deckId)
            .onEach { count ->
                val now = kotlin.time.Clock.System.now().epochSeconds
                val summary = repository.getDeckSummary(deckId, now)
                _uiState.value = _uiState.value.copy(
                    cardCount = count,
                    dueCount = summary?.dueCards ?: 0,
                    newCount = summary?.newCards ?: 0,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
        repository.observeSubDeckIndices(deckId)
            .onEach { indices ->
                val now = kotlin.time.Clock.System.now().epochSeconds
                val subDecks = indices.map { index ->
                    repository.getSubDeckSummary(deckId, index, now)
                }
                _uiState.value = _uiState.value.copy(
                    subDecks = subDecks,
                    hasPartitions = indices.isNotEmpty()
                )
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().epochSeconds
            val summary = repository.getDeckSummary(deckId, now)
            val indices = repository.getSubDeckIndices(deckId)
            val subDecks = indices.map { index ->
                repository.getSubDeckSummary(deckId, index, now)
            }
            _uiState.value = _uiState.value.copy(
                dueCount = summary?.dueCards ?: 0,
                newCount = summary?.newCards ?: 0,
                subDecks = subDecks,
                hasPartitions = indices.isNotEmpty()
            )
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
        }
    }

    fun enterSelectionMode() {
        _uiState.value = _uiState.value.copy(selectionMode = true, selectedCardIds = emptySet())
    }

    fun enterSelectionWith(cardId: Long) {
        _uiState.value = _uiState.value.copy(
            selectionMode = true,
            selectedCardIds = setOf(cardId)
        )
    }

    fun toggleSelection(cardId: Long) {
        val current = _uiState.value.selectedCardIds
        val updated = if (cardId in current) current - cardId else current + cardId
        if (updated.isEmpty()) {
            clearSelection()
        } else {
            _uiState.value = _uiState.value.copy(selectedCardIds = updated)
        }
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectionMode = false, selectedCardIds = emptySet())
    }

    fun deleteSelected() {
        val ids = _uiState.value.selectedCardIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.deleteCards(ids)
            clearSelection()
        }
    }

    fun removeSubDeck(subDeckIndex: Int) {
        viewModelScope.launch {
            repository.removeSubDeck(deckId, subDeckIndex)
        }
    }

    fun cloneSubDeck(subDeckIndex: Int) {
        viewModelScope.launch {
            val deckName = _uiState.value.deck?.name ?: "Deck"
            // Display index is 1-based for user-facing label
            val displayIndex = _uiState.value.subDecks.indexOfFirst { it.index == subDeckIndex } + 1
            repository.cloneSubDeckAsNewDeck(deckId, subDeckIndex, "$deckName - Part $displayIndex")
        }
    }

    fun createReversedDeck() {
        viewModelScope.launch {
            val deckName = _uiState.value.deck?.name ?: "Deck"
            repository.createReversedDeck(deckId, "$deckName (Reversed)")
        }
    }

    fun startUpdate(apkgBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(patchPhase = PatchPhase.ParsingApkg)
            val parsed = parseApkg(apkgBytes) ?: return@launch
            when (val gate = repository.checkDeckPatchGate(deckId)) {
                is DeckPatchGate.NeedsBackfill ->
                    _uiState.value = _uiState.value.copy(
                        patchPhase = PatchPhase.NeedsBackfill(gate.nullGuidCount)
                    )
                DeckPatchGate.Ready -> {
                    val handle = repository.previewDeckPatch(deckId, parsed)
                    if (handle.counts.totalChanges == 0) {
                        // No-op patch — drop the empty session and surface a friendly notice.
                        repository.discardDeckPatch(handle)
                        _uiState.value = _uiState.value.copy(patchPhase = PatchPhase.NoChanges)
                    } else {
                        _uiState.value = _uiState.value.copy(patchPhase = PatchPhase.PatchReady(handle))
                    }
                }
            }
        }
    }

    fun runBackfill(apkgBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(patchPhase = PatchPhase.RunningBackfill)
            val parsed = parseApkg(apkgBytes) ?: return@launch
            val result = repository.backfillAnkiGuids(deckId, parsed)
            _uiState.value = _uiState.value.copy(patchPhase = PatchPhase.BackfillDone(result))
        }
    }

    fun dismissPatchPhase() {
        _uiState.value = _uiState.value.copy(patchPhase = PatchPhase.Idle)
    }

    private suspend fun parseApkg(fileBytes: ByteArray): List<ParsedCard>? {
        return try {
            val entries = ZipExtractor.listEntries(fileBytes)
            val dbEntryName = entries.firstOrNull {
                it == "collection.anki21" || it == "collection.anki2" || it == "collection.anki21b"
            }
            if (dbEntryName == null) {
                _uiState.value = _uiState.value.copy(
                    patchPhase = PatchPhase.Error(PatchErrorCode.InvalidApkg)
                )
                return null
            }
            val extracted = ZipExtractor.extractEntries(fileBytes, setOf(dbEntryName))
            val dbBytes = extracted[dbEntryName]
            if (dbBytes == null) {
                _uiState.value = _uiState.value.copy(
                    patchPhase = PatchPhase.Error(PatchErrorCode.ReadDbFailed)
                )
                return null
            }
            val data = ApkgParser.parse(dbBytes)
            if (data.cards.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    patchPhase = PatchPhase.Error(PatchErrorCode.NoCards)
                )
                return null
            }
            data.cards
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                patchPhase = PatchPhase.Error(PatchErrorCode.ParseFailed, cause = e.message)
            )
            null
        }
    }
}
