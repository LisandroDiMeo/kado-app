package com.kado.app.presentation.screens.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.repository.DeviceDeck
import com.kado.app.domain.usecase.PrepareTransferDeckUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TransferState {
    data object Idle : TransferState()
    data object Uploading : TransferState()
    data object Success : TransferState()
    data class Error(val message: String) : TransferState()
}

data class TransferUiState(
    val deckName: String = "",
    val cardCount: Int = 0,
    val transferState: TransferState = TransferState.Idle,
    val deviceDecks: List<DeviceDeck> = emptyList(),
    val freeMb: Float = 0f,
    val isLoadingDevice: Boolean = false
)

class TransferViewModel(private val deckId: Long, private val subDeckIndex: Int? = null) : ViewModel() {
    private val deckRepository = AppDependencies.deckRepository
    private val deviceRepository = AppDependencies.deviceRepository
    private val prepareTransferDeck = PrepareTransferDeckUseCase(deckRepository)

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState

    init {
        viewModelScope.launch {
            val deck = deckRepository.getDeck(deckId)
            val cards = if (subDeckIndex != null) {
                deckRepository.getSubDeckCards(deckId, subDeckIndex)
            } else {
                deckRepository.getCards(deckId)
            }
            val displayName = PrepareTransferDeckUseCase.formatSubDeckName(
                deck?.name ?: "Unknown",
                subDeckIndex
            )
            _uiState.value = _uiState.value.copy(
                deckName = displayName,
                cardCount = cards.size
            )
        }
        loadDeviceDecks()
    }

    fun loadDeviceDecks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDevice = true)
            try {
                val response = deviceRepository.getDecks()
                _uiState.value = _uiState.value.copy(
                    deviceDecks = response.decks,
                    freeMb = response.freeMb,
                    isLoadingDevice = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingDevice = false)
            }
        }
    }

    fun upload() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(transferState = TransferState.Uploading)
            try {
                val payload = prepareTransferDeck(deckId, subDeckIndex)
                val success = deviceRepository.uploadDeck(payload.aldBytes, payload.filename)
                _uiState.value = _uiState.value.copy(
                    transferState = if (success) TransferState.Success else TransferState.Error("Upload failed")
                )
                if (success) loadDeviceDecks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    transferState = TransferState.Error(e.message ?: "Upload failed")
                )
            }
        }
    }

    fun deleteDeviceDeck(index: Int) {
        viewModelScope.launch {
            try {
                deviceRepository.deleteDeck(index)
                loadDeviceDecks()
            } catch (_: Exception) { }
        }
    }
}
