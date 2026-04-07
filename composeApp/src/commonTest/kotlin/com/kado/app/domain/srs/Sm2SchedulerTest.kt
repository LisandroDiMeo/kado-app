package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * SM2 regression tests exercised through the Sm2Scheduler (Scheduler interface wrapper).
 * These are the same test cases as SrsEngineTest but routed through the Scheduler interface,
 * ensuring the Sm2Scheduler wrapper delegates correctly to SrsEngine.
 */
class Sm2SchedulerTest {

    private val scheduler: Scheduler = Sm2Scheduler
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
        val result = scheduler.reviewCard(newCard(), Rating.Again, now)
        assertEquals(1, result.interval)
        assertEquals(1, result.queue)
        assertEquals(now + 60, result.due)
        assertEquals(1, result.lapses)
        assertEquals(0, result.reps)
        assertEquals(23, result.ease)
    }

    @Test
    fun newCard_ratedHard_schedulesOneDayInterval() {
        val result = scheduler.reviewCard(newCard(), Rating.Hard, now)
        assertEquals(1, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(24, result.ease)
        assertEquals(now + secondsPerDay, result.due)
    }

    @Test
    fun newCard_ratedGood_schedulesOneDayInterval() {
        val result = scheduler.reviewCard(newCard(), Rating.Good, now)
        assertEquals(1, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(25, result.ease)
        assertEquals(now + secondsPerDay, result.due)
    }

    @Test
    fun newCard_ratedEasy_schedulesFourDayInterval() {
        val result = scheduler.reviewCard(newCard(), Rating.Easy, now)
        assertEquals(4, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(27, result.ease)
        assertEquals(now + 4 * secondsPerDay, result.due)
    }

    // ── Subsequent reviews (queue=2, reps>0) ──

    @Test
    fun reviewCard_ratedGood_multipliesIntervalByEase() {
        val result = scheduler.reviewCard(reviewCard(interval = 10, ease = 25), Rating.Good, now)
        assertEquals(25, result.interval)
        assertEquals(25, result.ease)
        assertEquals(now + 25 * secondsPerDay, result.due)
    }

    @Test
    fun reviewCard_ratedHard_multipliesIntervalBy1_2() {
        val result = scheduler.reviewCard(reviewCard(interval = 10, ease = 25), Rating.Hard, now)
        assertEquals(12, result.interval)
        assertEquals(24, result.ease)
        assertEquals(now + 12 * secondsPerDay, result.due)
    }

    @Test
    fun reviewCard_ratedEasy_multipliesIntervalByEaseAndBonus() {
        val result = scheduler.reviewCard(reviewCard(interval = 10, ease = 25), Rating.Easy, now)
        assertEquals(35, result.interval)
        assertEquals(27, result.ease)
        assertEquals(now + 35 * secondsPerDay, result.due)
    }

    @Test
    fun reviewCard_ratedAgain_resetsInterval() {
        val card = reviewCard(interval = 20, ease = 25, reps = 5, lapses = 1)
        val result = scheduler.reviewCard(card, Rating.Again, now)
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

        state = scheduler.reviewCard(state, Rating.Good, now)
        intervals.add(state.interval)

        for (i in 1..4) {
            state = scheduler.reviewCard(state, Rating.Good, now + i * 100_000L)
            intervals.add(state.interval)
        }

        for (i in 1 until intervals.size) {
            assertTrue(
                intervals[i] > intervals[i - 1],
                "interval[$i]=${intervals[i]} should be > interval[${i - 1}]=${intervals[i - 1]}"
            )
        }
        assertEquals(1, intervals[0])
    }

    @Test
    fun multipleReviews_mixedRatings_intervalAdjustsCorrectly() {
        var state = newCard()

        state = scheduler.reviewCard(state, Rating.Good, now)
        assertEquals(1, state.interval)
        assertEquals(25, state.ease)

        state = scheduler.reviewCard(state, Rating.Hard, now + 100_000L)
        assertEquals(24, state.ease)

        state = scheduler.reviewCard(state, Rating.Good, now + 200_000L)
        assertEquals(24, state.ease)

        state = scheduler.reviewCard(state, Rating.Easy, now + 300_000L)
        assertEquals(26, state.ease)
    }

    // ── Edge cases / clamping ──

    @Test
    fun easeNeverDropsBelowMinimum() {
        var state = newCard(ease = 15)
        for (i in 0..10) {
            state = scheduler.reviewCard(state, Rating.Again, now + i * 100L)
        }
        assertTrue(state.ease >= 13, "ease=${state.ease} should be >= 13")
    }

    @Test
    fun easeNeverExceedsMaximum() {
        var state = newCard(ease = 38)
        state = scheduler.reviewCard(state, Rating.Easy, now)
        for (i in 1..10) {
            state = scheduler.reviewCard(state, Rating.Easy, now + i * 100_000L)
        }
        assertTrue(state.ease <= 40, "ease=${state.ease} should be <= 40")
    }

    @Test
    fun intervalNeverExceedsMaximum() {
        var state = reviewCard(interval = 30000, ease = 40, reps = 100)
        for (i in 0..5) {
            state = scheduler.reviewCard(state, Rating.Easy, now + i * 100_000L)
        }
        assertTrue(state.interval <= 36500, "interval=${state.interval} should be <= 36500")
    }

    @Test
    fun learningCard_ratedGood_treatedAsNewCard() {
        val learningCard = CardState(
            cardId = 1L, due = 0, interval = 1, ease = 23,
            reps = 0, lapses = 1, queue = 1
        )
        val result = scheduler.reviewCard(learningCard, Rating.Good, now)
        assertEquals(1, result.interval)
        assertEquals(2, result.queue)
        assertEquals(1, result.reps)
        assertEquals(now + secondsPerDay, result.due)
    }

    // ── Due date accuracy ──

    @Test
    fun dueDate_newCardGood_isNowPlusOneDay() {
        val result = scheduler.reviewCard(newCard(), Rating.Good, now)
        assertEquals(now + 1 * secondsPerDay, result.due)
    }

    @Test
    fun dueDate_againRating_isNowPlus60Seconds() {
        val result = scheduler.reviewCard(newCard(), Rating.Again, now)
        assertEquals(now + 60, result.due)
    }

    // ── Verify previewIntervals returns a map ──

    @Test
    fun previewIntervals_returnsAllRatings() {
        val preview = scheduler.previewIntervals(newCard(), now)
        assertTrue(preview.containsKey(Rating.Again), "preview should contain Again")
        assertTrue(preview.containsKey(Rating.Hard), "preview should contain Hard")
        assertTrue(preview.containsKey(Rating.Good), "preview should contain Good")
        assertTrue(preview.containsKey(Rating.Easy), "preview should contain Easy")
    }
}
