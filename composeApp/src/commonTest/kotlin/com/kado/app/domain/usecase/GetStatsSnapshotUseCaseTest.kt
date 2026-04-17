package com.kado.app.domain.usecase

import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.ReviewEvent
import com.kado.app.domain.model.StatsRange
import com.kado.app.test.fakes.FakeDeckRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetStatsSnapshotUseCaseTest {

    // A fixed UTC moment: Wed 2025-06-04 12:00:00 UTC (epoch 1749038400)
    private val now = 1_749_038_400L
    private val utc = TimeZone.UTC

    private fun recordReview(repo: FakeDeckRepository, atEpoch: Long, rating: Rating, deckId: Long = 1) {
        val event = ReviewEvent(
            cardId = 1,
            deckId = deckId,
            reviewedAt = atEpoch,
            rating = rating,
            durationMs = 0,
            previousInterval = 0,
            newInterval = 0,
            previousQueue = 0,
            newQueue = 0
        )
        kotlinx.coroutines.runBlocking { repo.recordReview(event) }
    }

    @Test
    fun emptyHistory_yieldsEmptySnapshot() = runTest {
        val repo = FakeDeckRepository()
        val useCase = GetStatsSnapshotUseCase(repo)

        val snapshot = useCase(emptyList(), StatsRange.Days30, now, utc).first()

        assertEquals(0, snapshot.totalReviews)
        assertTrue(snapshot.isEmpty)
        assertEquals(30, snapshot.heatmap.size)
        assertEquals(30, snapshot.dailyReviews.size)
        assertEquals(30, snapshot.ratings.size)
        assertEquals(24, snapshot.hourly.size)
        assertEquals(7, snapshot.weekly.size)
        snapshot.heatmap.forEach { assertEquals(0, it.count) }
    }

    @Test
    fun countsReviewsForToday() = runTest {
        val repo = FakeDeckRepository()
        recordReview(repo, now, Rating.Good)
        recordReview(repo, now - 60, Rating.Again)
        val useCase = GetStatsSnapshotUseCase(repo)

        val snapshot = useCase(emptyList(), StatsRange.Days30, now, utc).first()

        assertEquals(2, snapshot.totalReviews)
        val todayCell = snapshot.heatmap.last()
        assertEquals(2, todayCell.count)
    }

    @Test
    fun cumulativeIsMonotonic() = runTest {
        val repo = FakeDeckRepository()
        recordReview(repo, now - 3 * 86_400, Rating.Good)
        recordReview(repo, now - 1 * 86_400, Rating.Easy)
        recordReview(repo, now, Rating.Hard)
        val useCase = GetStatsSnapshotUseCase(repo)

        val snapshot = useCase(emptyList(), StatsRange.Days30, now, utc).first()

        val cums = snapshot.dailyReviews.map { it.cumulative }
        assertEquals(cums.sorted(), cums)
        assertEquals(3, snapshot.dailyReviews.last().cumulative)
    }

    @Test
    fun ratingBreakdownCountsCorrectly() = runTest {
        val repo = FakeDeckRepository()
        recordReview(repo, now, Rating.Again)
        recordReview(repo, now, Rating.Hard)
        recordReview(repo, now, Rating.Good)
        recordReview(repo, now, Rating.Good)
        recordReview(repo, now, Rating.Easy)
        val useCase = GetStatsSnapshotUseCase(repo)

        val snapshot = useCase(emptyList(), StatsRange.Days30, now, utc).first()
        val today = snapshot.ratings.last()

        assertEquals(1, today.again)
        assertEquals(1, today.hard)
        assertEquals(2, today.good)
        assertEquals(1, today.easy)
        assertEquals(5, today.total)
    }

    @Test
    fun hourlyBucketsReflectReviewTimes() = runTest {
        val repo = FakeDeckRepository()
        // now is 12:00 UTC → hour 12
        recordReview(repo, now, Rating.Good)
        // 09:00 UTC → hour 9
        recordReview(repo, now - 3 * 3600, Rating.Good)
        val useCase = GetStatsSnapshotUseCase(repo)

        val snapshot = useCase(emptyList(), StatsRange.Days30, now, utc).first()

        assertEquals(1, snapshot.hourly[12].count)
        assertEquals(1, snapshot.hourly[9].count)
        assertEquals(0, snapshot.hourly[0].count)
    }

    @Test
    fun weeklyBucketsReflectDayOfWeek() = runTest {
        val repo = FakeDeckRepository()
        // now is Wed 2025-06-04
        recordReview(repo, now, Rating.Good)
        val useCase = GetStatsSnapshotUseCase(repo)

        val snapshot = useCase(emptyList(), StatsRange.Days30, now, utc).first()

        val wednesday = snapshot.weekly.first { it.dayOfWeek == DayOfWeek.WEDNESDAY }
        assertEquals(1, wednesday.count)
    }

    @Test
    fun deckFilterRestrictsResults() = runTest {
        val repo = FakeDeckRepository()
        recordReview(repo, now, Rating.Good, deckId = 1)
        recordReview(repo, now, Rating.Good, deckId = 2)
        val useCase = GetStatsSnapshotUseCase(repo)

        val onlyDeck1 = useCase(listOf(1L), StatsRange.Days30, now, utc).first()
        val all = useCase(emptyList(), StatsRange.Days30, now, utc).first()

        assertEquals(1, onlyDeck1.totalReviews)
        assertEquals(2, all.totalReviews)
    }

    @Test
    fun rangeDaysMatchesSelection() = runTest {
        val repo = FakeDeckRepository()
        val useCase = GetStatsSnapshotUseCase(repo)

        val thirty = useCase(emptyList(), StatsRange.Days30, now, utc).first()
        val ninety = useCase(emptyList(), StatsRange.Days90, now, utc).first()
        val year = useCase(emptyList(), StatsRange.Year, now, utc).first()

        assertEquals(30, thirty.heatmap.size)
        assertEquals(90, ninety.heatmap.size)
        assertEquals(365, year.heatmap.size)
    }

    @Test
    fun heatmapLastDateIsToday() = runTest {
        val repo = FakeDeckRepository()
        val useCase = GetStatsSnapshotUseCase(repo)

        val snapshot = useCase(emptyList(), StatsRange.Days30, now, utc).first()
        val expectedToday = Instant.fromEpochSeconds(now).toLocalDateTime(utc).date

        assertEquals(expectedToday, snapshot.heatmap.last().date)
    }
}
