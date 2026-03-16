package com.kado.app.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val deck: Deck? = null,
    val totalCards: Int = 0,
    val newCards: Int = 0,
    val learningCards: Int = 0,
    val youngCards: Int = 0,
    val matureCards: Int = 0,
    val dueNow: Int = 0,
    val isLoading: Boolean = true,
    val isReset: Boolean = false
)

class StatsViewModel(private val deckId: Long) : ViewModel() {
    private val repository = AppDependencies.deckRepository

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        viewModelScope.launch { loadStats() }
    }

    private suspend fun loadStats() {
        val deck = repository.getDeck(deckId)
        val cards = repository.getCards(deckId)
        val states = repository.getCardStates(deckId)
        val now = kotlin.time.Clock.System.now().epochSeconds

        val newCount = states.count { it.queue == 0 }
        val learningCount = states.count { it.queue == 1 }
        val youngCount = states.count { it.queue == 2 && it.interval < 21 }
        val matureCount = states.count { it.queue == 2 && it.interval >= 21 }
        val dueCount = states.count {
            it.queue == 0 || (it.queue != 0 && it.due <= now)
        }

        _uiState.value = StatsUiState(
            deck = deck,
            totalCards = cards.size,
            newCards = newCount,
            learningCards = learningCount,
            youngCards = youngCount,
            matureCards = matureCount,
            dueNow = dueCount,
            isLoading = false
        )
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress(deckId)
            _uiState.value = _uiState.value.copy(isReset = true)
            loadStats()
        }
    }
}
