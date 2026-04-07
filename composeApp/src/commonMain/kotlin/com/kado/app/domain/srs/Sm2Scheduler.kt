package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating

object Sm2Scheduler : Scheduler {

    override fun reviewCard(state: CardState, rating: Rating, nowEpochSeconds: Long): CardState =
        SrsEngine.reviewCard(state, rating, nowEpochSeconds)

    override fun previewIntervals(state: CardState, nowEpochSeconds: Long): Map<Rating, String> =
        SrsEngine.previewIntervals(state, nowEpochSeconds)
}
