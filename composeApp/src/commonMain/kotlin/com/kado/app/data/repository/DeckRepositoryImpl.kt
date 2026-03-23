package com.kado.app.data.repository

import com.kado.app.data.local.dao.CardDao
import com.kado.app.data.local.dao.CardStateDao
import com.kado.app.data.local.dao.DeckDao
import com.kado.app.data.local.entity.CardEntity
import com.kado.app.data.local.entity.CardStateEntity
import com.kado.app.data.local.entity.DeckEntity
import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.DeckSummary
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.SubDeckInfo
import com.kado.app.data.importer.MediaStorage
import com.kado.app.domain.parser.CardContentParser
import com.kado.app.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckRepositoryImpl(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val cardStateDao: CardStateDao,
    private val contentParser: CardContentParser
) : DeckRepository {

    override fun observeDecks(): Flow<List<Deck>> =
        deckDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCardCount(): Flow<Int> = cardDao.observeTotalCount()

    override fun observeCardStateCount(): Flow<Int> = cardStateDao.observeTotalCount()

    override suspend fun getDeck(id: Long): Deck? =
        deckDao.getById(id)?.toDomain()

    override suspend fun createDeck(name: String, dailyLimit: Int): Long =
        deckDao.insert(DeckEntity(
            name = name,
            dailyLimit = dailyLimit,
            createdAt = kotlin.time.Clock.System.now().epochSeconds
        ))

    override suspend fun updateDeck(deck: Deck) =
        deckDao.update(DeckEntity(
            id = deck.id,
            name = deck.name,
            dailyLimit = deck.dailyLimit,
            createdAt = deck.createdAt
        ))

    override suspend fun deleteDeck(id: Long) {
        deckDao.deleteById(id)
        MediaStorage.deleteMediaDir(id)
    }

    override suspend fun getDeckSummary(deckId: Long, now: Long): DeckSummary? {
        val deck = getDeck(deckId) ?: return null
        val totalCards = cardDao.countByDeckId(deckId)
        val newCards = cardStateDao.countNewByDeckId(deckId)
        val dueCards = cardStateDao.countDueByDeckId(deckId, now)
        return DeckSummary(deck, totalCards, newCards, dueCards)
    }

    override fun observeCards(deckId: Long): Flow<List<Card>> =
        cardDao.observeByDeckId(deckId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCards(deckId: Long): List<Card> =
        cardDao.getByDeckId(deckId).map { it.toDomain() }

    override suspend fun getCard(id: Long): Card? =
        cardDao.getById(id)?.toDomain()

    override suspend fun addCard(deckId: Long, front: String, back: String): Long {
        val position = cardDao.nextPosition(deckId)
        val cardId = cardDao.insert(CardEntity(
            deckId = deckId,
            front = front,
            back = back,
            position = position,
            createdAt = kotlin.time.Clock.System.now().epochSeconds
        ))
        cardStateDao.upsert(CardStateEntity(cardId = cardId))
        return cardId
    }

    override suspend fun updateCard(card: Card) =
        cardDao.update(CardEntity(
            id = card.id,
            deckId = card.deckId,
            front = card.front.rawText,
            back = card.back.rawText,
            position = card.position,
            createdAt = card.createdAt
        ))

    override suspend fun deleteCard(id: Long) =
        cardDao.deleteById(id)

    override suspend fun getCardState(cardId: Long): CardState {
        val entity = cardStateDao.getByCardId(cardId)
        return entity?.toDomain() ?: CardState(cardId = cardId)
    }

    override suspend fun getCardStates(deckId: Long): List<CardState> =
        cardStateDao.getByDeckId(deckId).map { it.toDomain() }

    override suspend fun updateCardState(state: CardState) =
        cardStateDao.upsert(CardStateEntity(
            cardId = state.cardId,
            due = state.due,
            interval = state.interval,
            ease = state.ease,
            reps = state.reps,
            lapses = state.lapses,
            queue = state.queue
        ))

    override suspend fun resetProgress(deckId: Long) {
        cardStateDao.deleteByDeckId(deckId)
        // Re-create default states for all cards
        val cards = cardDao.getByDeckId(deckId)
        cards.forEach { card ->
            cardStateDao.upsert(CardStateEntity(cardId = card.id))
        }
    }

    override suspend fun getNextReviewCard(deckId: Long, now: Long, newLimit: Int): ReviewCard? {
        val cards = cardDao.getByDeckId(deckId)
        val states = cardStateDao.getByDeckId(deckId).associateBy { it.cardId }

        // Priority: learning (due now) > review (due now) > new (up to limit)

        // 1. Learning cards due now
        for (card in cards) {
            val state = states[card.id] ?: continue
            if (state.queue == 1 && state.due <= now) {
                return ReviewCard(card.toDomain(), state.toDomain())
            }
        }

        // 2. Review cards due now
        for (card in cards) {
            val state = states[card.id] ?: continue
            if (state.queue == 2 && state.due <= now) {
                return ReviewCard(card.toDomain(), state.toDomain())
            }
        }

        // 3. New cards (up to limit)
        if (newLimit > 0) {
            for (card in cards) {
                val state = states[card.id] ?: CardStateEntity(cardId = card.id)
                if (state.queue == 0) {
                    return ReviewCard(card.toDomain(), state.toDomain())
                }
            }
        }

        return null
    }

    override suspend fun importDeck(
        name: String,
        cards: List<Pair<String, String>>,
        onProgress: (Float) -> Unit
    ): Long {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val deckId = deckDao.insert(DeckEntity(name = name, dailyLimit = 20, createdAt = now))

        val chunks = cards.chunked(100)
        chunks.forEachIndexed { chunkIndex, chunk ->
            val cardEntities = chunk.mapIndexed { i, (front, back) ->
                CardEntity(
                    deckId = deckId,
                    front = front,
                    back = back,
                    position = chunkIndex * 100 + i,
                    createdAt = now
                )
            }
            cardDao.insertAll(cardEntities)

            // Get inserted card IDs to create states
            val allCards = cardDao.getByDeckId(deckId)
            val existingStates = cardStateDao.getByDeckId(deckId).map { it.cardId }.toSet()
            val newStates = allCards
                .filter { it.id !in existingStates }
                .map { CardStateEntity(cardId = it.id) }
            if (newStates.isNotEmpty()) {
                cardStateDao.insertAll(newStates)
            }

            onProgress((chunkIndex + 1).toFloat() / chunks.size)
        }

        return deckId
    }

    // Sub-deck operations

    override suspend fun getSubDeckIndices(deckId: Long): List<Int> =
        cardDao.getSubDeckIndices(deckId)

    override fun observeSubDeckIndices(deckId: Long): Flow<List<Int>> =
        cardDao.observeSubDeckIndices(deckId)

    override suspend fun getSubDeckCards(deckId: Long, subDeckIndex: Int): List<Card> =
        cardDao.getByDeckIdAndSubDeck(deckId, subDeckIndex).map { it.toDomain() }

    override fun observeSubDeckCards(deckId: Long, subDeckIndex: Int): Flow<List<Card>> =
        cardDao.observeByDeckIdAndSubDeck(deckId, subDeckIndex).map { entities -> entities.map { it.toDomain() } }

    override suspend fun assignSubDeck(cardId: Long, subDeckIndex: Int?) {
        cardDao.updateSubDeckIndex(cardId, subDeckIndex)
    }

    override suspend fun batchPartition(deckId: Long, groupSize: Int) {
        cardDao.clearAllSubDeckIndices(deckId)
        val cards = cardDao.getByDeckId(deckId)
        cards.forEachIndexed { i, card ->
            cardDao.updateSubDeckIndex(card.id, i / groupSize)
        }
    }

    override suspend fun clearPartitions(deckId: Long) {
        cardDao.clearAllSubDeckIndices(deckId)
    }

    override suspend fun removeSubDeck(deckId: Long, subDeckIndex: Int) {
        cardDao.clearSubDeckIndex(deckId, subDeckIndex)
        cardDao.shiftSubDeckIndicesDown(deckId, subDeckIndex)
    }

    override suspend fun getSubDeckSummary(deckId: Long, subDeckIndex: Int, now: Long): SubDeckInfo {
        val cardCount = cardDao.countByDeckIdAndSubDeck(deckId, subDeckIndex)
        val newCount = cardStateDao.countNewByDeckIdAndSubDeck(deckId, subDeckIndex)
        val dueCount = cardStateDao.countDueByDeckIdAndSubDeck(deckId, subDeckIndex, now)
        return SubDeckInfo(subDeckIndex, cardCount, dueCount, newCount)
    }

    override suspend fun getNextSubDeckReviewCard(deckId: Long, subDeckIndex: Int, now: Long, newLimit: Int): ReviewCard? {
        val cards = cardDao.getByDeckIdAndSubDeck(deckId, subDeckIndex)
        val states = cardStateDao.getByDeckIdAndSubDeck(deckId, subDeckIndex).associateBy { it.cardId }

        // Priority: learning (due now) > review (due now) > new (up to limit)
        for (card in cards) {
            val state = states[card.id] ?: continue
            if (state.queue == 1 && state.due <= now) {
                return ReviewCard(card.toDomain(), state.toDomain())
            }
        }
        for (card in cards) {
            val state = states[card.id] ?: continue
            if (state.queue == 2 && state.due <= now) {
                return ReviewCard(card.toDomain(), state.toDomain())
            }
        }
        if (newLimit > 0) {
            for (card in cards) {
                val state = states[card.id] ?: CardStateEntity(cardId = card.id)
                if (state.queue == 0) {
                    return ReviewCard(card.toDomain(), state.toDomain())
                }
            }
        }
        return null
    }

    override suspend fun cloneSubDeckAsNewDeck(deckId: Long, subDeckIndex: Int, newName: String): Long {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val newDeckId = deckDao.insert(DeckEntity(name = newName, dailyLimit = 20, createdAt = now))
        val cardEntities = cardDao.getByDeckIdAndSubDeck(deckId, subDeckIndex)
        cardEntities.forEachIndexed { i, card ->
            val newCardId = cardDao.insert(CardEntity(
                deckId = newDeckId,
                front = card.front,
                back = card.back,
                position = i,
                createdAt = now
            ))
            cardStateDao.upsert(CardStateEntity(cardId = newCardId))
        }
        return newDeckId
    }

    override suspend fun createReversedDeck(deckId: Long, newName: String): Long {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val newDeckId = deckDao.insert(DeckEntity(name = newName, dailyLimit = 20, createdAt = now))
        val cardEntities = cardDao.getByDeckId(deckId)
        cardEntities.forEachIndexed { i, card ->
            val newCardId = cardDao.insert(CardEntity(
                deckId = newDeckId,
                front = card.back,
                back = card.front,
                position = i,
                createdAt = now
            ))
            cardStateDao.upsert(CardStateEntity(cardId = newCardId))
        }
        return newDeckId
    }

    private fun DeckEntity.toDomain() = Deck(id, name, dailyLimit, createdAt)
    private fun CardEntity.toDomain() = Card(id, deckId, contentParser.detect(front), contentParser.detect(back), position, createdAt, subDeckIndex)
    private fun CardStateEntity.toDomain() = CardState(cardId, due, interval, ease, reps, lapses, queue)
}
