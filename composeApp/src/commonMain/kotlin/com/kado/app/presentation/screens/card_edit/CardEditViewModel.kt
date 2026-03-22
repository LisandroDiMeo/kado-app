package com.kado.app.presentation.screens.card_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.data.importer.MediaStorage
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Card
import com.kado.app.presentation.components.ImageInfo
import com.kado.app.presentation.model.DisplayableCardContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ActiveField { FRONT, BACK }

data class CardEditUiState(
    val front: String = "",
    val back: String = "",
    val frontDisplayable: DisplayableCardContent = DisplayableCardContent.PlainText(""),
    val backDisplayable: DisplayableCardContent = DisplayableCardContent.PlainText(""),
    val isNew: Boolean = true,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val showPreview: Boolean = false,
    val showImageDialog: Boolean = false,
    val activeField: ActiveField = ActiveField.FRONT,
    val cursorPosition: Int = 0,
    val attachedImages: List<ImageInfo> = emptyList()
)

class CardEditViewModel(
    private val deckId: Long,
    private val cardId: Long
) : ViewModel() {
    private val repository = AppDependencies.deckRepository
    private val parser = AppDependencies.cardContentParser
    private val htmlRenderer = AppDependencies.htmlRenderer

    private val _uiState = MutableStateFlow(CardEditUiState())
    val uiState: StateFlow<CardEditUiState> = _uiState

    private var existingCard: Card? = null

    init {
        if (cardId > 0) {
            viewModelScope.launch {
                repository.getCard(cardId)?.let { card ->
                    existingCard = card
                    val frontRaw = card.front.rawText
                    val backRaw = card.back.rawText
                    _uiState.value = CardEditUiState(
                        front = frontRaw,
                        back = backRaw,
                        frontDisplayable = toDisplayable(frontRaw),
                        backDisplayable = toDisplayable(backRaw),
                        isNew = false
                    )
                    refreshAttachedImages()
                }
            }
        }
    }

    fun onFrontChange(front: String) {
        _uiState.value = _uiState.value.copy(
            front = front,
            frontDisplayable = toDisplayable(front)
        )
    }

    fun onBackChange(back: String) {
        _uiState.value = _uiState.value.copy(
            back = back,
            backDisplayable = toDisplayable(back)
        )
    }

    private fun toDisplayable(raw: String): DisplayableCardContent {
        return DisplayableCardContent.from(parser.detect(raw), htmlRenderer)
    }

    fun onCursorPositionChange(field: ActiveField, position: Int) {
        _uiState.value = _uiState.value.copy(activeField = field, cursorPosition = position)
    }

    fun togglePreview() {
        _uiState.value = _uiState.value.copy(showPreview = !_uiState.value.showPreview)
    }

    fun showImageDialog() {
        refreshAttachedImages()
        _uiState.value = _uiState.value.copy(showImageDialog = true)
    }

    fun dismissImageDialog() {
        _uiState.value = _uiState.value.copy(showImageDialog = false)
    }

    fun onImagePicked(bytes: ByteArray) {
        val filename = "img_${epochSeconds()}_${Random.nextInt(1000, 9999)}.jpg"
        MediaStorage.saveMedia(deckId, filename, bytes)
        insertImageMarker(filename)
        refreshAttachedImages()
    }

    fun insertImageMarker(filename: String) {
        val state = _uiState.value
        when (state.activeField) {
            ActiveField.FRONT -> {
                val newText = parser.insertMarker(state.front, state.cursorPosition, filename)
                _uiState.value = state.copy(front = newText, frontDisplayable = toDisplayable(newText))
            }
            ActiveField.BACK -> {
                val newText = parser.insertMarker(state.back, state.cursorPosition, filename)
                _uiState.value = state.copy(back = newText, backDisplayable = toDisplayable(newText))
            }
        }
    }

    fun deleteImage(filename: String) {
        MediaStorage.deleteMedia(deckId, filename)
        val state = _uiState.value
        val newFront = parser.removeAllMarkersForFile(state.front, filename)
        val newBack = parser.removeAllMarkersForFile(state.back, filename)
        _uiState.value = state.copy(
            front = newFront,
            back = newBack,
            frontDisplayable = toDisplayable(newFront),
            backDisplayable = toDisplayable(newBack)
        )
        refreshAttachedImages()
    }

    fun refreshAttachedImages() {
        val state = _uiState.value
        val filenames = (parser.extractImageFilenames(state.front) +
                parser.extractImageFilenames(state.back)).distinct()
        val images = filenames.map { filename ->
            ImageInfo(
                filename = filename,
                path = MediaStorage.getMediaPath(deckId, filename)
            )
        }
        _uiState.value = _uiState.value.copy(attachedImages = images)
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
                    repository.updateCard(it.copy(
                        front = parser.detect(state.front.trim()),
                        back = parser.detect(state.back.trim())
                    ))
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

    private fun epochSeconds(): Long {
        return kotlin.time.Clock.System.now().epochSeconds
    }
}
