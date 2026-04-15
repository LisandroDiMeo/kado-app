package com.kado.app.domain.repository

import androidx.paging.PagingData
import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.DeckSummary
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.SubDeckInfo
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun observeDecks(): Flow<List<Deck>>
    fun observeCardCount(): Flow<Int>
    fun observeCardStateCount(): Flow<Int>
    suspend fun getDeck(id: Long): Deck?
    suspend fun createDeck(name: String, dailyLimit: Int = 20): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(id: Long)
    suspend fun getDeckSummary(deckId: Long, now: Long): DeckSummary?

    fun observeCards(deckId: Long): Flow<List<Card>>
    fun observeCardsPaged(deckId: Long): Flow<PagingData<Card>>
    fun observeCardCount(deckId: Long): Flow<Int>
    suspend fun getCardCount(deckId: Long): Int
    suspend fun getCards(deckId: Long): List<Card>
    suspend fun getCard(id: Long): Card?
    suspend fun addCard(deckId: Long, front: String, back: String): Long
    suspend fun updateCard(card: Card)
    suspend fun bulkUpdateCards(cards: List<Card>)
    suspend fun deleteCard(id: Long)

    suspend fun getCardState(cardId: Long): CardState
    suspend fun getCardStates(deckId: Long): List<CardState>
    suspend fun updateCardState(state: CardState)
    suspend fun resetProgress(deckId: Long)
    suspend fun getNextReviewCard(deckId: Long, now: Long, newLimit: Int, excludeCardId: Long = -1): ReviewCard?

    suspend fun importDeck(
        name: String,
        cards: List<Pair<String, String>>,
        onProgress: (Float) -> Unit = {}
    ): Long

    // Sub-deck operations
    suspend fun getSubDeckIndices(deckId: Long): List<Int>
    fun observeSubDeckIndices(deckId: Long): Flow<List<Int>>
    suspend fun getSubDeckCards(deckId: Long, subDeckIndex: Int): List<Card>
    fun observeSubDeckCards(deckId: Long, subDeckIndex: Int): Flow<List<Card>>
    suspend fun assignSubDeck(cardId: Long, subDeckIndex: Int?)
    suspend fun batchPartition(deckId: Long, groupSize: Int)
    suspend fun clearPartitions(deckId: Long)
    suspend fun removeSubDeck(deckId: Long, subDeckIndex: Int)
    suspend fun getSubDeckSummary(deckId: Long, subDeckIndex: Int, now: Long): SubDeckInfo
    suspend fun getNextSubDeckReviewCard(
        deckId: Long,
        subDeckIndex: Int,
        now: Long,
        newLimit: Int,
        excludeCardId: Long = -1
    ): ReviewCard?
    suspend fun cloneSubDeckAsNewDeck(deckId: Long, subDeckIndex: Int, newName: String): Long
    suspend fun createReversedDeck(deckId: Long, newName: String): Long
}
