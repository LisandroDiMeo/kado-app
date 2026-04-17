package com.kado.app.domain.usecase

import com.kado.app.domain.model.DailyReviewPoint
import com.kado.app.domain.model.HeatmapCell
import com.kado.app.domain.model.HourlyBucket
import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.RatingBreakdown
import com.kado.app.domain.model.ReviewLogEntry
import com.kado.app.domain.model.StatsRange
import com.kado.app.domain.model.StatsSnapshot
import com.kado.app.domain.model.WeeklyBucket
import com.kado.app.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class GetStatsSnapshotUseCase(private val repository: DeckRepository) {

    operator fun invoke(
        deckIds: List<Long>,
        range: StatsRange,
        now: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Flow<StatsSnapshot> {
        val today = Instant.fromEpochSeconds(now).toLocalDateTime(timeZone).date
        val fromDate = today.plus(-(range.days - 1), DateTimeUnit.DAY)
        val fromEpoch = fromDate.atStartOfDayIn(timeZone).epochSeconds
        return repository.observeReviewHistory(deckIds, fromEpoch, now).map { entries ->
            buildSnapshot(entries, fromDate, today, range, timeZone)
        }
    }

    private fun buildSnapshot(
        entries: List<ReviewLogEntry>,
        fromDate: LocalDate,
        today: LocalDate,
        range: StatsRange,
        timeZone: TimeZone
    ): StatsSnapshot {
        val allDates = generateDateSequence(fromDate, today)
        val entriesByDate = entries.groupBy {
            Instant.fromEpochSeconds(it.reviewedAt).toLocalDateTime(timeZone).date
        }

        val heatmap = allDates.map { HeatmapCell(it, entriesByDate[it]?.size ?: 0) }

        var running = 0
        val daily = allDates.map { date ->
            val count = entriesByDate[date]?.size ?: 0
            running += count
            DailyReviewPoint(date, count, running)
        }

        val ratings = allDates.map { date ->
            val group = entriesByDate[date].orEmpty()
            RatingBreakdown(
                date = date,
                again = group.count { it.rating == Rating.Again },
                hard = group.count { it.rating == Rating.Hard },
                good = group.count { it.rating == Rating.Good },
                easy = group.count { it.rating == Rating.Easy }
            )
        }

        return StatsSnapshot(
            heatmap = heatmap,
            dailyReviews = daily,
            ratings = ratings,
            hourly = bucketHourly(entries, timeZone),
            weekly = bucketWeekly(entries, timeZone),
            totalReviews = entries.size,
            rangeDays = range.days
        )
    }

    private fun bucketHourly(entries: List<ReviewLogEntry>, timeZone: TimeZone): List<HourlyBucket> {
        val counts = IntArray(HOURS_PER_DAY)
        entries.forEach { entry ->
            val ldt = Instant.fromEpochSeconds(entry.reviewedAt).toLocalDateTime(timeZone)
            counts[ldt.hour] += 1
        }
        return (0 until HOURS_PER_DAY).map { HourlyBucket(it, counts[it]) }
    }

    private fun bucketWeekly(entries: List<ReviewLogEntry>, timeZone: TimeZone): List<WeeklyBucket> {
        val counts = mutableMapOf<DayOfWeek, Int>()
        entries.forEach { entry ->
            val ldt = Instant.fromEpochSeconds(entry.reviewedAt).toLocalDateTime(timeZone)
            val dow = ldt.date.dayOfWeek
            counts[dow] = (counts[dow] ?: 0) + 1
        }
        return DayOfWeek.entries.map { WeeklyBucket(it, counts[it] ?: 0) }
    }

    private fun generateDateSequence(from: LocalDate, to: LocalDate): List<LocalDate> {
        val list = mutableListOf<LocalDate>()
        var cursor = from
        while (cursor <= to) {
            list += cursor
            cursor = cursor.plus(1, DateTimeUnit.DAY)
        }
        return list
    }

    private companion object {
        const val HOURS_PER_DAY = 24
    }
}
