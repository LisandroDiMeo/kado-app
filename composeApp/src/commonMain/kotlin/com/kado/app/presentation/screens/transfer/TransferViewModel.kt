package com.kado.app.presentation.screens.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.data.converter.AldConverter
import com.kado.app.domain.repository.DeviceDeck
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
            val baseName = deck?.name ?: "Unknown"
            val displayName = if (subDeckIndex != null) "$baseName - Part ${subDeckIndex + 1}" else baseName
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
                val deck = deckRepository.getDeck(deckId) ?: throw Exception("Deck not found")
                val cards = if (subDeckIndex != null) {
                    deckRepository.getSubDeckCards(deckId, subDeckIndex)
                } else {
                    deckRepository.getCards(deckId)
                }
                val transferName = if (subDeckIndex != null) "${deck.name} - Part ${subDeckIndex + 1}" else deck.name
                val transferDeck = deck.copy(name = transferName)
                val aldBytes = AldConverter.toAld(transferDeck, cards)
                val filename = AldConverter.toAldFilename(transferDeck.name)
                val success = deviceRepository.uploadDeck(aldBytes, filename)
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
