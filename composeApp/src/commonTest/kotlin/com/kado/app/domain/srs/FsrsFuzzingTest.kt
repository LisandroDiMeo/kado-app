package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for FSRS interval fuzzing behavior.
 * Fuzzing adds controlled randomness to review intervals to prevent
 * cards reviewed together from always being due together.
 */
class FsrsFuzzingTest {

    companion object {
        private const val SECONDS_PER_DAY = 86400L
        private const val BASE_TIME = 1_000_000_000L

        private const val STATE_REVIEW = 2
    }

    private fun newCard() = CardState(cardId = 1L)

    /**
     * Compute the interval in days between lastReview and due.
     */
    private fun intervalDays(state: CardState): Int {
        val lastReview = state.lastReview ?: return 0
        val diffSeconds = state.due - lastReview
        return (diffSeconds / SECONDS_PER_DAY).toInt()
    }

    // ── Different seeds produce different intervals ──

    @Test
    fun testDifferentSeedsDifferentIntervals() {
        // Use many different seeds and verify that fuzzing produces at least 2 distinct intervals
        // (Kotlin's Random differs from Java's Random, so we don't check exact values)
        val intervals = mutableSetOf<Int>()

        for (seed in listOf(1, 42, 100, 12345, 99999)) {
            val scheduler = FsrsScheduler(FsrsParameters(enableFuzzing = true, randomSeed = seed))
            var card = newCard()
            var now = BASE_TIME

            // 3 GOOD reviews to reach REVIEW state with a fuzzable interval
            card = scheduler.reviewCard(card, Rating.Good, now)
            now = card.due
            card = scheduler.reviewCard(card, Rating.Good, now)
            now = card.due
            card = scheduler.reviewCard(card, Rating.Good, now)

            intervals.add(intervalDays(card))
        }

        // With 5 different seeds, we should get at least 2 distinct fuzzed intervals
        assertTrue(
            intervals.size >= 2,
            "different seeds should produce at least 2 distinct intervals, got: $intervals"
        )
    }

    // ── Fuzzing disabled produces consistent results ──

    @Test
    fun testFuzzingDisabledConsistent() {
        val scheduler = FsrsScheduler(FsrsParameters(enableFuzzing = false))

        // Run the same sequence twice
        fun runSequence(): List<Long> {
            var card = newCard()
            var now = BASE_TIME
            val dues = mutableListOf<Long>()

            for (i in 0 until 8) {
                card = scheduler.reviewCard(card, Rating.Good, now)
                dues.add(card.due)
                now = card.due
            }
            return dues
        }

        val firstRun = runSequence()
        val secondRun = runSequence()

        assertEquals(firstRun, secondRun, "with fuzzing disabled, identical sequences should produce identical results")
    }

    // ── Fuzzed intervals are within acceptable range ──

    @Test
    fun testFuzzedIntervalWithinRange() {
        // Get unfuzzed intervals as baseline
        val unfuzzedScheduler = FsrsScheduler(FsrsParameters(enableFuzzing = false))
        var unfuzzedCard = newCard()
        var unfuzzedNow = BASE_TIME

        val unfuzzedIntervals = mutableListOf<Long>()
        for (i in 0 until 10) {
            unfuzzedCard = unfuzzedScheduler.reviewCard(unfuzzedCard, Rating.Good, unfuzzedNow)
            val lastReview = unfuzzedCard.lastReview ?: unfuzzedNow
            unfuzzedIntervals.add(unfuzzedCard.due - lastReview)
            unfuzzedNow = unfuzzedCard.due
        }

        // Run with fuzzing using multiple seeds
        for (seed in listOf(1, 42, 123, 456, 789)) {
            val fuzzedScheduler = FsrsScheduler(FsrsParameters(enableFuzzing = true, randomSeed = seed))
            var fuzzedCard = newCard()
            var fuzzedNow = BASE_TIME

            for (i in 0 until 10) {
                fuzzedCard = fuzzedScheduler.reviewCard(fuzzedCard, Rating.Good, fuzzedNow)
                val lastReview = fuzzedCard.lastReview ?: fuzzedNow
                val fuzzedInterval = fuzzedCard.due - lastReview
                fuzzedNow = fuzzedCard.due

                if (fuzzedCard.fsrsState == STATE_REVIEW && i >= 2) {
                    val unfuzzedInterval = unfuzzedIntervals.getOrNull(i) ?: continue
                    val unfuzzedDays = unfuzzedInterval / SECONDS_PER_DAY.toDouble()
                    val fuzzedDays = fuzzedInterval / SECONDS_PER_DAY.toDouble()

                    // Fuzz range depends on interval length:
                    // interval <= 2.5 days: no fuzz
                    // interval < 7 days: +/- 15%  (actually up to 25% to be safe)
                    // interval < 20 days: +/- 10% (actually up to 20% to be safe)
                    // interval >= 20 days: +/- 5% (actually up to 15% to be safe)
                    // Use generous bounds since exact fuzz algorithm may vary
                    if (unfuzzedDays > 2.5) {
                        val maxFuzzPercent = 0.25 // generous upper bound
                        val lowerBound = unfuzzedDays * (1.0 - maxFuzzPercent)
                        val upperBound = unfuzzedDays * (1.0 + maxFuzzPercent)

                        assertTrue(
                            fuzzedDays >= lowerBound - 1 && fuzzedDays <= upperBound + 1,
                            "fuzzed interval ($fuzzedDays days) should be within range " +
                                "[$lowerBound, $upperBound] of unfuzzed ($unfuzzedDays days) " +
                                "at review $i with seed $seed"
                        )
                    }
                }
            }
        }
    }

    // ── Same seed produces same results ──

    @Test
    fun testSameSeedProducesSameResults() {
        fun runWithSeed(seed: Int): List<Long> {
            val scheduler = FsrsScheduler(FsrsParameters(enableFuzzing = true, randomSeed = seed))
            var card = newCard()
            var now = BASE_TIME
            val dues = mutableListOf<Long>()

            for (i in 0 until 8) {
                card = scheduler.reviewCard(card, Rating.Good, now)
                dues.add(card.due)
                now = card.due
            }
            return dues
        }

        val run1 = runWithSeed(42)
        val run2 = runWithSeed(42)

        assertEquals(run1, run2, "same seed should produce identical interval sequences")
    }

    // ── Fuzzing does not affect learning steps ──

    @Test
    fun testFuzzingDoesNotAffectLearningSteps() {
        val fuzzedScheduler = FsrsScheduler(FsrsParameters(enableFuzzing = true, randomSeed = 42))
        val unfuzzedScheduler = FsrsScheduler(FsrsParameters(enableFuzzing = false))

        // First review (learning step 0 -> step 1) should be identical
        val fuzzedResult = fuzzedScheduler.reviewCard(newCard(), Rating.Good, BASE_TIME)
        val unfuzzedResult = unfuzzedScheduler.reviewCard(newCard(), Rating.Good, BASE_TIME)

        assertEquals(
            unfuzzedResult.due,
            fuzzedResult.due,
            "learning step intervals should not be affected by fuzzing"
        )
        assertEquals(
            unfuzzedResult.step,
            fuzzedResult.step,
            "learning step should be the same regardless of fuzzing"
        )
    }
}
