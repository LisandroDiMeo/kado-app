package com.kado.app.domain.srs

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Rating

interface Scheduler {
    fun reviewCard(state: CardState, rating: Rating, nowEpochSeconds: Long): CardState
    fun previewIntervals(state: CardState, nowEpochSeconds: Long): Map<Rating, String>
}
