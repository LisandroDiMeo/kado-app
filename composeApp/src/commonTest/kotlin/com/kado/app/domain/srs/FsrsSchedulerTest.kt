package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Core algorithm validation tests for the FSRS scheduler.
 * Expected values extracted from the Java reference implementation at
 * https://github.com/open-spaced-repetition/java-fsrs
 */
class FsrsSchedulerTest {

    companion object {
        private const val SECONDS_PER_DAY = 86400L
        private const val BASE_TIME = 1_000_000_000L // epoch seconds baseline

        // FSRS states
        private const val STATE_LEARNING = 1
        private const val STATE_REVIEW = 2
        private const val STATE_RELEARNING = 3

        // Queue mapping
        private const val QUEUE_NEW = 0
        private const val QUEUE_LEARNING = 1
        private const val QUEUE_REVIEW = 2

        // FSRS constants (from default weights)
        private const val DECAY = -0.2 // -w[20] where w[20]=0.2
        private val FACTOR = 0.9.pow(1.0 / DECAY) - 1 // pow(0.9, -5) - 1

        private const val STABILITY_MIN = 0.001
    }

    private fun assertClose(expected: Double, actual: Double, tolerance: Double, message: String = "") {
        assertTrue(
            abs(actual - expected) < tolerance,
            "$message expected=$expected actual=$actual diff=${abs(actual - expected)} tolerance=$tolerance"
        )
    }

    private fun newCard(cardId: Long = 1L) = CardState(cardId = cardId)

    private fun noFuzzParams() = FsrsParameters(enableFuzzing = false)

    private fun noFuzzScheduler() = FsrsScheduler(noFuzzParams())

    /**
     * Interval in days between lastReview and due.
     * For learning cards (due in seconds, not days), this returns 0.
     */
    private fun intervalDays(state: CardState): Int {
        val lastReview = state.lastReview ?: return 0
        val diffSeconds = state.due - lastReview
        return (diffSeconds / SECONDS_PER_DAY).toInt()
    }

    // ── Primary validation: baseline interval sequence ──

    @Test
    fun testBaselineIntervalSequence() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        val ratings = listOf(
            Rating.Good, Rating.Good, Rating.Good, Rating.Good, Rating.Good, Rating.Good,
            Rating.Again, Rating.Again,
            Rating.Good, Rating.Good, Rating.Good, Rating.Good, Rating.Good
        )
        val expectedIntervals = listOf(0, 4, 14, 45, 135, 372, 0, 0, 2, 5, 10, 20, 40)

        val actualIntervals = mutableListOf<Int>()

        for (rating in ratings) {
            card = scheduler.reviewCard(card, rating, now)
            actualIntervals.add(intervalDays(card))
            // Advance to the due date for the next review
            now = card.due
        }

        assertEquals(
            expectedIntervals,
            actualIntervals,
            "Interval sequence mismatch. Expected=$expectedIntervals Actual=$actualIntervals"
        )
    }

    // ── Stability/difficulty precision ──

    @Test
    fun testMemoState() {
        // Fuzzing can be on since we control review times explicitly
        val scheduler = FsrsScheduler()
        var card = newCard()

        val ratings = listOf(
            Rating.Again,
            Rating.Good,
            Rating.Good,
            Rating.Good,
            Rating.Good,
            Rating.Good
        )
        val dayGaps = listOf(0, 0, 1, 3, 8, 21)

        var now = BASE_TIME

        for (i in ratings.indices) {
            now += dayGaps[i] * SECONDS_PER_DAY
            card = scheduler.reviewCard(card, ratings[i], now)
        }

        // One additional GOOD review to finalize state
        card = scheduler.reviewCard(card, Rating.Good, now)

        assertNotNull(card.stability, "stability should not be null after reviews")
        assertNotNull(card.difficulty, "difficulty should not be null after reviews")

        assertClose(49.4472, card.stability!!, 0.0001, "stability")
        assertClose(6.8271, card.difficulty!!, 0.0001, "difficulty")
    }

    // ── Initial stability matches weights[0..3] ──

    @Test
    fun testInitialStability() {
        val scheduler = noFuzzScheduler()
        val weights = FsrsParameters.DEFAULT_WEIGHTS

        val expectedStabilities = mapOf(
            Rating.Again to weights[0], // 0.2172
            Rating.Hard to weights[1], // 1.1771
            Rating.Good to weights[2], // 3.2602
            Rating.Easy to weights[3] // 16.1507
        )

        for ((rating, expectedStability) in expectedStabilities) {
            val card = newCard()
            val result = scheduler.reviewCard(card, rating, BASE_TIME)
            assertNotNull(result.stability, "stability should be set after first review with $rating")
            assertClose(
                expectedStability,
                result.stability!!,
                0.0001,
                "initial stability for $rating"
            )
        }
    }

    // ── Initial difficulty ──

    @Test
    fun testInitialDifficulty() {
        val scheduler = noFuzzScheduler()
        val w = FsrsParameters.DEFAULT_WEIGHTS

        // Formula: w[4] - e^(w[5]*(fsrsRating-1)) + 1, clamped [1, 10]
        // fsrsRating: Again=1, Hard=2, Good=3, Easy=4
        val expectedDifficulties = mapOf(
            Rating.Again to (w[4] - exp(w[5] * (1.0 - 1)) + 1).coerceIn(1.0, 10.0),
            Rating.Hard to (w[4] - exp(w[5] * (2.0 - 1)) + 1).coerceIn(1.0, 10.0),
            Rating.Good to (w[4] - exp(w[5] * (3.0 - 1)) + 1).coerceIn(1.0, 10.0),
            Rating.Easy to (w[4] - exp(w[5] * (4.0 - 1)) + 1).coerceIn(1.0, 10.0)
        )

        for ((rating, expectedDifficulty) in expectedDifficulties) {
            val card = newCard()
            val result = scheduler.reviewCard(card, rating, BASE_TIME)
            assertNotNull(result.difficulty, "difficulty should be set after first review with $rating")
            assertClose(
                expectedDifficulty,
                result.difficulty!!,
                0.0001,
                "initial difficulty for $rating"
            )
        }
    }

    // ── Repeated EASY reaches minimum difficulty ──

    @Test
    fun testRepeatedEasyMinDifficulty() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        for (i in 0 until 10) {
            // Use slightly different timestamps to avoid same-instant issues
            card = scheduler.reviewCard(card, Rating.Easy, now + i)
            // If in review state, advance to due date
            if (card.fsrsState == STATE_REVIEW) {
                now = card.due
            } else {
                now = card.due
            }
        }

        assertNotNull(card.difficulty, "difficulty should be set after reviews")
        assertEquals(1.0, card.difficulty!!, "difficulty should reach minimum 1.0 after many EASY reviews")
    }

    // ── Retrievability formula ──

    @Test
    fun testRetrievability() {
        // For a card with known stability reviewed some days ago
        val stability = 10.0
        val elapsedDays = 5.0

        // Formula: (1 + FACTOR * days/stability)^DECAY
        val expectedRetrievability = (1.0 + FACTOR * elapsedDays / stability).pow(DECAY)

        // Verify the formula gives a reasonable value between 0 and 1
        assertTrue(expectedRetrievability > 0.0, "retrievability should be > 0")
        assertTrue(expectedRetrievability < 1.0, "retrievability should be < 1")

        // At 0 elapsed days, retrievability should be ~1.0
        val atZeroDays = (1.0 + FACTOR * 0.0 / stability).pow(DECAY)
        assertClose(1.0, atZeroDays, 0.0001, "retrievability at 0 days")

        // At stability days, retrievability should be ~0.9 (desired retention)
        val atStabilityDays = (1.0 + FACTOR * stability / stability).pow(DECAY)
        assertClose(0.9, atStabilityDays, 0.0001, "retrievability at stability days")

        // Verify FACTOR value: pow(0.9, -5) - 1
        val expectedFactor = 0.9.pow(-5.0) - 1
        assertClose(expectedFactor, FACTOR, 0.0001, "FACTOR constant")
    }

    // ── Learning step progression ──

    @Test
    fun testLearningStepProgression_goodAdvancesStep() {
        val scheduler = FsrsScheduler() // default params: learning steps = [60s, 600s]
        var card = newCard()

        // First GOOD: step 0 -> step 1, due in 600s (10 minutes)
        card = scheduler.reviewCard(card, Rating.Good, BASE_TIME)

        assertEquals(STATE_LEARNING, card.fsrsState, "should be in LEARNING state after first GOOD")
        assertEquals(1, card.step, "step should advance to 1 after first GOOD")
        // Due should be approximately BASE_TIME + 600s
        val expectedDue = BASE_TIME + 600L
        assertClose(
            expectedDue.toDouble(),
            card.due.toDouble(),
            5.0,
            "due should be ~10 minutes from now"
        )
    }

    @Test
    fun testLearningStepProgression_goodGraduatesToReview() {
        val scheduler = FsrsScheduler()
        var card = newCard()

        // First GOOD: step 0 -> step 1
        card = scheduler.reviewCard(card, Rating.Good, BASE_TIME)
        assertEquals(STATE_LEARNING, card.fsrsState)
        assertEquals(1, card.step)

        // Second GOOD: step 1 -> graduates to REVIEW
        card = scheduler.reviewCard(card, Rating.Good, card.due)
        assertEquals(STATE_REVIEW, card.fsrsState, "should graduate to REVIEW after completing all learning steps")
        assertNull(card.step, "step should be null after graduating")
    }

    @Test
    fun testLearningStepProgression_againResetsToStepZero() {
        val scheduler = FsrsScheduler()
        var card = newCard()

        // First GOOD: step 0 -> step 1
        card = scheduler.reviewCard(card, Rating.Good, BASE_TIME)
        assertEquals(1, card.step)

        // AGAIN: back to step 0, due = now + 60s
        val nowAtStep1 = card.due
        card = scheduler.reviewCard(card, Rating.Again, nowAtStep1)
        assertEquals(STATE_LEARNING, card.fsrsState, "should remain in LEARNING after AGAIN")
        assertEquals(0, card.step, "step should reset to 0 after AGAIN")
        assertClose(
            (nowAtStep1 + 60L).toDouble(),
            card.due.toDouble(),
            5.0,
            "due should be ~1 minute from now after AGAIN"
        )
    }

    @Test
    fun testLearningStepProgression_easyGraduatesImmediately() {
        val scheduler = FsrsScheduler()
        val card = newCard()

        // EASY on step 0: immediately graduates to REVIEW
        val result = scheduler.reviewCard(card, Rating.Easy, BASE_TIME)
        assertEquals(STATE_REVIEW, result.fsrsState, "EASY should immediately graduate to REVIEW")
        assertNull(result.step, "step should be null after EASY graduation")
        // Due should be at least 1 day out
        assertTrue(
            result.due >= BASE_TIME + SECONDS_PER_DAY,
            "due should be at least 1 day from now after EASY graduation"
        )
    }

    @Test
    fun testLearningStepProgression_hardStaysAtStep() {
        val scheduler = FsrsScheduler()
        val card = newCard()

        // HARD on step 0: stays at step 0
        val result = scheduler.reviewCard(card, Rating.Hard, BASE_TIME)
        assertEquals(STATE_LEARNING, result.fsrsState, "HARD should keep card in LEARNING")
        assertEquals(0, result.step, "step should stay at 0 after HARD")

        // Due should be between first step and some reasonable upper bound
        // Java reference: HARD on step 0 with 2 steps -> average of steps[0] and steps[1] = (60+600)/2 = 330s
        // Actually from Java test: rounded to 33 (in 10s units), so 330 seconds
        val dueDelta = result.due - BASE_TIME
        assertTrue(
            dueDelta > 0,
            "due should be in the future after HARD"
        )
    }

    // ── Relearning step progression ──

    @Test
    fun testRelearningStepProgression() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        // Build up to REVIEW state: GOOD, GOOD, GOOD
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due

        assertEquals(STATE_REVIEW, card.fsrsState, "should be in REVIEW state")

        // Rate AGAIN -> enters RELEARNING
        card = scheduler.reviewCard(card, Rating.Again, now)
        assertEquals(STATE_RELEARNING, card.fsrsState, "should enter RELEARNING after AGAIN on REVIEW card")
        assertEquals(0, card.step, "step should be 0 in RELEARNING")

        // Rate GOOD -> completes relearning, returns to REVIEW
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        assertEquals(STATE_REVIEW, card.fsrsState, "should return to REVIEW after completing relearning")
        assertNull(card.step, "step should be null after completing relearning")
    }

    @Test
    fun testRelearningAgainResetsStep() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        // Build up to REVIEW state
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due

        // Enter RELEARNING
        card = scheduler.reviewCard(card, Rating.Again, now)
        assertEquals(STATE_RELEARNING, card.fsrsState)
        assertEquals(0, card.step)

        // AGAIN again -> stays at step 0
        now = card.due
        card = scheduler.reviewCard(card, Rating.Again, now)
        assertEquals(STATE_RELEARNING, card.fsrsState, "should remain in RELEARNING")
        assertEquals(0, card.step, "step should remain at 0 after AGAIN in RELEARNING")
    }

    // ── Maximum interval clamping ──

    @Test
    fun testMaximumIntervalClamping() {
        val maxInterval = 100
        val params = FsrsParameters(enableFuzzing = false, maximumInterval = maxInterval)
        val scheduler = FsrsScheduler(params)
        var card = newCard()
        var now = BASE_TIME

        // Do many EASY reviews to push interval high
        for (i in 0 until 10) {
            card = scheduler.reviewCard(card, Rating.Easy, now)
            now = card.due
            val ivl = intervalDays(card)
            assertTrue(
                ivl <= maxInterval,
                "interval ($ivl) should not exceed maximum ($maxInterval) at review $i"
            )
        }
    }

    @Test
    fun testMaximumIntervalClampingWithGood() {
        val maxInterval = 100
        val params = FsrsParameters(enableFuzzing = false, maximumInterval = maxInterval)
        val scheduler = FsrsScheduler(params)
        var card = newCard()
        var now = BASE_TIME

        // Build up with GOOD reviews
        for (i in 0 until 15) {
            card = scheduler.reviewCard(card, Rating.Good, now)
            now = card.due
            if (card.fsrsState == STATE_REVIEW) {
                val ivl = intervalDays(card)
                assertTrue(
                    ivl <= maxInterval,
                    "interval ($ivl) should not exceed maximum ($maxInterval) at review $i"
                )
            }
        }
    }

    // ── Stability lower bound ──

    @Test
    fun testStabilityLowerBound() {
        val scheduler = FsrsScheduler()
        var card = newCard()
        var now = BASE_TIME

        // Rate AGAIN many times, advancing by 1 day each time
        for (i in 0 until 100) {
            card = scheduler.reviewCard(card, Rating.Again, now)
            now = card.due + SECONDS_PER_DAY // advance past due + 1 day

            assertNotNull(card.stability, "stability should not be null at iteration $i")
            assertTrue(
                card.stability!! >= STABILITY_MIN,
                "stability (${card.stability}) should never drop below $STABILITY_MIN at iteration $i"
            )
        }
    }

    // ── Queue mapping ──

    @Test
    fun testQueueMappingForStates() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        // New card starts with queue=0
        assertEquals(QUEUE_NEW, card.queue, "new card should have queue=0")

        // After first review (learning), queue should be 1
        card = scheduler.reviewCard(card, Rating.Good, now)
        if (card.fsrsState == STATE_LEARNING) {
            assertEquals(QUEUE_LEARNING, card.queue, "learning card should have queue=1")
        }

        // Graduate to review
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        if (card.fsrsState == STATE_REVIEW) {
            assertEquals(QUEUE_REVIEW, card.queue, "review card should have queue=2")
        }

        // AGAIN -> relearning, queue should be 1
        now = card.due
        card = scheduler.reviewCard(card, Rating.Again, now)
        if (card.fsrsState == STATE_RELEARNING) {
            assertEquals(QUEUE_LEARNING, card.queue, "relearning card should have queue=1")
        }
    }

    // ── lastReview tracking ──

    @Test
    fun testLastReviewIsUpdated() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        val reviewTime = BASE_TIME

        card = scheduler.reviewCard(card, Rating.Good, reviewTime)
        assertEquals(reviewTime, card.lastReview, "lastReview should be set to the review time")

        val secondReviewTime = card.due
        card = scheduler.reviewCard(card, Rating.Good, secondReviewTime)
        assertEquals(secondReviewTime, card.lastReview, "lastReview should update to second review time")
    }

    // ── Reps and lapses tracking ──

    @Test
    fun testRepsIncrement() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        for (i in 1..5) {
            card = scheduler.reviewCard(card, Rating.Good, now)
            now = card.due
            assertEquals(i, card.reps, "reps should be $i after $i reviews")
        }
    }

    @Test
    fun testLapsesIncrementOnAgain() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        // Build up to REVIEW state
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due

        val lapsesBeforeAgain = card.lapses

        // Rate AGAIN from REVIEW state
        card = scheduler.reviewCard(card, Rating.Again, now)
        assertEquals(
            lapsesBeforeAgain + 1,
            card.lapses,
            "lapses should increment by 1 after AGAIN on a review card"
        )
    }

    // ── CardId preservation ──

    @Test
    fun testCardIdPreserved() {
        val scheduler = noFuzzScheduler()
        val card = newCard(cardId = 42L)
        val result = scheduler.reviewCard(card, Rating.Good, BASE_TIME)
        assertEquals(42L, result.cardId, "cardId should be preserved through review")
    }
}
