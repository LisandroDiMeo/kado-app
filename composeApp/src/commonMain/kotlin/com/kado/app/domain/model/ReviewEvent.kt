package com.kado.app.domain.model

data class ReviewEvent(
    val cardId: Long,
    val deckId: Long,
    val reviewedAt: Long,
    val rating: Rating,
    val durationMs: Long,
    val previousInterval: Int,
    val newInterval: Int,
    val previousQueue: Int,
    val newQueue: Int
)

data class ReviewLogEntry(val deckId: Long, val reviewedAt: Long, val rating: Rating)
