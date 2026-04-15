package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Long-term behavioral/simulation tests for the FSRS scheduler.
 * These tests validate convergence, growth patterns, and parameter effects
 * rather than exact numerical values.
 */
class FsrsSimulationTest {

    companion object {
        private const val SECONDS_PER_DAY = 86400L
        private const val BASE_TIME = 1_000_000_000L

        private const val STATE_REVIEW = 2
    }

    private fun newCard() = CardState(cardId = 1L)

    private fun noFuzzParams() = FsrsParameters(enableFuzzing = false)

    private fun noFuzzScheduler() = FsrsScheduler(noFuzzParams())

    // ── Convergence to high half-life ──

    @Test
    fun testConvergenceToHighHalfLife() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME
        var reviewCount = 0

        while ((card.stability ?: 0.0) <= 360.0 && reviewCount < 15) {
            card = scheduler.reviewCard(card, Rating.Good, now)
            now = card.due
            reviewCount++
        }

        assertNotNull(card.stability)
        assertTrue(
            card.stability!! > 360.0,
            "stability (${card.stability}) should exceed 360 days within 15 GOOD reviews (took $reviewCount)"
        )
        assertTrue(
            reviewCount <= 15,
            "should reach stability > 360 within 15 reviews, but took $reviewCount"
        )
    }

    // ── Forgetting reduces stability ──

    @Test
    fun testForgettingReducesStability() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        // Build up stability with 5 GOOD reviews
        for (i in 0 until 5) {
            card = scheduler.reviewCard(card, Rating.Good, now)
            now = card.due
        }

        val stabilityBeforeAgain = card.stability
        assertNotNull(stabilityBeforeAgain, "stability should be set after 5 GOOD reviews")
        assertTrue(stabilityBeforeAgain!! > 1.0, "stability should be meaningful after 5 GOOD reviews")

        // Rate AGAIN
        card = scheduler.reviewCard(card, Rating.Again, now)

        assertNotNull(card.stability, "stability should still be set after AGAIN")
        assertTrue(
            card.stability!! < stabilityBeforeAgain,
            "stability (${card.stability}) should decrease after AGAIN (was $stabilityBeforeAgain)"
        )
    }

    // ── Mixed rating convergence ──

    @Test
    fun testMixedRatingConvergence() {
        val scheduler = noFuzzScheduler()

        // Pure GOOD sequence
        var pureGoodCard = newCard()
        var pureGoodNow = BASE_TIME
        for (i in 0 until 10) {
            pureGoodCard = scheduler.reviewCard(pureGoodCard, Rating.Good, pureGoodNow)
            pureGoodNow = pureGoodCard.due
        }

        // Mixed GOOD/HARD sequence
        var mixedCard = newCard()
        var mixedNow = BASE_TIME
        for (i in 0 until 10) {
            val rating = if (i % 2 == 0) Rating.Good else Rating.Hard
            mixedCard = scheduler.reviewCard(mixedCard, rating, mixedNow)
            mixedNow = mixedCard.due
        }

        val pureGoodStability = pureGoodCard.stability
        val mixedStability = mixedCard.stability

        assertNotNull(pureGoodStability)
        assertNotNull(mixedStability)

        // Mixed should still grow (stability > 0) but less than pure GOOD
        assertTrue(
            mixedStability!! > 0.0,
            "mixed sequence stability should be positive"
        )
        assertTrue(
            mixedStability < pureGoodStability!!,
            "mixed stability ($mixedStability) should be less than pure GOOD stability ($pureGoodStability)"
        )
    }

    // ── Desired retention affects intervals ──

    @Test
    fun testDesiredRetentionAffectsIntervals() {
        val highRetentionParams = FsrsParameters(enableFuzzing = false, desiredRetention = 0.9)
        val lowRetentionParams = FsrsParameters(enableFuzzing = false, desiredRetention = 0.8)

        val highRetentionScheduler = FsrsScheduler(highRetentionParams)
        val lowRetentionScheduler = FsrsScheduler(lowRetentionParams)

        // Same rating sequence for both
        var highCard = newCard()
        var lowCard = newCard()
        var highNow = BASE_TIME
        var lowNow = BASE_TIME

        for (i in 0 until 8) {
            highCard = highRetentionScheduler.reviewCard(highCard, Rating.Good, highNow)
            lowCard = lowRetentionScheduler.reviewCard(lowCard, Rating.Good, lowNow)
            highNow = highCard.due
            lowNow = lowCard.due
        }

        // Lower retention should produce longer intervals (cards spaced further apart)
        // Compare the last interval (due - lastReview)
        val highLastReview = highCard.lastReview ?: BASE_TIME
        val lowLastReview = lowCard.lastReview ?: BASE_TIME
        val highInterval = highCard.due - highLastReview
        val lowInterval = lowCard.due - lowLastReview

        // Only compare if both are in REVIEW state (past learning steps)
        if (highCard.fsrsState == STATE_REVIEW && lowCard.fsrsState == STATE_REVIEW) {
            assertTrue(
                lowInterval > highInterval,
                "lower retention interval ($lowInterval) should be greater than higher retention interval ($highInterval)"
            )
        }
    }

    // ── Stability monotonically increases with GOOD reviews ──

    @Test
    fun testStabilityGrowsWithConsecutiveGoodReviews() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME
        var previousStability = 0.0

        for (i in 0 until 8) {
            card = scheduler.reviewCard(card, Rating.Good, now)
            now = card.due

            val currentStability = card.stability ?: 0.0
            if (card.fsrsState == STATE_REVIEW) {
                assertTrue(
                    currentStability > previousStability,
                    "stability should grow with consecutive GOOD reviews: " +
                        "$currentStability should be > $previousStability at review $i"
                )
            }
            previousStability = currentStability
        }
    }

    // ── Difficulty decreases with EASY, increases with AGAIN ──

    @Test
    fun testDifficultyAdjustsWithRatings() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        // Start with GOOD to establish baseline
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due
        card = scheduler.reviewCard(card, Rating.Good, now)
        now = card.due

        val baselineDifficulty = card.difficulty
        assertNotNull(baselineDifficulty)

        // EASY should reduce difficulty
        val easyCard = scheduler.reviewCard(card, Rating.Easy, now)
        assertNotNull(easyCard.difficulty)
        assertTrue(
            easyCard.difficulty!! <= baselineDifficulty!!,
            "EASY should reduce or maintain difficulty: ${easyCard.difficulty} should be <= $baselineDifficulty"
        )

        // AGAIN should increase difficulty
        val againCard = scheduler.reviewCard(card, Rating.Again, now)
        assertNotNull(againCard.difficulty)
        assertTrue(
            againCard.difficulty!! >= baselineDifficulty,
            "AGAIN should increase or maintain difficulty: ${againCard.difficulty} should be >= $baselineDifficulty"
        )
    }

    // ── Interval grows over time with GOOD reviews ──

    @Test
    fun testIntervalGrowthPattern() {
        val scheduler = noFuzzScheduler()
        var card = newCard()
        var now = BASE_TIME

        val reviewIntervals = mutableListOf<Long>()

        for (i in 0 until 10) {
            card = scheduler.reviewCard(card, Rating.Good, now)
            val lastReview = card.lastReview ?: now
            val interval = card.due - lastReview
            reviewIntervals.add(interval)
            now = card.due
        }

        // Once in REVIEW state, intervals should generally increase
        val reviewStateIntervals = reviewIntervals.drop(2) // skip learning steps
        for (i in 1 until reviewStateIntervals.size) {
            assertTrue(
                reviewStateIntervals[i] >= reviewStateIntervals[i - 1],
                "review intervals should be non-decreasing: " +
                    "${reviewStateIntervals[i]} should be >= ${reviewStateIntervals[i - 1]} at index $i"
            )
        }
    }

    // ── Hard produces shorter intervals than Good ──

    @Test
    fun testHardProducesShorterIntervalsThanGood() {
        val scheduler = noFuzzScheduler()

        // Build two identical cards to REVIEW state
        var goodCard = newCard()
        var hardCard = newCard()
        var now = BASE_TIME

        // Same setup for both
        for (i in 0 until 4) {
            goodCard = scheduler.reviewCard(goodCard, Rating.Good, now)
            hardCard = scheduler.reviewCard(hardCard, Rating.Good, now)
            now = goodCard.due
        }

        // Now diverge
        val goodResult = scheduler.reviewCard(goodCard, Rating.Good, now)
        val hardResult = scheduler.reviewCard(hardCard, Rating.Hard, now)

        if (goodResult.fsrsState == STATE_REVIEW && hardResult.fsrsState == STATE_REVIEW) {
            val goodInterval = goodResult.due - (goodResult.lastReview ?: now)
            val hardInterval = hardResult.due - (hardResult.lastReview ?: now)

            assertTrue(
                hardInterval <= goodInterval,
                "HARD interval ($hardInterval) should be <= GOOD interval ($goodInterval)"
            )
        }
    }
}
