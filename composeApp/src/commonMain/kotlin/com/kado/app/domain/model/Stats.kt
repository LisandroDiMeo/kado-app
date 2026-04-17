package com.kado.app.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

enum class StatsRange(val days: Int) {
    Days30(30),
    Days90(90),
    Year(365)
}

data class HeatmapCell(val date: LocalDate, val count: Int)

data class DailyReviewPoint(val date: LocalDate, val count: Int, val cumulative: Int)

data class RatingBreakdown(val date: LocalDate, val again: Int, val hard: Int, val good: Int, val easy: Int) {
    val total: Int get() = again + hard + good + easy
}

data class HourlyBucket(val hour: Int, val count: Int)

data class WeeklyBucket(val dayOfWeek: DayOfWeek, val count: Int)

data class StatsSnapshot(
    val heatmap: List<HeatmapCell>,
    val dailyReviews: List<DailyReviewPoint>,
    val ratings: List<RatingBreakdown>,
    val hourly: List<HourlyBucket>,
    val weekly: List<WeeklyBucket>,
    val totalReviews: Int,
    val rangeDays: Int
) {
    val isEmpty: Boolean get() = totalReviews == 0

    companion object {
        val EMPTY = StatsSnapshot(
            heatmap = emptyList(),
            dailyReviews = emptyList(),
            ratings = emptyList(),
            hourly = emptyList(),
            weekly = emptyList(),
            totalReviews = 0,
            rangeDays = 0
        )
    }
}
