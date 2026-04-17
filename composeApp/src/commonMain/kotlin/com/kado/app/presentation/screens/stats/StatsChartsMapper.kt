package com.kado.app.presentation.screens.stats

import com.kado.app.domain.model.RatingBreakdown
import com.kado.app.domain.model.StatsSnapshot
import kotlinx.datetime.DayOfWeek

data class ChartBar(val value: Int, val cumulative: Int? = null)

data class ChartData(val bars: List<ChartBar>, val xLabels: Map<Int, String>, val yMax: Int, val yMaxSecondary: Int = 0)

data class StatsChartsState(
    val daily: ChartData,
    val ratingsBars: List<RatingBreakdown>,
    val ratingsXLabels: Map<Int, String>,
    val ratingsYMax: Int,
    val hourly: ChartData,
    val weekly: ChartData
) {
    companion object {
        val EMPTY = StatsChartsState(
            daily = ChartData(emptyList(), emptyMap(), 0, 0),
            ratingsBars = emptyList(),
            ratingsXLabels = emptyMap(),
            ratingsYMax = 0,
            hourly = ChartData(emptyList(), emptyMap(), 0),
            weekly = ChartData(emptyList(), emptyMap(), 0)
        )
    }
}

object StatsChartsMapper {

    private const val DAILY_TICK_COUNT = 6
    private const val HOURLY_TICK_STEP = 6
    private const val DATE_LABEL_TAIL = 5

    fun map(snapshot: StatsSnapshot): StatsChartsState = StatsChartsState(
        daily = mapDaily(snapshot),
        ratingsBars = snapshot.ratings,
        ratingsXLabels = sparseDateLabels(snapshot.ratings.map { it.date.toString() }),
        ratingsYMax = snapshot.ratings.maxOfOrNull { it.total } ?: 0,
        hourly = mapHourly(snapshot),
        weekly = mapWeekly(snapshot)
    )

    private fun mapDaily(snapshot: StatsSnapshot): ChartData {
        val bars = snapshot.dailyReviews.map { ChartBar(it.count, it.cumulative) }
        val labels = sparseDateLabels(snapshot.dailyReviews.map { it.date.toString() })
        return ChartData(
            bars = bars,
            xLabels = labels,
            yMax = bars.maxOfOrNull { it.value } ?: 0,
            yMaxSecondary = bars.maxOfOrNull { it.cumulative ?: 0 } ?: 0
        )
    }

    private fun mapHourly(snapshot: StatsSnapshot): ChartData {
        val bars = snapshot.hourly.map { ChartBar(it.count) }
        val labels = mutableMapOf<Int, String>()
        snapshot.hourly.forEachIndexed { index, bucket ->
            if (bucket.hour % HOURLY_TICK_STEP == 0) {
                labels[index] = "${bucket.hour}h"
            }
        }
        return ChartData(bars, labels, bars.maxOfOrNull { it.value } ?: 0)
    }

    private fun mapWeekly(snapshot: StatsSnapshot): ChartData {
        val bars = snapshot.weekly.map { ChartBar(it.count) }
        val labels = snapshot.weekly
            .mapIndexed { index, bucket -> index to bucket.dayOfWeek.shortLabel() }
            .toMap()
        return ChartData(bars, labels, bars.maxOfOrNull { it.value } ?: 0)
    }

    private fun sparseDateLabels(dates: List<String>): Map<Int, String> {
        if (dates.isEmpty()) return emptyMap()
        val step = (dates.size / DAILY_TICK_COUNT).coerceAtLeast(1)
        val out = mutableMapOf<Int, String>()
        var i = 0
        while (i < dates.size) {
            out[i] = dates[i].takeLast(DATE_LABEL_TAIL) // "MM-DD"
            i += step
        }
        // Always include the last index.
        val last = dates.lastIndex
        if (last !in out) out[last] = dates[last].takeLast(DATE_LABEL_TAIL)
        return out
    }

    private fun DayOfWeek.shortLabel(): String = when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}
