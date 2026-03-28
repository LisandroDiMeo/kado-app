package com.kado.app.domain.usecase

import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardContent
import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.SessionSummary
import com.kado.app.test.fakes.FakeDeckRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReviewCardUseCaseTest {

    private val now = 1_000_000L
    private val emptySummary = SessionSummary(0, 0, 0, 0, 0)

    private fun reviewCard(queue: Int = 0, interval: Int = 0, ease: Int = 25) = ReviewCard(
        card = Card(
            id = 1, deckId = 1,
            front = CardContent.PlainText("front"),
            back = CardContent.PlainText("back")
        ),
        state = CardState(
            cardId = 1, due = 0, interval = interval,
            ease = ease, reps = 0, lapses = 0, queue = queue
        )
    )

    @Test
    fun rateNewCard_decrementsNewLimit() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(queue = 0), Rating.Good, now, currentNewLimit = 20, emptySummary)
        assertEquals(19, result.updatedNewLimit)
    }

    @Test
    fun rateReviewCard_doesNotDecrementNewLimit() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(queue = 2, interval = 10), Rating.Good, now, currentNewLimit = 20, emptySummary)
        assertEquals(20, result.updatedNewLimit)
    }

    @Test
    fun rateLearningCard_doesNotDecrementNewLimit() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(queue = 1), Rating.Good, now, currentNewLimit = 15, emptySummary)
        assertEquals(15, result.updatedNewLimit)
    }

    @Test
    fun rateAgain_incrementsAgainCount() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(), Rating.Again, now, 20, emptySummary)
        assertEquals(1, result.updatedSummary.again)
        assertEquals(0, result.updatedSummary.hard)
        assertEquals(0, result.updatedSummary.good)
        assertEquals(0, result.updatedSummary.easy)
    }

    @Test
    fun rateHard_incrementsHardCount() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(), Rating.Hard, now, 20, emptySummary)
        assertEquals(0, result.updatedSummary.again)
        assertEquals(1, result.updatedSummary.hard)
        assertEquals(0, result.updatedSummary.good)
        assertEquals(0, result.updatedSummary.easy)
    }

    @Test
    fun rateGood_incrementsGoodCount() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(), Rating.Good, now, 20, emptySummary)
        assertEquals(1, result.updatedSummary.good)
    }

    @Test
    fun rateEasy_incrementsEasyCount() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(), Rating.Easy, now, 20, emptySummary)
        assertEquals(1, result.updatedSummary.easy)
    }

    @Test
    fun rateCard_incrementsReviewedCount() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(), Rating.Good, now, 20, emptySummary)
        assertEquals(1, result.updatedSummary.reviewed)
    }

    @Test
    fun rateCard_accumulatesSummary() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val existingSummary = SessionSummary(reviewed = 5, again = 1, hard = 1, good = 2, easy = 1)
        val result = useCase(reviewCard(), Rating.Good, now, 20, existingSummary)

        assertEquals(6, result.updatedSummary.reviewed)
        assertEquals(1, result.updatedSummary.again)
        assertEquals(1, result.updatedSummary.hard)
        assertEquals(3, result.updatedSummary.good)
        assertEquals(1, result.updatedSummary.easy)
    }

    @Test
    fun rateCard_persistsNewState() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        useCase(reviewCard(), Rating.Good, now, 20, emptySummary)

        assertEquals(1, repo.updatedCardStates.size)
        assertEquals(1L, repo.updatedCardStates[0].cardId)
    }

    @Test
    fun rateCard_returnsNewState() = runTest {
        val repo = FakeDeckRepository()
        val useCase = ReviewCardUseCase(repo)

        val result = useCase(reviewCard(queue = 0), Rating.Good, now, 20, emptySummary)
        // SrsEngine for new card Good: interval=1, queue=2
        assertEquals(1L, result.newState.cardId)
    }
}
