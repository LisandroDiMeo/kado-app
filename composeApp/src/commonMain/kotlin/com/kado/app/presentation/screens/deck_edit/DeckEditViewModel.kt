package com.kado.app.presentation.screens.deck_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Deck
import com.kado.app.domain.srs.SchedulerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface FieldError {
    data object RetentionRange : FieldError
    data object InvalidNumber : FieldError
    data object PositiveInteger : FieldError
    data object InvalidStepsFormat : FieldError
}

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
    val enableFuzzing: Boolean = true,
    val dailyLimitError: FieldError? = null,
    val desiredRetentionError: FieldError? = null,
    val learningStepsError: FieldError? = null,
    val relearningStepsError: FieldError? = null,
    val maxIntervalError: FieldError? = null
)

val DeckEditUiState.hasValidationErrors: Boolean get() =
    dailyLimitError != null ||
        (
            schedulerType == SchedulerType.FSRS &&
                (
                    desiredRetentionError != null ||
                        learningStepsError != null ||
                        relearningStepsError != null ||
                        maxIntervalError != null
                    )
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
        val error = validatePositiveInt(limit)
        _uiState.value = _uiState.value.copy(dailyLimit = limit, dailyLimitError = error)
    }

    fun onSchedulerTypeChange(type: SchedulerType) {
        _uiState.value = _uiState.value.copy(schedulerType = type)
    }

    fun onDesiredRetentionChange(value: String) {
        val error = validateRetention(value)
        _uiState.value = _uiState.value.copy(desiredRetention = value, desiredRetentionError = error)
    }

    fun onLearningStepsChange(value: String) {
        val error = validateSteps(value)
        _uiState.value = _uiState.value.copy(learningSteps = value, learningStepsError = error)
    }

    fun onRelearningStepsChange(value: String) {
        val error = validateSteps(value)
        _uiState.value = _uiState.value.copy(relearningSteps = value, relearningStepsError = error)
    }

    fun onMaxIntervalChange(value: String) {
        val error = validatePositiveInt(value)
        _uiState.value = _uiState.value.copy(maxInterval = value, maxIntervalError = error)
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
            enableFuzzing = true,
            desiredRetentionError = null,
            learningStepsError = null,
            relearningStepsError = null,
            maxIntervalError = null
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
        if (state.hasValidationErrors) return
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
                        repository.updateDeck(
                            createdDeck.copy(
                                schedulerType = state.schedulerType,
                                fsrsDesiredRetention = clampedRetention,
                                fsrsLearningSteps = state.learningSteps.trim(),
                                fsrsRelearningSteps = state.relearningSteps.trim(),
                                fsrsMaxInterval = maxInterval,
                                fsrsEnableFuzzing = state.enableFuzzing
                            )
                        )
                    }
                }
            } else {
                existingDeck?.let {
                    repository.updateDeck(
                        it.copy(
                            name = state.name.trim(),
                            dailyLimit = limit,
                            schedulerType = state.schedulerType,
                            fsrsDesiredRetention = clampedRetention,
                            fsrsLearningSteps = state.learningSteps.trim(),
                            fsrsRelearningSteps = state.relearningSteps.trim(),
                            fsrsMaxInterval = maxInterval,
                            fsrsEnableFuzzing = state.enableFuzzing
                        )
                    )
                }
            }
            _uiState.value = _uiState.value.copy(isSaved = true, isLoading = false)
        }
    }

    private fun validateRetention(value: String): FieldError? {
        if (value.isBlank()) return FieldError.InvalidNumber
        val d = value.toDoubleOrNull() ?: return FieldError.InvalidNumber
        if (d < 0.70 || d > 0.99) return FieldError.RetentionRange
        return null
    }

    private fun validatePositiveInt(value: String): FieldError? {
        if (value.isBlank()) return FieldError.PositiveInteger
        val n = value.toIntOrNull() ?: return FieldError.PositiveInteger
        if (n <= 0) return FieldError.PositiveInteger
        return null
    }

    private fun validateSteps(input: String): FieldError? {
        if (input.isBlank()) return null
        val tokens = input.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (tokens.isEmpty()) return null
        val pattern = Regex("^\\d+[mhsMHS]?$")
        for (token in tokens) {
            if (!token.matches(pattern)) return FieldError.InvalidStepsFormat
            val numPart = token.replace(Regex("[mhsMHS]$"), "")
            val num = numPart.toLongOrNull() ?: return FieldError.InvalidStepsFormat
            if (num <= 0) return FieldError.InvalidStepsFormat
        }
        return null
    }
}
