package com.kado.app.presentation.screens.deck_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Deck
import com.kado.app.domain.srs.SchedulerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DeckEditUiState(
    val name: String = "",
    val dailyLimit: String = "20",
    val isNew: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val isLoading: Boolean = false,
    val schedulerType: SchedulerType = SchedulerType.SM2,
    val desiredRetention: String = "0.9",
    val learningSteps: String = "1m, 10m",
    val relearningSteps: String = "10m",
    val maxInterval: String = "36500",
    val enableFuzzing: Boolean = true
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
                        isNew = false,
                        schedulerType = deck.schedulerType,
                        desiredRetention = deck.fsrsDesiredRetention.toString(),
                        learningSteps = deck.fsrsLearningSteps,
                        relearningSteps = deck.fsrsRelearningSteps,
                        maxInterval = deck.fsrsMaxInterval.toString(),
                        enableFuzzing = deck.fsrsEnableFuzzing
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

    fun onSchedulerTypeChange(type: SchedulerType) {
        _uiState.value = _uiState.value.copy(schedulerType = type)
    }

    fun onDesiredRetentionChange(value: String) {
        _uiState.value = _uiState.value.copy(desiredRetention = value)
    }

    fun onLearningStepsChange(value: String) {
        _uiState.value = _uiState.value.copy(learningSteps = value)
    }

    fun onRelearningStepsChange(value: String) {
        _uiState.value = _uiState.value.copy(relearningSteps = value)
    }

    fun onMaxIntervalChange(value: String) {
        _uiState.value = _uiState.value.copy(maxInterval = value)
    }

    fun onFuzzingToggle() {
        _uiState.value = _uiState.value.copy(enableFuzzing = !_uiState.value.enableFuzzing)
    }

    fun resetFsrsDefaults() {
        _uiState.value = _uiState.value.copy(
            desiredRetention = "0.9",
            learningSteps = "1m, 10m",
            relearningSteps = "10m",
            maxInterval = "36500",
            enableFuzzing = true
        )
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
        val retention = state.desiredRetention.toDoubleOrNull() ?: 0.9
        val clampedRetention = retention.coerceIn(0.70, 0.99)
        val maxInterval = state.maxInterval.toIntOrNull() ?: 36500

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            if (state.isNew) {
                val deckId = repository.createDeck(state.name.trim(), limit)
                // Update with scheduler settings after creation
                if (state.schedulerType == SchedulerType.FSRS) {
                    repository.getDeck(deckId)?.let { createdDeck ->
                        repository.updateDeck(createdDeck.copy(
                            schedulerType = state.schedulerType,
                            fsrsDesiredRetention = clampedRetention,
                            fsrsLearningSteps = state.learningSteps.trim(),
                            fsrsRelearningSteps = state.relearningSteps.trim(),
                            fsrsMaxInterval = maxInterval,
                            fsrsEnableFuzzing = state.enableFuzzing
                        ))
                    }
                }
            } else {
                existingDeck?.let {
                    repository.updateDeck(it.copy(
                        name = state.name.trim(),
                        dailyLimit = limit,
                        schedulerType = state.schedulerType,
                        fsrsDesiredRetention = clampedRetention,
                        fsrsLearningSteps = state.learningSteps.trim(),
                        fsrsRelearningSteps = state.relearningSteps.trim(),
                        fsrsMaxInterval = maxInterval,
                        fsrsEnableFuzzing = state.enableFuzzing
                    ))
                }
            }
            _uiState.value = _uiState.value.copy(isSaved = true, isLoading = false)
        }
    }
}
