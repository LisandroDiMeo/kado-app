package com.kado.app.domain.model

data class ReviewCard(
    val card: Card,
    val state: CardState
)

data class SessionSummary(
    val reviewed: Int,
    val again: Int,
    val hard: Int,
    val good: Int,
    val easy: Int
)
