package com.kado.app.domain.model

import com.kado.app.domain.srs.SchedulerType

data class Deck(
    val id: Long = 0,
    val name: String,
    val dailyLimit: Int = 20,
    val createdAt: Long = 0,
    val schedulerType: SchedulerType = SchedulerType.SM2,
    val fsrsDesiredRetention: Double = 0.9,
    val fsrsLearningSteps: String = "1m, 10m",
    val fsrsRelearningSteps: String = "10m",
    val fsrsMaxInterval: Int = 36500,
    val fsrsEnableFuzzing: Boolean = true
)

data class Card(
    val id: Long = 0,
    val deckId: Long,
    val front: CardContent,
    val back: CardContent,
    val position: Int = 0,
    val createdAt: Long = 0,
    val subDeckIndex: Int? = null
)

data class SubDeckInfo(
    val index: Int,
    val cardCount: Int,
    val dueCount: Int,
    val newCount: Int
)

data class CardState(
    val cardId: Long,
    val due: Long = 0,
    val interval: Int = 0,
    val ease: Int = 25,
    val reps: Int = 0,
    val lapses: Int = 0,
    val queue: Int = 0, // 0=new, 1=learning, 2=review
    // FSRS fields
    val stability: Double? = null,
    val difficulty: Double? = null,
    val fsrsState: Int? = null,    // 1=LEARNING, 2=REVIEW, 3=RELEARNING
    val step: Int? = null,
    val lastReview: Long? = null
)

data class DeckSummary(
    val deck: Deck,
    val totalCards: Int,
    val newCards: Int,
    val dueCards: Int
)
