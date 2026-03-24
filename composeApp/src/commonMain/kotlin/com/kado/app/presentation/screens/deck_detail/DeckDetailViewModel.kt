package com.kado.app.presentation.screens.deck_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Card
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.SubDeckInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DeckDetailUiState(
    val deck: Deck? = null,
    val cardCount: Int = 0,
    val dueCount: Int = 0,
    val newCount: Int = 0,
    val isLoading: Boolean = true,
    val subDecks: List<SubDeckInfo> = emptyList(),
    val hasPartitions: Boolean = false
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
}
