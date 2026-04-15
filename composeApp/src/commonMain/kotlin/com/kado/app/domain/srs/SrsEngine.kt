package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating

object SrsEngine {
    private const val SECONDS_PER_DAY = 86400L
    private const val EASE_MIN = 13
    private const val EASE_MAX = 40
    const val EASE_DEFAULT = 25

    private fun clampInterval(value: Int): Int =
        value.coerceIn(1, 36500)

    private fun clampEase(value: Int): Int =
        value.coerceIn(EASE_MIN, EASE_MAX)

    fun previewIntervals(
        state: CardState,
        nowEpochSeconds: Long
    ): Map<Rating, String> =
        Rating.entries.associateWith { rating ->
            val newState = reviewCard(state, rating, nowEpochSeconds)
            formatInterval(newState.due - nowEpochSeconds)
        }

    private fun formatInterval(seconds: Long): String {
        if (seconds < 60) return "<1m"
        val minutes = seconds / 60
        if (minutes < 60) return "${minutes}m"
        val hours = minutes / 60
        if (hours < 24) return "${hours}h"
        val days = hours / 24
        if (days < 30) return "${days}d"
        val months = days / 30
        return "${months}mo"
    }

    fun reviewCard(state: CardState, rating: Rating, nowEpochSeconds: Long): CardState {
        if (rating == Rating.Again) {
            return state.copy(
                lapses = state.lapses + 1,
                reps = 0,
                interval = 1,
                queue = 1,
                ease = clampEase(state.ease - 2),
                due = nowEpochSeconds + 60
            )
        }

        val easeDelta = when (rating) {
            Rating.Hard -> -1
            Rating.Good -> 0
            Rating.Easy -> 2
            else -> 0
        }
        val newEase = clampEase(state.ease + easeDelta)
        val easeFloat = newEase / 10.0f

        return if (state.queue == 0 || state.reps == 0) {
            when (rating) {
                Rating.Hard -> state.copy(
                    ease = newEase,
                    queue = 2,
                    reps = 1,
                    interval = 1,
                    due = nowEpochSeconds + SECONDS_PER_DAY
                )
                Rating.Good -> state.copy(
                    ease = newEase,
                    queue = 2,
                    reps = 1,
                    interval = 1,
                    due = nowEpochSeconds + SECONDS_PER_DAY
                )
                Rating.Easy -> state.copy(
                    ease = newEase,
                    queue = 2,
                    reps = 1,
                    interval = 4,
                    due = nowEpochSeconds + 4 * SECONDS_PER_DAY
                )
                else -> state.copy(
                    ease = newEase,
                    queue = 2,
                    reps = 1,
                    interval = 1,
                    due = nowEpochSeconds + SECONDS_PER_DAY
                )
            }
        } else {
            val intervalF = state.interval.toFloat()
            val newInterval = when (rating) {
                Rating.Hard -> intervalF * 1.2f
                Rating.Good -> intervalF * easeFloat
                Rating.Easy -> intervalF * easeFloat * 1.3f
                else -> intervalF
            }
            val clampedInterval = clampInterval((newInterval + 0.5f).toInt())
            state.copy(
                ease = newEase,
                reps = state.reps + 1,
                interval = clampedInterval,
                queue = 2,
                due = nowEpochSeconds + clampedInterval * SECONDS_PER_DAY
            )
        }
    }
}
