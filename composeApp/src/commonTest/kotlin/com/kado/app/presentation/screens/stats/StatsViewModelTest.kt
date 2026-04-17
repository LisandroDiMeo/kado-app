package com.kado.app.presentation.screens.stats

import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardContent
import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.ReviewEvent
import com.kado.app.domain.model.StatsRange
import com.kado.app.test.fakes.FakeDeckRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val now = 1_749_038_400L

    private fun setUpMain(dispatcher: kotlinx.coroutines.test.TestDispatcher) {
        Dispatchers.setMain(dispatcher)
    }

    private fun tearDownMain() {
        Dispatchers.resetMain()
    }

    private fun seeded(): FakeDeckRepository = FakeDeckRepository().apply {
        decks += Deck(id = 1, name = "Spanish")
        decks += Deck(id = 2, name = "Kanji")
        cards += Card(id = 10, deckId = 1, front = CardContent.PlainText("a"), back = CardContent.PlainText("b"))
        cards += Card(id = 11, deckId = 2, front = CardContent.PlainText("c"), back = CardContent.PlainText("d"))
        cardStates += CardState(cardId = 10, queue = 0)
        cardStates += CardState(cardId = 11, queue = 2, interval = 30, due = now - 1)
    }

    private fun newVm(
        repo: FakeDeckRepository,
        initial: List<Long>,
        dispatcher: kotlinx.coroutines.test.TestDispatcher
    ): StatsViewModel {
        setUpMain(dispatcher)
        return StatsViewModel(
            initialDeckIds = initial,
            repository = repo,
            clock = { now },
            timeZone = kotlinx.datetime.TimeZone.UTC
        )
    }

    @Test
    fun startsWithInitialDeck() = runTest {
        val repo = seeded()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, listOf(1L), dispatcher)

        dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(setOf(1L), state.selectedDeckIds)
        assertEquals("Spanish", state.title)
        assertFalse(state.isAllDecks)
        assertTrue(state.canReset)

        tearDownMain()
    }

    @Test
    fun emptyInitialMeansAllDecks() = runTest {
        val repo = seeded()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, emptyList(), dispatcher)

        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isAllDecks)
        assertEquals("All decks", vm.uiState.value.title)
        assertFalse(vm.uiState.value.canReset)

        tearDownMain()
    }

    @Test
    fun toggleDeckAddsAndRemoves() = runTest {
        val repo = seeded()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, listOf(1L), dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

        vm.toggleDeck(2L)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(setOf(1L, 2L), vm.uiState.value.selectedDeckIds)

        vm.toggleDeck(1L)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(setOf(2L), vm.uiState.value.selectedDeckIds)

        tearDownMain()
    }

    @Test
    fun selectAllDecksClearsSelection() = runTest {
        val repo = seeded()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, listOf(1L), dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

        vm.selectAllDecks()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isAllDecks)

        tearDownMain()
    }

    @Test
    fun setRangeUpdatesSnapshotRange() = runTest {
        val repo = seeded()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, emptyList(), dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

        vm.setRange(StatsRange.Year)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(StatsRange.Year, vm.uiState.value.range)
        assertEquals(365, vm.uiState.value.snapshot.rangeDays)

        tearDownMain()
    }

    @Test
    fun cardStatsReflectSelectedDecks() = runTest {
        val repo = seeded()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, listOf(1L), dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

        val onlyDeck1 = vm.uiState.value.cardStats
        assertEquals(1, onlyDeck1.totalCards)
        assertEquals(1, onlyDeck1.newCards)

        tearDownMain()
    }

    @Test
    fun resetProgressOnlyWhenSingleDeckSelected() = runTest {
        val repo = seeded()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, listOf(1L, 2L), dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

        vm.resetProgress() // should be a no-op
        dispatcher.scheduler.advanceUntilIdle()

        // Both card states still present
        assertEquals(2, repo.cardStates.size)

        tearDownMain()
    }

    @Test
    fun chartsStateIsDerivedFromSnapshot() = runTest {
        val repo = seeded()
        repo.recordReview(
            ReviewEvent(
                cardId = 10, deckId = 1, reviewedAt = now, rating = Rating.Good,
                durationMs = 0, previousInterval = 0, newInterval = 1,
                previousQueue = 0, newQueue = 2
            )
        )
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, listOf(1L), dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

        val charts = vm.uiState.value.charts
        assertEquals(1, charts.daily.yMax)
        assertTrue(charts.weekly.xLabels.isNotEmpty())
        assertEquals(setOf(0, 6, 12, 18), charts.hourly.xLabels.keys)
    }

    @Test
    fun snapshotReflectsRecordedReviews() = runTest {
        val repo = seeded()
        repo.recordReview(
            ReviewEvent(
                cardId = 10, deckId = 1, reviewedAt = now, rating = Rating.Good,
                durationMs = 0, previousInterval = 0, newInterval = 1,
                previousQueue = 0, newQueue = 2
            )
        )
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = newVm(repo, listOf(1L), dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.snapshot.totalReviews)

        tearDownMain()
    }
}
