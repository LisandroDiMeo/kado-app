package com.kado.app.presentation.screens.deck_patch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.DeckPatchHandle
import com.kado.app.domain.model.DeckPatchPreviewItem
import com.kado.app.domain.model.PatchCounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 30

enum class PatchPreviewError { SessionMissing, ApplyFailed }

data class DeckPatchPreviewUiState(
    val isLoading: Boolean = true,
    val deckName: String = "",
    val counts: PatchCounts = PatchCounts(0, 0, 0, 0),
    val added: List<DeckPatchPreviewItem.Added> = emptyList(),
    val modified: List<DeckPatchPreviewItem.Modified> = emptyList(),
    val removed: List<DeckPatchPreviewItem.Removed> = emptyList(),
    val keepRemovedIds: Set<Long> = emptySet(),
    val expandedSection: Section = Section.None,
    val isApplying: Boolean = false,
    val isApplied: Boolean = false,
    val error: PatchPreviewError? = null,
    val errorDetail: String? = null
) {
    enum class Section { None, Added, Modified, Removed }
}

class DeckPatchPreviewViewModel(private val sessionId: String, private val deckId: Long) : ViewModel() {
    private val repository = AppDependencies.deckRepository

    private val _uiState = MutableStateFlow(DeckPatchPreviewUiState())
    val uiState: StateFlow<DeckPatchPreviewUiState> = _uiState

    private var handle: DeckPatchHandle? = null

    init {
        viewModelScope.launch {
            val rebuilt = repository.rebuildPatchHandle(sessionId, deckId)
            if (rebuilt == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = PatchPreviewError.SessionMissing
                )
                return@launch
            }
            handle = rebuilt
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                deckName = rebuilt.deckName,
                counts = rebuilt.counts
            )
        }
    }

    fun expandSection(section: DeckPatchPreviewUiState.Section) {
        if (_uiState.value.expandedSection == section) {
            _uiState.value = _uiState.value.copy(expandedSection = DeckPatchPreviewUiState.Section.None)
            return
        }
        _uiState.value = _uiState.value.copy(expandedSection = section)
        val h = handle ?: return
        viewModelScope.launch {
            when (section) {
                DeckPatchPreviewUiState.Section.Added ->
                    if (_uiState.value.added.size < _uiState.value.counts.added) {
                        val rows = repository.pageAddedCards(h, 0, PAGE_SIZE)
                        _uiState.value = _uiState.value.copy(added = rows)
                    }
                DeckPatchPreviewUiState.Section.Modified ->
                    if (_uiState.value.modified.size < _uiState.value.counts.modified) {
                        val rows = repository.pageModifiedCards(h, 0, PAGE_SIZE)
                        _uiState.value = _uiState.value.copy(modified = rows)
                    }
                DeckPatchPreviewUiState.Section.Removed ->
                    if (_uiState.value.removed.size < _uiState.value.counts.removed) {
                        val rows = repository.pageRemovedCards(h, 0, PAGE_SIZE)
                        _uiState.value = _uiState.value.copy(removed = rows)
                    }
                DeckPatchPreviewUiState.Section.None -> Unit
            }
        }
    }

    fun loadMoreRemoved() {
        val h = handle ?: return
        val current = _uiState.value.removed.size
        if (current >= _uiState.value.counts.removed) return
        viewModelScope.launch {
            val rows = repository.pageRemovedCards(h, current, PAGE_SIZE)
            _uiState.value = _uiState.value.copy(removed = _uiState.value.removed + rows)
        }
    }

    fun toggleKeep(cardId: Long) {
        val current = _uiState.value.keepRemovedIds
        val updated = if (cardId in current) current - cardId else current + cardId
        _uiState.value = _uiState.value.copy(keepRemovedIds = updated)
    }

    fun apply() {
        val h = handle ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            try {
                repository.applyDeckPatch(h, _uiState.value.keepRemovedIds)
                _uiState.value = _uiState.value.copy(isApplying = false, isApplied = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    error = PatchPreviewError.ApplyFailed,
                    errorDetail = e.message
                )
            }
        }
    }

    fun discard() {
        val h = handle ?: return
        viewModelScope.launch {
            repository.discardDeckPatch(h)
            _uiState.value = _uiState.value.copy(isApplied = true)
        }
    }
}
