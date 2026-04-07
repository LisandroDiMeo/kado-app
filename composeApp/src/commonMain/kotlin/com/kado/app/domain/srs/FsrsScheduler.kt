package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import kotlin.math.E
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * FSRS (Free Spaced Repetition Scheduler) implementation.
 * Ported from the Java reference at https://github.com/open-spaced-repetition/java-fsrs
 *
 * State mapping: 1=LEARNING, 2=REVIEW, 3=RELEARNING
 */
class FsrsScheduler(
    private val params: FsrsParameters = FsrsParameters()
) : Scheduler {

    companion object {
        private const val SECONDS_PER_DAY = 86400L
        private const val STABILITY_MIN = 0.001
        private const val MIN_DIFFICULTY = 1.0
        private const val MAX_DIFFICULTY = 10.0

        // FSRS state constants
        private const val STATE_LEARNING = 1
        private const val STATE_REVIEW = 2
        private const val STATE_RELEARNING = 3
    }

    private data class FuzzRange(val start: Double, val end: Double, val factor: Double)

    private val fuzzRanges = arrayOf(
        FuzzRange(2.5, 7.0, 0.15),
        FuzzRange(7.0, 20.0, 0.1),
        FuzzRange(20.0, Double.POSITIVE_INFINITY, 0.05)
    )

    private val decay: Double = -params.weights[20]
    private val factor: Double = 0.9.pow(1.0 / decay) - 1
    private val random: Random = Random(params.randomSeed)

    /** Map Kado Rating (0-3) to FSRS rating value (1-4) */
    private val Rating.fsrsValue: Int get() = this.value + 1

    // --- Core FSRS math functions ---

    private fun clampStability(stability: Double): Double =
        max(stability, STABILITY_MIN)

    private fun initialStability(rating: Rating): Double =
        clampStability(params.weights[rating.fsrsValue - 1])

    private fun clampDifficulty(difficulty: Double): Double =
        min(max(difficulty, MIN_DIFFICULTY), MAX_DIFFICULTY)

    private fun initialDifficulty(rating: Rating): Double {
        val d = params.weights[4] - E.pow(params.weights[5] * (rating.fsrsValue - 1)) + 1
        return clampDifficulty(d)
    }

    private fun shortTermStability(stability: Double, rating: Rating): Double {
        var increase = exp(params.weights[17] * (rating.fsrsValue - 3 + params.weights[18])) *
                stability.pow(-params.weights[19])

        if (rating == Rating.Good || rating == Rating.Easy) {
            increase = max(increase, 1.0)
        }

        return clampStability(stability * increase)
    }

    private fun linearDamping(deltaDifficulty: Double, difficulty: Double): Double =
        (10.0 - difficulty) * deltaDifficulty / 9.0

    private fun meanReversion(arg1: Double, arg2: Double): Double =
        params.weights[7] * arg1 + (1 - params.weights[7]) * arg2

    private fun nextDifficulty(difficulty: Double, rating: Rating): Double {
        val arg1 = initialDifficulty(Rating.Easy)
        val deltaDifficulty = -(params.weights[6] * (rating.fsrsValue - 3))
        val arg2 = difficulty + linearDamping(deltaDifficulty, difficulty)
        return clampDifficulty(meanReversion(arg1, arg2))
    }

    private fun nextForgetStability(difficulty: Double, stability: Double, retrievability: Double): Double {
        val longTermParams = params.weights[11] *
                difficulty.pow(-params.weights[12]) *
                ((stability + 1).pow(params.weights[13]) - 1) *
                exp((1 - retrievability) * params.weights[14])

        val shortTermParams = stability / exp(params.weights[17] * params.weights[18])

        return min(longTermParams, shortTermParams)
    }

    private fun nextRecallStability(
        difficulty: Double, stability: Double, retrievability: Double, rating: Rating
    ): Double {
        val hardPenalty = if (rating == Rating.Hard) params.weights[15] else 1.0
        val easyBonus = if (rating == Rating.Easy) params.weights[16] else 1.0

        return stability * (1 +
                exp(params.weights[8]) *
                (11 - difficulty) *
                stability.pow(-params.weights[9]) *
                (exp((1 - retrievability) * params.weights[10]) - 1) *
                hardPenalty *
                easyBonus)
    }

    private fun nextStability(
        difficulty: Double, stability: Double, retrievability: Double, rating: Rating
    ): Double {
        val ns = if (rating == Rating.Again) {
            nextForgetStability(difficulty, stability, retrievability)
        } else {
            nextRecallStability(difficulty, stability, retrievability, rating)
        }
        return clampStability(ns)
    }

    private fun nextInterval(stability: Double): Int {
        var ni = ((stability / factor) * (params.desiredRetention.pow(1.0 / decay) - 1)).roundToLong().toInt()
        ni = max(ni, 1)
        ni = min(ni, params.maximumInterval)
        return ni
    }

    private fun getCardRetrievability(
        stability: Double, lastReview: Long, nowEpochSeconds: Long
    ): Double {
        val elapsedDays = max(0, (nowEpochSeconds - lastReview) / SECONDS_PER_DAY).toInt()
        return (1 + factor * elapsedDays / stability).pow(decay)
    }

    // --- Fuzzing ---

    private fun getFuzzRange(intervalDays: Int): IntArray {
        var delta = 1.0
        for (fr in fuzzRanges) {
            delta += fr.factor * max(min(intervalDays.toDouble(), fr.end - fr.start), 0.0)
        }

        var minIvl = (intervalDays - delta).roundToInt()
        var maxIvl = (intervalDays + delta).roundToInt()

        minIvl = max(2, minIvl)
        maxIvl = min(maxIvl, params.maximumInterval)
        minIvl = min(minIvl, maxIvl)

        return intArrayOf(minIvl, maxIvl)
    }

    private fun getFuzzedIntervalSeconds(intervalSeconds: Long): Long {
        val intervalDays = (intervalSeconds / SECONDS_PER_DAY).toInt()

        // Match Java: if (intervalDays < 2.5) return interval
        // intervalDays is int, so < 2.5 means <= 2
        if (intervalDays < 2.5) return intervalSeconds

        val bounds = getFuzzRange(intervalDays)
        val minIvl = bounds[0]
        val maxIvl = bounds[1]

        val fuzzedDouble = random.nextDouble() * (maxIvl - minIvl + 1) + minIvl
        val fuzzedDays = min(fuzzedDouble.roundToLong().toInt(), params.maximumInterval)

        return fuzzedDays.toLong() * SECONDS_PER_DAY
    }

    // --- Interval formatting (matches SrsEngine) ---

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

    // --- Queue mapping ---

    private fun stateToQueue(fsrsState: Int?): Int = when (fsrsState) {
        STATE_LEARNING, STATE_RELEARNING -> 1
        STATE_REVIEW -> 2
        else -> 0
    }

    // --- Main reviewCard ---

    override fun reviewCard(state: CardState, rating: Rating, nowEpochSeconds: Long): CardState {
        // Work with mutable local variables
        var stability = state.stability
        var difficulty = state.difficulty
        var fsrsState = state.fsrsState
        var step = state.step

        val daysSinceLastReview: Long? = if (state.lastReview != null) {
            (nowEpochSeconds - state.lastReview) / SECONDS_PER_DAY
        } else null

        var nextIntervalSeconds = 0L

        // Determine current state: if fsrsState is null and stability is null, it's a first review (LEARNING)
        val currentState = if (fsrsState == null && stability == null) {
            STATE_LEARNING
        } else {
            fsrsState ?: STATE_LEARNING
        }

        // Initialize step for LEARNING if not set
        if (currentState == STATE_LEARNING && step == null) {
            step = 0
        }

        when (currentState) {
            STATE_LEARNING -> {
                // Update stability and difficulty
                if (stability == null && difficulty == null) {
                    // First review ever
                    stability = initialStability(rating)
                    difficulty = initialDifficulty(rating)
                } else if (daysSinceLastReview != null && daysSinceLastReview < 1) {
                    // Same-day re-review
                    stability = shortTermStability(stability!!, rating)
                    difficulty = nextDifficulty(difficulty!!, rating)
                } else {
                    // Long-term review
                    val retrievability = getCardRetrievability(stability!!, state.lastReview!!, nowEpochSeconds)
                    stability = nextStability(difficulty!!, stability, retrievability, rating)
                    difficulty = nextDifficulty(difficulty, rating)
                }

                // Calculate next interval
                val learningSteps = params.learningStepsSeconds
                if (learningSteps.isEmpty() ||
                    (step!! >= learningSteps.size && (rating == Rating.Hard || rating == Rating.Good || rating == Rating.Easy))
                ) {
                    // Graduate to REVIEW
                    fsrsState = STATE_REVIEW
                    step = null
                    val intervalDays = nextInterval(stability!!)
                    nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                } else {
                    fsrsState = STATE_LEARNING
                    when (rating) {
                        Rating.Again -> {
                            step = 0
                            nextIntervalSeconds = learningSteps[step!!]
                        }
                        Rating.Hard -> {
                            // step stays the same
                            nextIntervalSeconds = if (step == 0 && learningSteps.size == 1) {
                                (learningSteps[0] * 1.5).roundToLong()
                            } else if (step == 0) {
                                ((learningSteps[0] + learningSteps[1]) / 2.0).roundToLong()
                            } else {
                                learningSteps[step!!]
                            }
                        }
                        Rating.Good -> {
                            if (step!! + 1 == learningSteps.size) {
                                // Last step -> graduate to REVIEW
                                fsrsState = STATE_REVIEW
                                step = null
                                val intervalDays = nextInterval(stability!!)
                                nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                            } else {
                                step = step!! + 1
                                nextIntervalSeconds = learningSteps[step!!]
                            }
                        }
                        Rating.Easy -> {
                            fsrsState = STATE_REVIEW
                            step = null
                            val intervalDays = nextInterval(stability!!)
                            nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                        }
                    }
                }
            }

            STATE_REVIEW -> {
                // Update stability and difficulty
                if (daysSinceLastReview != null && daysSinceLastReview < 1) {
                    stability = shortTermStability(stability!!, rating)
                } else {
                    val retrievability = getCardRetrievability(stability!!, state.lastReview!!, nowEpochSeconds)
                    stability = nextStability(difficulty!!, stability, retrievability, rating)
                }
                difficulty = nextDifficulty(difficulty!!, rating)

                // Calculate next interval
                when (rating) {
                    Rating.Again -> {
                        val relearningSteps = params.relearningStepsSeconds
                        if (relearningSteps.isEmpty()) {
                            val intervalDays = nextInterval(stability!!)
                            nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                        } else {
                            fsrsState = STATE_RELEARNING
                            step = 0
                            nextIntervalSeconds = relearningSteps[step!!]
                        }
                    }
                    Rating.Hard, Rating.Good, Rating.Easy -> {
                        val intervalDays = nextInterval(stability!!)
                        nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                    }
                }
            }

            STATE_RELEARNING -> {
                // Update stability and difficulty
                if (daysSinceLastReview != null && daysSinceLastReview < 1) {
                    stability = shortTermStability(stability!!, rating)
                } else {
                    val retrievability = getCardRetrievability(stability!!, state.lastReview!!, nowEpochSeconds)
                    stability = nextStability(difficulty!!, stability, retrievability, rating)
                }
                difficulty = nextDifficulty(difficulty!!, rating)

                // Initialize step if null
                if (step == null) step = 0

                // Calculate next interval
                val relearningSteps = params.relearningStepsSeconds
                if (relearningSteps.isEmpty() ||
                    (step!! >= relearningSteps.size && (rating == Rating.Hard || rating == Rating.Good || rating == Rating.Easy))
                ) {
                    // Graduate back to REVIEW
                    fsrsState = STATE_REVIEW
                    step = null
                    val intervalDays = nextInterval(stability!!)
                    nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                } else {
                    when (rating) {
                        Rating.Again -> {
                            step = 0
                            nextIntervalSeconds = relearningSteps[step!!]
                        }
                        Rating.Hard -> {
                            // step stays the same
                            nextIntervalSeconds = if (step == 0 && relearningSteps.size == 1) {
                                (relearningSteps[0] * 1.5).roundToLong()
                            } else if (step == 0) {
                                // Java reference uses learningSteps here (matches the original code)
                                ((params.learningStepsSeconds[0] + params.learningStepsSeconds[1]) / 2.0).roundToLong()
                            } else {
                                relearningSteps[step!!]
                            }
                        }
                        Rating.Good -> {
                            if (step!! + 1 == relearningSteps.size) {
                                // Last step -> graduate back to REVIEW
                                fsrsState = STATE_REVIEW
                                step = null
                                val intervalDays = nextInterval(stability!!)
                                nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                            } else {
                                step = step!! + 1
                                nextIntervalSeconds = relearningSteps[step!!]
                            }
                        }
                        Rating.Easy -> {
                            fsrsState = STATE_REVIEW
                            step = null
                            val intervalDays = nextInterval(stability!!)
                            nextIntervalSeconds = intervalDays.toLong() * SECONDS_PER_DAY
                        }
                    }
                }
            }
        }

        // Apply fuzzing for REVIEW state
        if (params.enableFuzzing && fsrsState == STATE_REVIEW) {
            nextIntervalSeconds = getFuzzedIntervalSeconds(nextIntervalSeconds)
        }

        val due = nowEpochSeconds + nextIntervalSeconds
        val intervalDays = (nextIntervalSeconds / SECONDS_PER_DAY).toInt()

        return state.copy(
            due = due,
            interval = intervalDays,
            reps = state.reps + 1,
            lapses = if (rating == Rating.Again) state.lapses + 1 else state.lapses,
            queue = stateToQueue(fsrsState),
            stability = stability,
            difficulty = difficulty,
            fsrsState = fsrsState,
            step = step,
            lastReview = nowEpochSeconds
        )
    }

    override fun previewIntervals(state: CardState, nowEpochSeconds: Long): Map<Rating, String> {
        return Rating.entries.associateWith { rating ->
            val newState = reviewCard(state, rating, nowEpochSeconds)
            formatInterval(newState.due - nowEpochSeconds)
        }
    }
}
