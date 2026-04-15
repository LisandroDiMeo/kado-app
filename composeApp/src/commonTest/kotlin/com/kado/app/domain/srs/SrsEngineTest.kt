package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SrsEngineTest {

    private val now = 1_000_000L
    private val secondsPerDay = 86400L

    private fun newCard(ease: Int = SrsEngine.EASE_DEFAULT) = CardState(
        cardId = 1L,
        due = 0,
        interval = 0,
        ease = ease,
        reps = 0,
        lapses = 0,
        queue = 0
    )

    private fun reviewCard(
        interval: Int = 10,
        ease: Int = SrsEngine.EASE_DEFAULT,
        reps: Int = 3,
        lapses: Int = 0
    ) = CardState(
        cardId = 1L,
        due = 0,
        interval = interval,
        ease = ease,
        reps = reps,
        lapses = lapses,
        queue = 2
    )

    // ── New card reviews (queue=0) ──

    @Test
    fun newCard_ratedAgain_resetsToLearning() {
        val result = SrsEngine.reviewCard(newCard(), Rating.Again, now)
        assertEquals(1, result.interval)
        assertEquals(1, result.queue)
        assertEquals(now + 60, result.due)
        assertEquals(1, result.lapses)
        assertEquals(0, result.reps)
        assertEquals(23, result.ease)
    }

    @Test
    fun newCard_ratedHard_schedulesOneDayInterval() {
        val result = SrsEngine.reviewCard(newCard(), Rating.Hard, now)
        assertEquals(1, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(24, result.ease)
        assertEquals(now + secondsPerDay, result.due)
    }

    @Test
    fun newCard_ratedGood_schedulesOneDayInterval() {
        val result = SrsEngine.reviewCard(newCard(), Rating.Good, now)
        assertEquals(1, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(25, result.ease)
        assertEquals(now + secondsPerDay, result.due)
    }

    @Test
    fun newCard_ratedEasy_schedulesFourDayInterval() {
        val result = SrsEngine.reviewCard(newCard(), Rating.Easy, now)
        assertEquals(4, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(27, result.ease)
        assertEquals(now + 4 * secondsPerDay, result.due)
    }

    // ── Subsequent reviews (queue=2, reps>0) ──

    @Test
    fun reviewCard_ratedGood_multipliesIntervalByEase() {
        // interval=10, ease=25 → 10 * 2.5 = 25
        val result = SrsEngine.reviewCard(reviewCard(interval = 10, ease = 25), Rating.Good, now)
        assertEquals(25, result.interval)
        assertEquals(25, result.ease)
        assertEquals(now + 25 * secondsPerDay, result.due)
    }

    @Test
    fun reviewCard_ratedHard_multipliesIntervalBy1_2() {
        // interval=10 → 10 * 1.2 = 12
        val result = SrsEngine.reviewCard(reviewCard(interval = 10, ease = 25), Rating.Hard, now)
        assertEquals(12, result.interval)
        assertEquals(24, result.ease)
        assertEquals(now + 12 * secondsPerDay, result.due)
    }

    @Test
    fun reviewCard_ratedEasy_multipliesIntervalByEaseAndBonus() {
        // interval=10, ease=25 → newEase=27, 10 * 2.7 * 1.3 = 35.1 → rounds to 35
        val result = SrsEngine.reviewCard(reviewCard(interval = 10, ease = 25), Rating.Easy, now)
        assertEquals(35, result.interval)
        assertEquals(27, result.ease)
        assertEquals(now + 35 * secondsPerDay, result.due)
    }

    @Test
    fun reviewCard_ratedAgain_resetsInterval() {
        val card = reviewCard(interval = 20, ease = 25, reps = 5, lapses = 1)
        val result = SrsEngine.reviewCard(card, Rating.Again, now)
        assertEquals(1, result.interval)
        assertEquals(1, result.queue)
        assertEquals(0, result.reps)
        assertEquals(2, result.lapses)
        assertEquals(23, result.ease)
        assertEquals(now + 60, result.due)
    }

    // ── Multi-step scheduling sequences ──

    @Test
    fun multipleReviews_goodStreak_intervalsGrowExponentially() {
        var state = newCard()
        val intervals = mutableListOf<Int>()

        // First review: new card → interval=1
        state = SrsEngine.reviewCard(state, Rating.Good, now)
        intervals.add(state.interval)

        // Subsequent reviews with Good
        for (i in 1..4) {
            state = SrsEngine.reviewCard(state, Rating.Good, now + i * 100_000L)
            intervals.add(state.interval)
        }

        // Each interval should be larger than the previous
        for (i in 1 until intervals.size) {
            assertTrue(
                intervals[i] > intervals[i - 1],
                "interval[$i]=${intervals[i]} should be > interval[${i - 1}]=${intervals[i - 1]}"
            )
        }
        // First interval is 1 (new card Good)
        assertEquals(1, intervals[0])
    }

    @Test
    fun multipleReviews_mixedRatings_intervalAdjustsCorrectly() {
        var state = newCard()

        // Good → interval=1, ease=25
        state = SrsEngine.reviewCard(state, Rating.Good, now)
        assertEquals(1, state.interval)
        assertEquals(25, state.ease)

        // Hard → interval=1*1.2=1.2→1, ease=24
        state = SrsEngine.reviewCard(state, Rating.Hard, now + 100_000L)
        assertEquals(24, state.ease)

        // Good → interval * (24/10) = interval * 2.4
        val prevInterval = state.interval
        state = SrsEngine.reviewCard(state, Rating.Good, now + 200_000L)
        assertEquals(24, state.ease)

        // Easy → interval * (26/10) * 1.3, ease=26
        state = SrsEngine.reviewCard(state, Rating.Easy, now + 300_000L)
        assertEquals(26, state.ease)
    }

    // ── Edge cases / clamping ──

    @Test
    fun easeNeverDropsBelowMinimum() {
        var state = newCard(ease = 15) // start near minimum
        // Repeated Again ratings
        for (i in 0..10) {
            state = SrsEngine.reviewCard(state, Rating.Again, now + i * 100L)
        }
        assertTrue(state.ease >= 13, "ease=${state.ease} should be >= 13")
    }

    @Test
    fun easeNeverExceedsMaximum() {
        var state = newCard(ease = 38) // start near maximum
        // Repeated Easy ratings
        state = SrsEngine.reviewCard(state, Rating.Easy, now)
        for (i in 1..10) {
            state = SrsEngine.reviewCard(state, Rating.Easy, now + i * 100_000L)
        }
        assertTrue(state.ease <= 40, "ease=${state.ease} should be <= 40")
    }

    @Test
    fun intervalNeverExceedsMaximum() {
        var state = reviewCard(interval = 30000, ease = 40, reps = 100)
        // Several Easy reviews with max ease should still clamp
        for (i in 0..5) {
            state = SrsEngine.reviewCard(state, Rating.Easy, now + i * 100_000L)
        }
        assertTrue(state.interval <= 36500, "interval=${state.interval} should be <= 36500")
    }

    @Test
    fun learningCard_ratedGood_treatedAsNewCard() {
        // queue=1, reps=0 → treated as new card (reps==0 branch)
        val learningCard = CardState(
            cardId = 1L,
            due = 0,
            interval = 1,
            ease = 23,
            reps = 0,
            lapses = 1,
            queue = 1
        )
        val result = SrsEngine.reviewCard(learningCard, Rating.Good, now)
        assertEquals(1, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(now + secondsPerDay, result.due)
    }

    // ── Due date accuracy ──

    @Test
    fun dueDate_newCardGood_isNowPlusOneDay() {
        val result = SrsEngine.reviewCard(newCard(), Rating.Good, now)
        assertEquals(now + 1 * secondsPerDay, result.due)
    }

    @Test
    fun dueDate_againRating_isNowPlus60Seconds() {
        val result = SrsEngine.reviewCard(newCard(), Rating.Again, now)
        assertEquals(now + 60, result.due)
    }
}
