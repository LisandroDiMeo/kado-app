package com.kado.app.domain.usecase

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.ReviewEvent
import com.kado.app.domain.model.SessionSummary
import com.kado.app.domain.repository.DeckRepository
import com.kado.app.domain.srs.Scheduler

data class ReviewResult(val newState: CardState, val updatedNewLimit: Int, val updatedSummary: SessionSummary)

class ReviewCardUseCase(private val repository: DeckRepository, private val scheduler: Scheduler) {

    @Suppress("LongParameterList")
    suspend operator fun invoke(
        card: ReviewCard,
        rating: Rating,
        now: Long,
        currentNewLimit: Int,
        currentSummary: SessionSummary,
        durationMs: Long = 0L
    ): ReviewResult {
        val newState = scheduler.reviewCard(card.state, rating, now)

        val updatedNewLimit = if (card.state.queue == 0) currentNewLimit - 1 else currentNewLimit

        val updatedSummary = currentSummary.copy(
            reviewed = currentSummary.reviewed + 1,
            again = currentSummary.again + if (rating == Rating.Again) 1 else 0,
            hard = currentSummary.hard + if (rating == Rating.Hard) 1 else 0,
            good = currentSummary.good + if (rating == Rating.Good) 1 else 0,
            easy = currentSummary.easy + if (rating == Rating.Easy) 1 else 0
        )

        repository.updateCardState(newState)
        repository.recordReview(
            ReviewEvent(
                cardId = card.card.id,
                deckId = card.card.deckId,
                reviewedAt = now,
                rating = rating,
                durationMs = durationMs,
                previousInterval = card.state.interval,
                newInterval = newState.interval,
                previousQueue = card.state.queue,
                newQueue = newState.queue
            )
        )

        return ReviewResult(
            newState = newState,
            updatedNewLimit = updatedNewLimit,
            updatedSummary = updatedSummary
        )
    }
}
