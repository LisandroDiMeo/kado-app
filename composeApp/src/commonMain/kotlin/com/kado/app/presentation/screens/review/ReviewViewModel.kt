package com.kado.app.presentation.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.SessionSummary
import com.kado.app.domain.srs.FsrsParameters
import com.kado.app.domain.srs.FsrsScheduler
import com.kado.app.domain.srs.Scheduler
import com.kado.app.domain.srs.SchedulerType
import com.kado.app.domain.srs.Sm2Scheduler
import com.kado.app.domain.usecase.ReviewCardUseCase
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
    private lateinit var scheduler: Scheduler
    private lateinit var reviewCardUseCase: ReviewCardUseCase

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState

    private var newLimit = 20
    private var summary = SessionSummary(0, 0, 0, 0, 0)

    // Prefetched next card data, ready to display instantly on rate()
    private var prefetchedCard: ReviewCard? = null
    private var prefetchedFront: DisplayableCardContent? = null
    private var prefetchedBack: DisplayableCardContent? = null
    private var prefetchedIntervals: Map<Rating, String> = emptyMap()
    private var hasPrefetch = false
    private var cardShownAt: Long = 0L

    init {
        viewModelScope.launch {
            val deck = repository.getDeck(deckId)
            newLimit = deck?.dailyLimit ?: 20
            scheduler = when (deck?.schedulerType) {
                SchedulerType.FSRS -> {
                    val params = FsrsParameters(
                        desiredRetention = deck.fsrsDesiredRetention,
                        learningStepsSeconds = FsrsParameters.parseStepsString(deck.fsrsLearningSteps),
                        relearningStepsSeconds = FsrsParameters.parseStepsString(deck.fsrsRelearningSteps),
                        maximumInterval = deck.fsrsMaxInterval,
                        enableFuzzing = deck.fsrsEnableFuzzing
                    )
                    FsrsScheduler(params)
                }
                else -> Sm2Scheduler
            }
            reviewCardUseCase = ReviewCardUseCase(repository, scheduler)
            loadNextCard()
        }
    }

    private suspend fun fetchNextCard(excludeCardId: Long = -1): ReviewCard? {
        val now = kotlin.time.Clock.System.now().epochSeconds
        return if (subDeckIndex != null) {
            repository.getNextSubDeckReviewCard(deckId, subDeckIndex, now, newLimit, excludeCardId)
        } else {
            repository.getNextReviewCard(deckId, now, newLimit, excludeCardId)
        }
    }

    private suspend fun loadNextCard() {
        val card = fetchNextCard()
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
            showCard(card)
            prefetchNext(card.card.id)
        }
    }

    private suspend fun showCard(card: ReviewCard) {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val intervals = scheduler.previewIntervals(card.state, now)
        cardShownAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
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

    private suspend fun prefetchNext(currentCardId: Long) {
        val card = fetchNextCard(excludeCardId = currentCardId)
        if (card != null) {
            val now = kotlin.time.Clock.System.now().epochSeconds
            prefetchedCard = card
            prefetchedFront = DisplayableCardContent.from(card.card.front, htmlRenderer)
            prefetchedBack = DisplayableCardContent.from(card.card.back, htmlRenderer)
            prefetchedIntervals = scheduler.previewIntervals(card.state, now)
            hasPrefetch = true
        } else {
            hasPrefetch = false
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

        // Immediately unflip and hide buttons
        _uiState.value = _uiState.value.copy(
            isFlipped = false,
            hasBeenFlipped = false
        )

        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().epochSeconds
            val nowMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val duration = if (cardShownAt > 0) nowMs - cardShownAt else 0L
            val result = reviewCardUseCase(card, rating, now, newLimit, summary, duration)

            newLimit = result.updatedNewLimit
            summary = result.updatedSummary
            delay(400)
            if (hasPrefetch) {
                // Show prefetched card instantly, prefetch next in background
                val nextCard = prefetchedCard!!
                cardShownAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
                _uiState.value = _uiState.value.copy(
                    currentCard = nextCard,
                    frontContent = prefetchedFront,
                    backContent = prefetchedBack,
                    isFlipped = false,
                    hasBeenFlipped = false,
                    isLoading = false,
                    intervals = prefetchedIntervals
                )
                hasPrefetch = false
                prefetchNext(nextCard.card.id)
            } else {
                // No prefetch available — load next
                loadNextCard()
            }
        }
    }
}
