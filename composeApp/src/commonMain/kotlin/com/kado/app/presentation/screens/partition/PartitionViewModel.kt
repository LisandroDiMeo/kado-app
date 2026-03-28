package com.kado.app.presentation.screens.partition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Card
import com.kado.app.domain.usecase.CalculatePartitionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PartitionMode { Batch, Manual }

data class PartitionUiState(
    val cards: List<Card> = emptyList(),
    val mode: PartitionMode = PartitionMode.Batch,
    val batchSize: Int = 10,
    val assignments: Map<Long, Int> = emptyMap(), // cardId -> subDeckIndex for manual mode
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val hasExistingPartitions: Boolean = false
)

class PartitionViewModel(private val deckId: Long) : ViewModel() {
    private val repository = AppDependencies.deckRepository
    private val calculatePartition = CalculatePartitionUseCase()

    private val _uiState = MutableStateFlow(PartitionUiState())
    val uiState: StateFlow<PartitionUiState> = _uiState

    init {
        viewModelScope.launch {
            val cards = repository.getCards(deckId)
            val indices = repository.getSubDeckIndices(deckId)
            val assignments = cards.filter { it.subDeckIndex != null }
                .associate { it.id to it.subDeckIndex!! }
            _uiState.value = _uiState.value.copy(
                cards = cards,
                hasExistingPartitions = indices.isNotEmpty(),
                assignments = assignments,
                batchSize = minOf(10, maxOf(1, cards.size))
            )
        }
    }

    fun setMode(mode: PartitionMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun setBatchSize(size: Int) {
        _uiState.value = _uiState.value.copy(batchSize = size)
    }

    fun setCardAssignment(cardId: Long, subDeckIndex: Int) {
        val assignments = _uiState.value.assignments.toMutableMap()
        assignments[cardId] = subDeckIndex
        _uiState.value = _uiState.value.copy(assignments = assignments)
    }

    fun applyBatchPartition() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            repository.batchPartition(deckId, _uiState.value.batchSize)
            _uiState.value = _uiState.value.copy(isSaving = false, isDone = true)
        }
    }

    fun applyManualPartition() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            // Clear existing partitions first
            repository.clearPartitions(deckId)
            // Apply manual assignments
            for ((cardId, subDeckIndex) in _uiState.value.assignments) {
                repository.assignSubDeck(cardId, subDeckIndex)
            }
            _uiState.value = _uiState.value.copy(isSaving = false, isDone = true)
        }
    }

    val subDeckCount: Int
        get() {
            val state = _uiState.value
            return calculatePartition(state.cards.size, state.batchSize).subDeckCount
        }

    val lastSubDeckSize: Int
        get() {
            val state = _uiState.value
            return calculatePartition(state.cards.size, state.batchSize).lastSubDeckSize
        }
}
