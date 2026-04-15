package com.kado.app.domain.usecase

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Deck
import com.kado.app.test.fakes.FakeDeckRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class CalculateDeckStatsUseCaseTest {

    private val now = 1_000_000L

    private fun cardState(
        cardId: Long,
        queue: Int = 0,
        interval: Int = 0,
        due: Long = 0
    ) = CardState(cardId = cardId, queue = queue, interval = interval, due = due)

    @Test
    fun emptyDeck_allZeros() = runTest {
        val repo = FakeDeckRepository().apply {
            decks.add(Deck(id = 1, name = "Test"))
        }
        val useCase = CalculateDeckStatsUseCase(repo)
        val stats = useCase(deckId = 1, now = now)

        assertEquals(0, stats.totalCards)
        assertEquals(0, stats.newCards)
        assertEquals(0, stats.learningCards)
        assertEquals(0, stats.youngCards)
        assertEquals(0, stats.matureCards)
        assertEquals(0, stats.dueNow)
    }

    @Test
    fun countsNewCards_queue0() {
        val states = listOf(
            cardState(1, queue = 0),
            cardState(2, queue = 0),
            cardState(3, queue = 1)
        )
        val stats = CalculateDeckStatsUseCase.calculateStats(null, 3, states, now)
        assertEquals(2, stats.newCards)
    }

    @Test
    fun countsLearningCards_queue1() {
        val states = listOf(
            cardState(1, queue = 1, due = now - 100),
            cardState(2, queue = 1, due = now + 100),
            cardState(3, queue = 0)
        )
        val stats = CalculateDeckStatsUseCase.calculateStats(null, 3, states, now)
        assertEquals(2, stats.learningCards)
    }

    @Test
    fun countsYoungCards_queue2IntervalBelow21() {
        val states = listOf(
            cardState(1, queue = 2, interval = 5),
            cardState(2, queue = 2, interval = 20),
            cardState(3, queue = 2, interval = 21)
        )
        val stats = CalculateDeckStatsUseCase.calculateStats(null, 3, states, now)
        assertEquals(2, stats.youngCards)
    }

    @Test
    fun countsMatureCards_queue2IntervalAtLeast21() {
        val states = listOf(
            cardState(1, queue = 2, interval = 21),
            cardState(2, queue = 2, interval = 100),
            cardState(3, queue = 2, interval = 20)
        )
        val stats = CalculateDeckStatsUseCase.calculateStats(null, 3, states, now)
        assertEquals(2, stats.matureCards)
    }

    @Test
    fun countsDueCards_queue0AlwaysDue() {
        val states = listOf(
            cardState(1, queue = 0, due = now + 999999), // new cards are always due
            cardState(2, queue = 0)
        )
        val stats = CalculateDeckStatsUseCase.calculateStats(null, 2, states, now)
        assertEquals(2, stats.dueNow)
    }

    @Test
    fun countsDueCards_nonNewOnlyWhenDueLessThanOrEqualNow() {
        val states = listOf(
            cardState(1, queue = 1, due = now - 1), // due (past)
            cardState(2, queue = 1, due = now), // due (exactly now)
            cardState(3, queue = 2, due = now + 1), // not due (future)
            cardState(4, queue = 2, due = now - 100) // due (past)
        )
        val stats = CalculateDeckStatsUseCase.calculateStats(null, 4, states, now)
        assertEquals(3, stats.dueNow)
    }

    @Test
    fun mixedStates_correctCounts() {
        val states = listOf(
            cardState(1, queue = 0), // new + due
            cardState(2, queue = 0), // new + due
            cardState(3, queue = 1, due = now - 10), // learning + due
            cardState(4, queue = 1, due = now + 100), // learning, not due
            cardState(5, queue = 2, interval = 5, due = now - 1), // young + due
            cardState(6, queue = 2, interval = 15, due = now + 100), // young, not due
            cardState(7, queue = 2, interval = 30, due = now - 50), // mature + due
            cardState(8, queue = 2, interval = 25, due = now + 200) // mature, not due
        )
        val stats = CalculateDeckStatsUseCase.calculateStats(null, 8, states, now)

        assertEquals(2, stats.newCards)
        assertEquals(2, stats.learningCards)
        assertEquals(2, stats.youngCards)
        assertEquals(2, stats.matureCards)
        assertEquals(5, stats.dueNow) // 2 new + 1 learning + 1 young + 1 mature
    }

    @Test
    fun fullIntegration_withRepository() = runTest {
        val repo = FakeDeckRepository().apply {
            decks.add(Deck(id = 1, name = "Test Deck", dailyLimit = 20))
            cards.addAll(
                listOf(
                    com.kado.app.domain.model.Card(
                        id = 1,
                        deckId = 1,
                        front = com.kado.app.domain.model.CardContent.PlainText("a"),
                        back = com.kado.app.domain.model.CardContent.PlainText("b")
                    ),
                    com.kado.app.domain.model.Card(
                        id = 2,
                        deckId = 1,
                        front = com.kado.app.domain.model.CardContent.PlainText("c"),
                        back = com.kado.app.domain.model.CardContent.PlainText("d")
                    )
                )
            )
            cardStates.addAll(
                listOf(
                    cardState(1, queue = 0),
                    cardState(2, queue = 2, interval = 30, due = now - 10)
                )
            )
        }

        val useCase = CalculateDeckStatsUseCase(repo)
        val stats = useCase(deckId = 1, now = now)

        assertEquals("Test Deck", stats.deck?.name)
        assertEquals(2, stats.totalCards)
        assertEquals(1, stats.newCards)
        assertEquals(0, stats.learningCards)
        assertEquals(0, stats.youngCards)
        assertEquals(1, stats.matureCards)
        assertEquals(2, stats.dueNow)
    }
}
