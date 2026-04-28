package com.kado.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.RoomDatabase
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.kado.app.data.importer.MediaStorage
import com.kado.app.data.importer.ParsedCard
import com.kado.app.data.local.dao.CardDao
import com.kado.app.data.local.dao.CardStateDao
import com.kado.app.data.local.dao.DeckDao
import com.kado.app.data.local.dao.PatchDiffDao
import com.kado.app.data.local.dao.ReviewHistoryDao
import com.kado.app.data.local.entity.CardEntity
import com.kado.app.data.local.entity.CardStateEntity
import com.kado.app.data.local.entity.DeckEntity
import com.kado.app.data.local.entity.PatchDiffEntryEntity
import com.kado.app.data.local.entity.ReviewHistoryEntity
import com.kado.app.data.paging.CardPagingSource
import com.kado.app.domain.model.BackfillResult
import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardState
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.DeckPatchGate
import com.kado.app.domain.model.DeckPatchHandle
import com.kado.app.domain.model.DeckPatchPreviewItem
import com.kado.app.domain.model.DeckSummary
import com.kado.app.domain.model.PatchCounts
import com.kado.app.domain.model.Rating
import com.kado.app.domain.model.ReviewCard
import com.kado.app.domain.model.ReviewEvent
import com.kado.app.domain.model.ReviewLogEntry
import com.kado.app.domain.model.SubDeckInfo
import com.kado.app.domain.parser.CardContentParser
import com.kado.app.domain.repository.DeckRepository
import com.kado.app.domain.srs.FsrsParameters
import com.kado.app.domain.srs.SchedulerType
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckRepositoryImpl(
    private val database: RoomDatabase,
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val cardStateDao: CardStateDao,
    private val reviewHistoryDao: ReviewHistoryDao,
    private val patchDiffDao: PatchDiffDao,
    private val contentParser: CardContentParser
) : DeckRepository {

    override fun observeDecks(): Flow<List<Deck>> =
        deckDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCardCount(): Flow<Int> = cardDao.observeTotalCount()

    override fun observeCardStateCount(): Flow<Int> = cardStateDao.observeTotalCount()

    override suspend fun getDeck(id: Long): Deck? =
        deckDao.getById(id)?.toDomain()

    override suspend fun createDeck(name: String, dailyLimit: Int): Long =
        deckDao.insert(
            DeckEntity(
                name = name,
                dailyLimit = dailyLimit,
                createdAt = kotlin.time.Clock.System.now().epochSeconds
            )
        )

    override suspend fun updateDeck(deck: Deck) {
        val fsrsParams = FsrsParameters.serialize(
            FsrsParameters(
                desiredRetention = deck.fsrsDesiredRetention,
                learningStepsSeconds = FsrsParameters.parseStepsString(deck.fsrsLearningSteps),
                relearningStepsSeconds = FsrsParameters.parseStepsString(deck.fsrsRelearningSteps),
                maximumInterval = deck.fsrsMaxInterval,
                enableFuzzing = deck.fsrsEnableFuzzing
            )
        )
        deckDao.update(
            DeckEntity(
                id = deck.id,
                name = deck.name,
                dailyLimit = deck.dailyLimit,
                createdAt = deck.createdAt,
                schedulerType = deck.schedulerType.name,
                fsrsParams = fsrsParams
            )
        )
    }

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

    override fun observeCardsPaged(deckId: Long): Flow<PagingData<Card>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE / 2,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { CardPagingSource(cardDao, deckId) }
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun observeCardCount(deckId: Long): Flow<Int> =
        cardDao.observeCountByDeckId(deckId)

    override suspend fun getCardCount(deckId: Long): Int =
        cardDao.countByDeckId(deckId)

    override suspend fun getCards(deckId: Long): List<Card> =
        cardDao.getByDeckId(deckId).map { it.toDomain() }

    override suspend fun getCard(id: Long): Card? =
        cardDao.getById(id)?.toDomain()

    override suspend fun addCard(deckId: Long, front: String, back: String): Long {
        val position = cardDao.nextPosition(deckId)
        val cardId = cardDao.insert(
            CardEntity(
                deckId = deckId,
                front = front,
                back = back,
                position = position,
                createdAt = kotlin.time.Clock.System.now().epochSeconds
            )
        )
        cardStateDao.upsert(CardStateEntity(cardId = cardId))
        return cardId
    }

    override suspend fun updateCard(card: Card) =
        cardDao.update(
            CardEntity(
                id = card.id,
                deckId = card.deckId,
                front = card.front.rawText,
                back = card.back.rawText,
                position = card.position,
                createdAt = card.createdAt
            )
        )

    override suspend fun bulkUpdateCards(cards: List<Card>) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                cards.chunked(CHUNK_SIZE).forEach { chunk ->
                    cardDao.updateAll(
                        chunk.map { card ->
                            CardEntity(
                                id = card.id,
                                deckId = card.deckId,
                                front = card.front.rawText,
                                back = card.back.rawText,
                                position = card.position,
                                createdAt = card.createdAt,
                                subDeckIndex = card.subDeckIndex
                            )
                        }
                    )
                }
            }
        }
    }

    override suspend fun deleteCard(id: Long) =
        cardDao.deleteById(id)

    override suspend fun deleteCards(cardIds: List<Long>) {
        if (cardIds.isEmpty()) return
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                cardIds.chunked(CHUNK_SIZE).forEach { chunk ->
                    cardDao.deleteByIds(chunk)
                }
            }
        }
    }

    override suspend fun getCardState(cardId: Long): CardState {
        val entity = cardStateDao.getByCardId(cardId)
        return entity?.toDomain() ?: CardState(cardId = cardId)
    }

    override suspend fun getCardStates(deckId: Long): List<CardState> =
        cardStateDao.getByDeckId(deckId).map { it.toDomain() }

    override suspend fun getCardStates(deckIds: List<Long>): List<CardState> =
        if (deckIds.isEmpty()) {
            deckDao.getAll().flatMap { cardStateDao.getByDeckId(it.id) }.map { it.toDomain() }
        } else {
            deckIds.flatMap { cardStateDao.getByDeckId(it) }.map { it.toDomain() }
        }

    override suspend fun recordReview(event: ReviewEvent) {
        reviewHistoryDao.insert(
            ReviewHistoryEntity(
                cardId = event.cardId,
                deckId = event.deckId,
                reviewedAt = event.reviewedAt,
                rating = event.rating.value,
                durationMs = event.durationMs,
                previousInterval = event.previousInterval,
                newInterval = event.newInterval,
                previousQueue = event.previousQueue,
                newQueue = event.newQueue
            )
        )
    }

    override fun observeReviewHistory(
        deckIds: List<Long>,
        fromEpoch: Long,
        toEpoch: Long
    ): Flow<List<ReviewLogEntry>> {
        val flow = if (deckIds.isEmpty()) {
            reviewHistoryDao.observeAll(fromEpoch, toEpoch)
        } else {
            reviewHistoryDao.observeForDecks(deckIds, fromEpoch, toEpoch)
        }
        return flow.map { rows ->
            rows.map { row ->
                ReviewLogEntry(
                    deckId = row.deckId,
                    reviewedAt = row.reviewedAt,
                    rating = ratingFromValue(row.rating)
                )
            }
        }
    }

    override suspend fun reviewHistoryCount(deckIds: List<Long>): Int =
        if (deckIds.isEmpty()) reviewHistoryDao.totalCount() else reviewHistoryDao.totalCountForDecks(deckIds)

    private fun ratingFromValue(value: Int): Rating = when (value) {
        Rating.Again.value -> Rating.Again
        Rating.Hard.value -> Rating.Hard
        Rating.Easy.value -> Rating.Easy
        else -> Rating.Good
    }

    override suspend fun updateCardState(state: CardState) =
        cardStateDao.upsert(
            CardStateEntity(
                cardId = state.cardId,
                due = state.due,
                interval = state.interval,
                ease = state.ease,
                reps = state.reps,
                lapses = state.lapses,
                queue = state.queue,
                stability = state.stability,
                difficulty = state.difficulty,
                fsrsState = state.fsrsState,
                step = state.step,
                lastReview = state.lastReview
            )
        )

    override suspend fun resetProgress(deckId: Long) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                cardStateDao.deleteByDeckId(deckId)
                val cardIds = cardDao.getIdsByDeckId(deckId)
                cardIds.chunked(CHUNK_SIZE).forEach { chunk ->
                    cardStateDao.insertAll(chunk.map { CardStateEntity(cardId = it) })
                }
            }
        }
    }

    override suspend fun resetCardProgress(cardId: Long) {
        cardStateDao.upsert(CardStateEntity(cardId = cardId))
    }

    override suspend fun getNextReviewCard(deckId: Long, now: Long, newLimit: Int, excludeCardId: Long): ReviewCard? {
        // Priority: learning (due now) > review (due now) > new (up to limit)
        val state = cardStateDao.getNextLearningByDeckId(deckId, now, excludeCardId)
            ?: cardStateDao.getNextReviewByDeckId(deckId, now, excludeCardId)
            ?: (if (newLimit > 0) cardStateDao.getNextNewByDeckId(deckId, excludeCardId) else null)
            ?: return null
        val card = cardDao.getById(state.cardId) ?: return null
        return ReviewCard(card.toDomain(), state.toDomain())
    }

    override suspend fun importDeck(
        name: String,
        cards: List<ParsedCard>,
        onProgress: (Float) -> Unit
    ): Long {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val deckId = deckDao.insert(DeckEntity(name = name, dailyLimit = 20, createdAt = now))

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                // Phase 1: Insert all cards in batches
                val cardChunks = cards.chunked(CHUNK_SIZE)
                cardChunks.forEachIndexed { chunkIndex, chunk ->
                    val cardEntities = chunk.mapIndexed { i, parsed ->
                        CardEntity(
                            deckId = deckId,
                            front = parsed.front,
                            back = parsed.back,
                            position = chunkIndex * CHUNK_SIZE + i,
                            createdAt = now,
                            ankiGuid = parsed.ankiGuid
                        )
                    }
                    cardDao.insertAll(cardEntities)
                    onProgress((chunkIndex + 1).toFloat() / cardChunks.size * 0.7f)
                }

                // Phase 2: Create all card states in one pass
                val allCardIds = cardDao.getIdsByDeckId(deckId)
                val stateChunks = allCardIds.chunked(CHUNK_SIZE)
                stateChunks.forEachIndexed { chunkIndex, idChunk ->
                    val states = idChunk.map { cardId -> CardStateEntity(cardId = cardId) }
                    cardStateDao.insertAll(states)
                    onProgress(0.7f + (chunkIndex + 1).toFloat() / stateChunks.size * 0.3f)
                }
            }
        }

        return deckId
    }

    // ==================== Deck patch / update flow ====================

    override suspend fun rebuildPatchHandle(sessionId: String, deckId: Long): DeckPatchHandle? {
        val deck = deckDao.getById(deckId) ?: return null
        val added = patchDiffDao.countByKind(sessionId, PatchDiffEntryEntity.KIND_ADDED)
        val modified = patchDiffDao.countByKind(sessionId, PatchDiffEntryEntity.KIND_MODIFIED)
        val removed = patchDiffDao.countByKind(sessionId, PatchDiffEntryEntity.KIND_REMOVED)
        if (added == 0 && modified == 0 && removed == 0) return null
        return DeckPatchHandle(
            sessionId = sessionId,
            deckId = deckId,
            deckName = deck.name,
            counts = PatchCounts(added = added, modified = modified, removed = removed, unchanged = 0)
        )
    }

    override suspend fun checkDeckPatchGate(deckId: Long): DeckPatchGate {
        val nullCount = cardDao.countNullGuid(deckId)
        return if (nullCount == 0) DeckPatchGate.Ready else DeckPatchGate.NeedsBackfill(nullCount)
    }

    override suspend fun backfillAnkiGuids(
        deckId: Long,
        parsedCards: List<ParsedCard>
    ): BackfillResult {
        // Build hash -> guid map from the user-supplied APKG (bounded by file size).
        val hashToGuid = HashMap<String, String>(parsedCards.size)
        for (parsed in parsedCards) {
            val guid = parsed.ankiGuid ?: continue
            hashToGuid[contentHash(parsed.front, parsed.back)] = guid
        }

        var matched = 0
        var unmatched = 0
        // Track guids already used so two legacy rows don't both claim the same guid.
        val usedGuids = HashSet<String>()

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                var afterId = -1L
                while (true) {
                    val chunk = cardDao.pageNullGuidByDeck(deckId, afterId, CHUNK_SIZE)
                    if (chunk.isEmpty()) break
                    afterId = chunk.last().id
                    for (row in chunk) {
                        val hash = contentHash(row.front, row.back)
                        val guid = hashToGuid[hash]
                        if (guid != null && usedGuids.add(guid)) {
                            cardDao.setAnkiGuid(row.id, guid)
                            matched++
                        } else {
                            unmatched++
                        }
                    }
                }
            }
        }
        return BackfillResult(matched = matched, unmatched = unmatched)
    }

    override suspend fun previewDeckPatch(
        deckId: Long,
        parsedCards: List<ParsedCard>
    ): DeckPatchHandle {
        val deck = deckDao.getById(deckId) ?: error("Deck $deckId not found")
        val sessionId = generateSessionId()
        val now = kotlin.time.Clock.System.now().epochSeconds

        // Vacuum stale sessions older than 24 h up front.
        patchDiffDao.vacuumOlderThan(now - 24L * 60 * 60)

        // Index incoming APKG by guid. Cards from a patch APKG are expected to have guids — skip
        // any without (they cannot be matched).
        val parsedByGuid = HashMap<String, ParsedCard>(parsedCards.size)
        for (parsed in parsedCards) {
            val guid = parsed.ankiGuid ?: continue
            parsedByGuid[guid] = parsed
        }

        var addedCount = 0
        var modifiedCount = 0
        var removedCount = 0
        var unchangedCount = 0
        val seenGuids = HashSet<String>(parsedByGuid.size)

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                var afterId = -1L
                while (true) {
                    val chunk = cardDao.pageByDeck(deckId, afterId, CHUNK_SIZE)
                    if (chunk.isEmpty()) break
                    afterId = chunk.last().id

                    val diffEntries = ArrayList<PatchDiffEntryEntity>()
                    for (row in chunk) {
                        val guid = row.ankiGuid
                        val parsed = if (guid != null) parsedByGuid[guid] else null
                        if (parsed != null && guid != null) {
                            seenGuids.add(guid)
                            if (parsed.front == row.front && parsed.back == row.back) {
                                unchangedCount++
                            } else {
                                modifiedCount++
                                diffEntries.add(
                                    PatchDiffEntryEntity(
                                        sessionId = sessionId,
                                        kind = PatchDiffEntryEntity.KIND_MODIFIED,
                                        existingCardId = row.id,
                                        newAnkiGuid = guid,
                                        newFront = parsed.front,
                                        newBack = parsed.back,
                                        createdAt = now
                                    )
                                )
                            }
                        } else {
                            removedCount++
                            diffEntries.add(
                                PatchDiffEntryEntity(
                                    sessionId = sessionId,
                                    kind = PatchDiffEntryEntity.KIND_REMOVED,
                                    existingCardId = row.id,
                                    newAnkiGuid = null,
                                    newFront = row.front,
                                    newBack = row.back,
                                    createdAt = now
                                )
                            )
                        }
                    }
                    if (diffEntries.isNotEmpty()) patchDiffDao.insertAll(diffEntries)
                }

                // Drain remaining APKG entries -> added.
                val addedEntries = ArrayList<PatchDiffEntryEntity>(parsedByGuid.size - seenGuids.size)
                for ((guid, parsed) in parsedByGuid) {
                    if (guid in seenGuids) continue
                    addedCount++
                    addedEntries.add(
                        PatchDiffEntryEntity(
                            sessionId = sessionId,
                            kind = PatchDiffEntryEntity.KIND_ADDED,
                            existingCardId = null,
                            newAnkiGuid = guid,
                            newFront = parsed.front,
                            newBack = parsed.back,
                            createdAt = now
                        )
                    )
                    if (addedEntries.size >= CHUNK_SIZE) {
                        patchDiffDao.insertAll(addedEntries)
                        addedEntries.clear()
                    }
                }
                if (addedEntries.isNotEmpty()) patchDiffDao.insertAll(addedEntries)
            }
        }

        return DeckPatchHandle(
            sessionId = sessionId,
            deckId = deckId,
            deckName = deck.name,
            counts = PatchCounts(
                added = addedCount,
                modified = modifiedCount,
                removed = removedCount,
                unchanged = unchangedCount
            )
        )
    }

    override suspend fun pageAddedCards(
        handle: DeckPatchHandle,
        offset: Int,
        limit: Int
    ): List<DeckPatchPreviewItem.Added> =
        patchDiffDao.page(handle.sessionId, PatchDiffEntryEntity.KIND_ADDED, offset, limit).map {
            DeckPatchPreviewItem.Added(front = it.newFront.orEmpty(), back = it.newBack.orEmpty())
        }

    override suspend fun pageModifiedCards(
        handle: DeckPatchHandle,
        offset: Int,
        limit: Int
    ): List<DeckPatchPreviewItem.Modified> {
        val rows = patchDiffDao.page(handle.sessionId, PatchDiffEntryEntity.KIND_MODIFIED, offset, limit)
        return rows.mapNotNull { row ->
            val cardId = row.existingCardId ?: return@mapNotNull null
            val current = cardDao.getById(cardId) ?: return@mapNotNull null
            DeckPatchPreviewItem.Modified(
                cardId = cardId,
                oldFront = current.front,
                oldBack = current.back,
                newFront = row.newFront.orEmpty(),
                newBack = row.newBack.orEmpty()
            )
        }
    }

    override suspend fun pageRemovedCards(
        handle: DeckPatchHandle,
        offset: Int,
        limit: Int
    ): List<DeckPatchPreviewItem.Removed> =
        patchDiffDao.page(handle.sessionId, PatchDiffEntryEntity.KIND_REMOVED, offset, limit).mapNotNull {
            val id = it.existingCardId ?: return@mapNotNull null
            DeckPatchPreviewItem.Removed(
                cardId = id,
                front = it.newFront.orEmpty(),
                back = it.newBack.orEmpty()
            )
        }

    override suspend fun applyDeckPatch(handle: DeckPatchHandle, keepRemovedIds: Set<Long>) {
        val now = kotlin.time.Clock.System.now().epochSeconds

        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                // 1) Modified — update content in place, preserve card_states untouched.
                var offset = 0
                while (true) {
                    val rows = patchDiffDao.page(
                        handle.sessionId,
                        PatchDiffEntryEntity.KIND_MODIFIED,
                        offset,
                        CHUNK_SIZE
                    )
                    if (rows.isEmpty()) break
                    for (row in rows) {
                        val cardId = row.existingCardId ?: continue
                        cardDao.updateContent(
                            cardId = cardId,
                            front = row.newFront.orEmpty(),
                            back = row.newBack.orEmpty()
                        )
                    }
                    offset += rows.size
                    if (rows.size < CHUNK_SIZE) break
                }

                // 2) Added — insert cards with default states, append at the end.
                var nextPosition = cardDao.nextPosition(handle.deckId)
                offset = 0
                while (true) {
                    val rows = patchDiffDao.page(
                        handle.sessionId,
                        PatchDiffEntryEntity.KIND_ADDED,
                        offset,
                        CHUNK_SIZE
                    )
                    if (rows.isEmpty()) break
                    val cardEntities = rows.map { row ->
                        CardEntity(
                            deckId = handle.deckId,
                            front = row.newFront.orEmpty(),
                            back = row.newBack.orEmpty(),
                            position = nextPosition++,
                            createdAt = now,
                            ankiGuid = row.newAnkiGuid
                        )
                    }
                    val insertedIds = cardDao.insertAll(cardEntities)
                    if (insertedIds.isNotEmpty()) {
                        cardStateDao.insertAll(insertedIds.map { CardStateEntity(cardId = it) })
                    }
                    offset += rows.size
                    if (rows.size < CHUNK_SIZE) break
                }

                // 3) Removed — delete only the ones the user did NOT mark to keep.
                offset = 0
                while (true) {
                    val ids = patchDiffDao.pageExistingCardIds(
                        handle.sessionId,
                        PatchDiffEntryEntity.KIND_REMOVED,
                        offset,
                        CHUNK_SIZE
                    )
                    if (ids.isEmpty()) break
                    val toDelete = ids.filterNot { it in keepRemovedIds }
                    if (toDelete.isNotEmpty()) cardDao.deleteByIds(toDelete)
                    offset += ids.size
                    if (ids.size < CHUNK_SIZE) break
                }

                // 4) Clean up the session.
                patchDiffDao.deleteSession(handle.sessionId)
            }
        }
    }

    override suspend fun discardDeckPatch(handle: DeckPatchHandle) {
        patchDiffDao.deleteSession(handle.sessionId)
    }

    private fun contentHash(front: String, back: String): String {
        // Lightweight stable hash for content matching during legacy backfill. We only need
        // collision-resistance within a single deck (a few thousand rows), so a 64-bit fnv-1a
        // with a separator covers it without depending on a crypto library.
        val combined = front + "" + back
        var h = 0xcbf29ce484222325uL
        val prime = 0x100000001b3uL
        for (i in combined.indices) {
            h = (h xor combined[i].code.toULong()) * prime
        }
        return h.toString(16)
    }

    private fun generateSessionId(): String {
        val r = Random.Default
        val a = r.nextLong()
        val b = r.nextLong()
        return a.toString(16) + "-" + b.toString(16)
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

    override suspend fun getNextSubDeckReviewCard(
        deckId: Long,
        subDeckIndex: Int,
        now: Long,
        newLimit: Int,
        excludeCardId: Long
    ): ReviewCard? {
        val state = cardStateDao.getNextLearningByDeckIdAndSubDeck(deckId, subDeckIndex, now, excludeCardId)
            ?: cardStateDao.getNextReviewByDeckIdAndSubDeck(deckId, subDeckIndex, now, excludeCardId)
            ?: (
                if (newLimit >
                    0
                ) {
                    cardStateDao.getNextNewByDeckIdAndSubDeck(deckId, subDeckIndex, excludeCardId)
                } else {
                    null
                }
                )
            ?: return null
        val card = cardDao.getById(state.cardId) ?: return null
        return ReviewCard(card.toDomain(), state.toDomain())
    }

    override suspend fun cloneSubDeckAsNewDeck(deckId: Long, subDeckIndex: Int, newName: String): Long {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val newDeckId = deckDao.insert(DeckEntity(name = newName, dailyLimit = 20, createdAt = now))
        val cardEntities = cardDao.getByDeckIdAndSubDeck(deckId, subDeckIndex)
        cardEntities.forEachIndexed { i, card ->
            val newCardId = cardDao.insert(
                CardEntity(
                    deckId = newDeckId,
                    front = card.front,
                    back = card.back,
                    position = i,
                    createdAt = now
                )
            )
            cardStateDao.upsert(CardStateEntity(cardId = newCardId))
        }
        return newDeckId
    }

    override suspend fun createReversedDeck(deckId: Long, newName: String): Long {
        val now = kotlin.time.Clock.System.now().epochSeconds
        val newDeckId = deckDao.insert(DeckEntity(name = newName, dailyLimit = 20, createdAt = now))
        val cardEntities = cardDao.getByDeckId(deckId)
        cardEntities.forEachIndexed { i, card ->
            val newCardId = cardDao.insert(
                CardEntity(
                    deckId = newDeckId,
                    front = card.back,
                    back = card.front,
                    position = i,
                    createdAt = now
                )
            )
            cardStateDao.upsert(CardStateEntity(cardId = newCardId))
        }
        return newDeckId
    }

    private fun DeckEntity.toDomain(): Deck {
        val parsedType = try {
            SchedulerType.valueOf(schedulerType)
        } catch (_: Exception) {
            SchedulerType.SM2
        }
        val params = FsrsParameters.deserialize(fsrsParams)
        return Deck(
            id = id,
            name = name,
            dailyLimit = dailyLimit,
            createdAt = createdAt,
            schedulerType = parsedType,
            fsrsDesiredRetention = params.desiredRetention,
            fsrsLearningSteps = FsrsParameters.formatStepsString(params.learningStepsSeconds),
            fsrsRelearningSteps = FsrsParameters.formatStepsString(params.relearningStepsSeconds),
            fsrsMaxInterval = params.maximumInterval,
            fsrsEnableFuzzing = params.enableFuzzing
        )
    }
    private fun CardEntity.toDomain() = Card(
        id,
        deckId,
        contentParser.detect(front),
        contentParser.detect(back),
        position,
        createdAt,
        subDeckIndex
    )
    private fun CardStateEntity.toDomain() = CardState(
        cardId, due, interval, ease, reps, lapses, queue,
        stability, difficulty, fsrsState, step, lastReview
    )

    companion object {
        const val CHUNK_SIZE = 5000
        const val PAGE_SIZE = 30
    }
}
