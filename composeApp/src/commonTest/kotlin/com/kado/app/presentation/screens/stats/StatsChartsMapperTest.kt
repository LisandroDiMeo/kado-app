package com.kado.app.presentation.screens.stats

import com.kado.app.domain.model.DailyReviewPoint
import com.kado.app.domain.model.HeatmapCell
import com.kado.app.domain.model.HourlyBucket
import com.kado.app.domain.model.RatingBreakdown
import com.kado.app.domain.model.StatsSnapshot
import com.kado.app.domain.model.WeeklyBucket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class StatsChartsMapperTest {

    private fun date(daysOffset: Int): LocalDate =
        LocalDate.parse("2026-04-01").let {
            LocalDate.fromEpochDays(it.toEpochDays() + daysOffset)
        }

    private fun buildSnapshot(days: Int): StatsSnapshot {
        val heatmap = (0 until days).map { HeatmapCell(date(it), it) }
        val daily = (0 until days).map {
            DailyReviewPoint(date(it), count = it, cumulative = (0..it).sum())
        }
        val ratings = (0 until days).map {
            RatingBreakdown(date(it), again = 0, hard = 0, good = it, easy = 0)
        }
        val hourly = (0 until 24).map { HourlyBucket(it, count = if (it == 9) 5 else 0) }
        val weekly = DayOfWeek.entries.map { WeeklyBucket(it, count = if (it == DayOfWeek.MONDAY) 3 else 0) }
        return StatsSnapshot(
            heatmap = heatmap,
            dailyReviews = daily,
            ratings = ratings,
            hourly = hourly,
            weekly = weekly,
            totalReviews = daily.sumOf { it.count },
            rangeDays = days
        )
    }

    @Test
    fun mapsDailyYMaxAndCumulativeYMax() {
        val snapshot = buildSnapshot(days = 30)
        val state = StatsChartsMapper.map(snapshot)

        assertEquals(29, state.daily.yMax) // last day's count in buildSnapshot
        assertEquals((0..29).sum(), state.daily.yMaxSecondary)
        assertEquals(30, state.daily.bars.size)
    }

    @Test
    fun dailyXLabelsAreSparseAndIncludeLast() {
        val snapshot = buildSnapshot(days = 30)
        val state = StatsChartsMapper.map(snapshot)

        assertTrue(state.daily.xLabels.size <= 8) // DAILY_TICK_COUNT+1 at most
        assertTrue(29 in state.daily.xLabels.keys)
    }

    @Test
    fun hourlyLabelsAreEverySixHours() {
        val snapshot = buildSnapshot(days = 7)
        val state = StatsChartsMapper.map(snapshot)

        assertEquals(setOf(0, 6, 12, 18), state.hourly.xLabels.keys)
        assertEquals("0h", state.hourly.xLabels[0])
        assertEquals("6h", state.hourly.xLabels[6])
        assertEquals(5, state.hourly.yMax)
    }

    @Test
    fun weeklyLabelsUseShortDayNames() {
        val snapshot = buildSnapshot(days = 7)
        val state = StatsChartsMapper.map(snapshot)

        assertEquals(7, state.weekly.xLabels.size)
        assertTrue(state.weekly.xLabels.values.containsAll(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")))
        assertEquals(3, state.weekly.yMax)
    }

    @Test
    fun ratingsYMaxIsMaxOfTotals() {
        val snapshot = buildSnapshot(days = 5)
        val state = StatsChartsMapper.map(snapshot)

        // good counts are 0,1,2,3,4 => max total = 4
        assertEquals(4, state.ratingsYMax)
    }

    @Test
    fun emptySnapshotProducesEmptyState() {
        val empty = StatsSnapshot.EMPTY
        val state = StatsChartsMapper.map(empty)

        assertEquals(0, state.daily.yMax)
        assertEquals(0, state.ratingsYMax)
        assertEquals(0, state.hourly.yMax)
        assertEquals(0, state.weekly.yMax)
    }
}
