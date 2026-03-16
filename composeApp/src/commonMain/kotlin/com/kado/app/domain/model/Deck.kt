package com.kado.app.domain.model

data class Deck(
    val id: Long = 0,
    val name: String,
    val dailyLimit: Int = 20,
    val createdAt: Long = 0
)

data class Card(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
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
    val queue: Int = 0 // 0=new, 1=learning, 2=review
)

data class DeckSummary(
    val deck: Deck,
    val totalCards: Int,
    val newCards: Int,
    val dueCards: Int
)
