package com.kado.app.presentation.screens.card_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CardEditUiState(
    val front: String = "",
    val back: String = "",
    val isNew: Boolean = true,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)

class CardEditViewModel(
    private val deckId: Long,
    private val cardId: Long
) : ViewModel() {
    private val repository = AppDependencies.deckRepository

    private val _uiState = MutableStateFlow(CardEditUiState())
    val uiState: StateFlow<CardEditUiState> = _uiState

    private var existingCard: Card? = null

    init {
        if (cardId > 0) {
            viewModelScope.launch {
                repository.getCard(cardId)?.let { card ->
                    existingCard = card
                    _uiState.value = CardEditUiState(
                        front = card.front,
                        back = card.back,
                        isNew = false
                    )
                }
            }
        }
    }

    fun onFrontChange(front: String) {
        _uiState.value = _uiState.value.copy(front = front)
    }

    fun onBackChange(back: String) {
        _uiState.value = _uiState.value.copy(back = back)
    }

    fun save() {
        val state = _uiState.value
        if (state.front.isBlank() || state.back.isBlank()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            if (state.isNew) {
                repository.addCard(deckId, state.front.trim(), state.back.trim())
            } else {
                existingCard?.let {
                    repository.updateCard(it.copy(front = state.front.trim(), back = state.back.trim()))
                }
            }
            _uiState.value = _uiState.value.copy(isSaved = true, isLoading = false)
        }
    }

    fun delete() {
        if (cardId <= 0) return
        viewModelScope.launch {
            repository.deleteCard(cardId)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
