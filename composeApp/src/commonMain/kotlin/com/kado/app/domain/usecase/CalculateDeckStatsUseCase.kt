package com.kado.app.domain.usecase

import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Deck
import com.kado.app.domain.repository.DeckRepository

data class DeckStats(
    val deck: Deck?,
    val totalCards: Int,
    val newCards: Int,
    val learningCards: Int,
    val youngCards: Int,
    val matureCards: Int,
    val dueNow: Int
)

class CalculateDeckStatsUseCase(private val repository: DeckRepository) {

    suspend operator fun invoke(deckId: Long, now: Long): DeckStats {
        val deck = repository.getDeck(deckId)
        val totalCards = repository.getCardCount(deckId)
        val states = repository.getCardStates(deckId)
        return calculateStats(deck, totalCards, states, now)
    }

    companion object {
        fun calculateStats(
            deck: Deck?,
            totalCards: Int,
            states: List<CardState>,
            now: Long
        ): DeckStats {
            val newCount = states.count { it.queue == 0 }
            val learningCount = states.count { it.queue == 1 }
            val youngCount = states.count { it.queue == 2 && it.interval < 21 }
            val matureCount = states.count { it.queue == 2 && it.interval >= 21 }
            val dueCount = states.count { it.queue == 0 || (it.queue != 0 && it.due <= now) }

            return DeckStats(
                deck = deck,
                totalCards = totalCards,
                newCards = newCount,
                learningCards = learningCount,
                youngCards = youngCount,
                matureCards = matureCount,
                dueNow = dueCount
            )
        }
    }
}
