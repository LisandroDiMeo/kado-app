package com.kado.app.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Deck
import com.kado.app.domain.usecase.CalculateDeckStatsUseCase
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
    private val calculateDeckStats = CalculateDeckStatsUseCase(repository)

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        viewModelScope.launch { loadStats() }
    }

    private suspend fun loadStats() {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val stats = calculateDeckStats(deckId, now)

        _uiState.value = StatsUiState(
            deck = stats.deck,
            totalCards = stats.totalCards,
            newCards = stats.newCards,
            learningCards = stats.learningCards,
            youngCards = stats.youngCards,
            matureCards = stats.matureCards,
            dueNow = stats.dueNow,
            isLoading = false
        )
    }

    fun resetProgress() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.resetProgress(deckId)
            _uiState.value = _uiState.value.copy(isReset = true)
            loadStats()
        }
    }
}
