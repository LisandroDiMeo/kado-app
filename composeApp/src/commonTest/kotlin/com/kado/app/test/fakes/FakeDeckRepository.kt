package com.kado.app.test.fakes

import androidx.paging.PagingData
import com.kado.app.data.importer.ParsedCard
import com.kado.app.domain.model.BackfillResult
import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.DeckPatchGate
import com.kado.app.domain.model.DeckPatchHandle
import com.kado.app.domain.model.DeckPatchPreviewItem
import com.kado.app.domain.model.DeckSummary
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.ReviewEvent
import com.kado.app.domain.model.ReviewLogEntry
import com.kado.app.domain.model.SubDeckInfo
import com.kado.app.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDeckRepository : DeckRepository {

    var decks = mutableListOf<Deck>()
    var cards = mutableListOf<Card>()
    var cardStates = mutableListOf<CardState>()
    var updatedCardStates = mutableListOf<CardState>()
    val recordedReviews = mutableListOf<ReviewEvent>()
    private val reviewHistoryFlow = MutableStateFlow<List<ReviewLogEntry>>(emptyList())

    override suspend fun getDeck(id: Long): Deck? = decks.find { it.id == id }
    override suspend fun getCardCount(deckId: Long): Int = cards.count { it.deckId == deckId }
    override suspend fun getCards(deckId: Long): List<Card> = cards.filter { it.deckId == deckId }
    override suspend fun getCard(id: Long): Card? = cards.find { it.id == id }
    override suspend fun getCardStates(deckId: Long): List<CardState> = cardStates.toList()
    override suspend fun getCardStates(deckIds: List<Long>): List<CardState> = cardStates.toList()

    override suspend fun recordReview(event: ReviewEvent) {
        recordedReviews.add(event)
        reviewHistoryFlow.value = reviewHistoryFlow.value +
            ReviewLogEntry(event.deckId, event.reviewedAt, event.rating)
    }

    override fun observeReviewHistory(
        deckIds: List<Long>,
        fromEpoch: Long,
        toEpoch: Long
    ): Flow<List<ReviewLogEntry>> = reviewHistoryFlow.map { all ->
        all.filter { entry ->
            (deckIds.isEmpty() || entry.deckId in deckIds) &&
                entry.reviewedAt in fromEpoch..toEpoch
        }
    }

    override suspend fun reviewHistoryCount(deckIds: List<Long>): Int =
        reviewHistoryFlow.value.count { deckIds.isEmpty() || it.deckId in deckIds }
    override suspend fun getCardState(cardId: Long): CardState =
        cardStates.find { it.cardId == cardId } ?: CardState(cardId = cardId)

    override suspend fun updateCardState(state: CardState) {
        updatedCardStates.add(state)
        val index = cardStates.indexOfFirst { it.cardId == state.cardId }
        if (index >= 0) cardStates[index] = state else cardStates.add(state)
    }

    override suspend fun bulkUpdateCards(cards: List<Card>) {
        cards.forEach { updated ->
            val index = this.cards.indexOfFirst { it.id == updated.id }
            if (index >= 0) this.cards[index] = updated
        }
    }

    override suspend fun getSubDeckCards(deckId: Long, subDeckIndex: Int): List<Card> =
        cards.filter { it.deckId == deckId && it.subDeckIndex == subDeckIndex }

    override suspend fun getNextReviewCard(deckId: Long, now: Long, newLimit: Int, excludeCardId: Long): ReviewCard? =
        TODO()

    override suspend fun getNextSubDeckReviewCard(
        deckId: Long,
        subDeckIndex: Int,
        now: Long,
        newLimit: Int,
        excludeCardId: Long
    ): ReviewCard? = TODO()

    override fun observeDecks(): Flow<List<Deck>> = MutableStateFlow(decks.toList())
    override fun observeCardCount(): Flow<Int> = TODO()
    override fun observeCardStateCount(): Flow<Int> = TODO()
    override suspend fun createDeck(name: String, dailyLimit: Int): Long = TODO()
    override suspend fun updateDeck(deck: Deck) = TODO()
    override suspend fun deleteDeck(id: Long) = TODO()
    override suspend fun getDeckSummary(deckId: Long, now: Long): DeckSummary? = TODO()
    override fun observeCards(deckId: Long): Flow<List<Card>> = TODO()
    override fun observeCardsPaged(deckId: Long): Flow<PagingData<Card>> = TODO()
    override fun observeCardCount(deckId: Long): Flow<Int> = TODO()
    override suspend fun addCard(deckId: Long, front: String, back: String): Long = TODO()
    override suspend fun updateCard(card: Card) = TODO()
    override suspend fun deleteCard(id: Long) = TODO()
    override suspend fun deleteCards(cardIds: List<Long>) {
        cards.removeAll { it.id in cardIds }
        cardStates.removeAll { it.cardId in cardIds }
    }
    override suspend fun resetProgress(deckId: Long) {
        cardStates.removeAll { it.cardId in cards.filter { c -> c.deckId == deckId }.map { c -> c.id } }
    }
    override suspend fun resetCardProgress(cardId: Long) {
        val index = cardStates.indexOfFirst { it.cardId == cardId }
        val fresh = CardState(cardId = cardId)
        if (index >= 0) cardStates[index] = fresh else cardStates.add(fresh)
    }
    override suspend fun importDeck(
        name: String,
        cards: List<ParsedCard>,
        onProgress: (Float) -> Unit
    ): Long = TODO()
    override suspend fun rebuildPatchHandle(sessionId: String, deckId: Long): DeckPatchHandle? = null
    override suspend fun checkDeckPatchGate(deckId: Long): DeckPatchGate = TODO()
    override suspend fun backfillAnkiGuids(deckId: Long, parsedCards: List<ParsedCard>): BackfillResult = TODO()
    override suspend fun previewDeckPatch(deckId: Long, parsedCards: List<ParsedCard>): DeckPatchHandle = TODO()
    override suspend fun pageAddedCards(
        handle: DeckPatchHandle,
        offset: Int,
        limit: Int
    ): List<DeckPatchPreviewItem.Added> = TODO()
    override suspend fun pageModifiedCards(
        handle: DeckPatchHandle,
        offset: Int,
        limit: Int
    ): List<DeckPatchPreviewItem.Modified> = TODO()
    override suspend fun pageRemovedCards(
        handle: DeckPatchHandle,
        offset: Int,
        limit: Int
    ): List<DeckPatchPreviewItem.Removed> = TODO()
    override suspend fun applyDeckPatch(handle: DeckPatchHandle, keepRemovedIds: Set<Long>) = TODO()
    override suspend fun discardDeckPatch(handle: DeckPatchHandle) = TODO()
    override suspend fun getSubDeckIndices(deckId: Long): List<Int> = TODO()
    override fun observeSubDeckIndices(deckId: Long): Flow<List<Int>> = TODO()
    override fun observeSubDeckCards(deckId: Long, subDeckIndex: Int): Flow<List<Card>> = TODO()
    override suspend fun assignSubDeck(cardId: Long, subDeckIndex: Int?) = TODO()
    override suspend fun batchPartition(deckId: Long, groupSize: Int) = TODO()
    override suspend fun clearPartitions(deckId: Long) = TODO()
    override suspend fun removeSubDeck(deckId: Long, subDeckIndex: Int) = TODO()
    override suspend fun getSubDeckSummary(deckId: Long, subDeckIndex: Int, now: Long): SubDeckInfo = TODO()
    override suspend fun cloneSubDeckAsNewDeck(deckId: Long, subDeckIndex: Int, newName: String): Long = TODO()
    override suspend fun createReversedDeck(deckId: Long, newName: String): Long = TODO()
}
