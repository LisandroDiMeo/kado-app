package com.kado.app.presentation.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.SessionSummary
import com.kado.app.domain.srs.SrsEngine
import com.kado.app.presentation.model.DisplayableCardContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReviewUiState(
    val currentCard: ReviewCard? = null,
    val frontContent: DisplayableCardContent? = null,
    val backContent: DisplayableCardContent? = null,
    val isFlipped: Boolean = false,
    val hasBeenFlipped: Boolean = false,
    val isFinished: Boolean = false,
    val summary: SessionSummary = SessionSummary(0, 0, 0, 0, 0),
    val remaining: Int = 0,
    val isLoading: Boolean = true,
    val intervals: Map<Rating, String> = emptyMap()
)

class ReviewViewModel(private val deckId: Long, private val subDeckIndex: Int? = null) : ViewModel() {
    private val repository = AppDependencies.deckRepository
    private val htmlRenderer = AppDependencies.htmlRenderer

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState

    private var newLimit = 20
    private var summary = SessionSummary(0, 0, 0, 0, 0)

    init {
        viewModelScope.launch {
            val deck = repository.getDeck(deckId)
            newLimit = deck?.dailyLimit ?: 20
            loadNextCard()
        }
    }

    private suspend fun loadNextCard() {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val card = if (subDeckIndex != null) {
            repository.getNextSubDeckReviewCard(deckId, subDeckIndex, now, newLimit)
        } else {
            repository.getNextReviewCard(deckId, now, newLimit)
        }
        if (card == null) {
            _uiState.value = _uiState.value.copy(
                currentCard = null,
                frontContent = null,
                backContent = null,
                isFinished = true,
                isLoading = false,
                summary = summary
            )
        } else {
            val intervals = SrsEngine.previewIntervals(card.state, now)
            _uiState.value = _uiState.value.copy(
                currentCard = card,
                frontContent = DisplayableCardContent.from(card.card.front, htmlRenderer),
                backContent = DisplayableCardContent.from(card.card.back, htmlRenderer),
                isFlipped = false,
                hasBeenFlipped = false,
                isLoading = false,
                intervals = intervals
            )
        }
    }

    fun flip() {
        val current = _uiState.value
        _uiState.value = current.copy(
            isFlipped = !current.isFlipped,
            hasBeenFlipped = true
        )
    }

    fun rate(rating: Rating) {
        val card = _uiState.value.currentCard ?: return
        val wasFlipped = _uiState.value.isFlipped

        // Immediately unflip and hide buttons
        _uiState.value = _uiState.value.copy(
            isFlipped = false,
            hasBeenFlipped = false
        )

        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().epochSeconds
            val newState = SrsEngine.reviewCard(card.state, rating, now)
            repository.updateCardState(newState)

            if (card.state.queue == 0) newLimit--

            summary = summary.copy(
                reviewed = summary.reviewed + 1,
                again = summary.again + if (rating == Rating.Again) 1 else 0,
                hard = summary.hard + if (rating == Rating.Hard) 1 else 0,
                good = summary.good + if (rating == Rating.Good) 1 else 0,
                easy = summary.easy + if (rating == Rating.Easy) 1 else 0
            )

            // Wait for flip-back animation before transitioning
            if (wasFlipped) delay(400)

            loadNextCard()
        }
    }
}
