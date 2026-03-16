package com.kado.app.presentation.screens.deck_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DeckEditUiState(
    val name: String = "",
    val dailyLimit: String = "20",
    val isNew: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val isLoading: Boolean = false
)

class DeckEditViewModel(private val deckId: Long) : ViewModel() {
    private val repository = AppDependencies.deckRepository

    private val _uiState = MutableStateFlow(DeckEditUiState())
    val uiState: StateFlow<DeckEditUiState> = _uiState

    private var existingDeck: Deck? = null

    init {
        if (deckId > 0) {
            viewModelScope.launch {
                repository.getDeck(deckId)?.let { deck ->
                    existingDeck = deck
                    _uiState.value = DeckEditUiState(
                        name = deck.name,
                        dailyLimit = deck.dailyLimit.toString(),
                        isNew = false
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onDailyLimitChange(limit: String) {
        _uiState.value = _uiState.value.copy(dailyLimit = limit)
    }

    fun deleteDeck() {
        if (deckId <= 0) return
        viewModelScope.launch {
            repository.deleteDeck(deckId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        val limit = state.dailyLimit.toIntOrNull() ?: 20

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            if (state.isNew) {
                repository.createDeck(state.name.trim(), limit)
            } else {
                existingDeck?.let {
                    repository.updateDeck(it.copy(name = state.name.trim(), dailyLimit = limit))
                }
            }
            _uiState.value = _uiState.value.copy(isSaved = true, isLoading = false)
        }
    }
}
