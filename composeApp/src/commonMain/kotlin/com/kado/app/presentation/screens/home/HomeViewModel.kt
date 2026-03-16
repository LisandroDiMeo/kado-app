package com.kado.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.DeckSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HomeUiState(
    val decks: List<DeckSummary> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val repository = AppDependencies.deckRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        combine(
            repository.observeDecks(),
            repository.observeCardCount(),
            repository.observeCardStateCount()
        ) { decks, _, _ -> decks }
            .onEach { decks ->
                val now = kotlin.time.Clock.System.now().epochSeconds
                val summaries = decks.map { deck ->
                    repository.getDeckSummary(deck.id, now)
                        ?: DeckSummary(deck, 0, 0, 0)
                }
                _uiState.value = HomeUiState(decks = summaries, isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun deleteDeck(deckId: Long) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }
}
