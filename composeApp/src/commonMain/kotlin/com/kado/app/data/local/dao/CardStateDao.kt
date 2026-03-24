package com.kado.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kado.app.data.local.entity.CardStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardStateDao {
    @Query("SELECT * FROM card_states WHERE cardId = :cardId")
    suspend fun getByCardId(cardId: Long): CardStateEntity?

    @Query("SELECT * FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId)")
    suspend fun getByDeckId(deckId: Long): List<CardStateEntity>

    @Query("SELECT COUNT(*) FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId) AND queue = 0")
    suspend fun countNewByDeckId(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId) AND (queue = 0 OR due <= :now)")
    suspend fun countDueByDeckId(deckId: Long, now: Long): Int

    @Query("SELECT COUNT(*) FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId) AND queue = 1")
    suspend fun countLearningByDeckId(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId) AND queue = 2 AND due <= :now")
    suspend fun countReviewByDeckId(deckId: Long, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: CardStateEntity)

    @Insert
    suspend fun insertAll(states: List<CardStateEntity>)

    @Query("DELETE FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId)")
    suspend fun deleteByDeckId(deckId: Long)

    @Query("SELECT COUNT(*) FROM card_states")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex) AND queue = 0")
    suspend fun countNewByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int): Int

    @Query("SELECT COUNT(*) FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex) AND (queue = 0 OR due <= :now)")
    suspend fun countDueByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int, now: Long): Int

    @Query("SELECT * FROM card_states WHERE cardId IN (SELECT id FROM cards WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex)")
    suspend fun getByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int): List<CardStateEntity>

    // Next review card queries — push selection logic into SQL
    // Priority 1: learning cards due now
    @Query("""
        SELECT cs.* FROM card_states cs
        INNER JOIN cards c ON cs.cardId = c.id
        WHERE c.deckId = :deckId AND cs.queue = 1 AND cs.due <= :now AND cs.cardId != :excludeCardId
        ORDER BY c.position ASC LIMIT 1
    """)
    suspend fun getNextLearningByDeckId(deckId: Long, now: Long, excludeCardId: Long = -1): CardStateEntity?

    // Priority 2: review cards due now
    @Query("""
        SELECT cs.* FROM card_states cs
        INNER JOIN cards c ON cs.cardId = c.id
        WHERE c.deckId = :deckId AND cs.queue = 2 AND cs.due <= :now AND cs.cardId != :excludeCardId
        ORDER BY c.position ASC LIMIT 1
    """)
    suspend fun getNextReviewByDeckId(deckId: Long, now: Long, excludeCardId: Long = -1): CardStateEntity?

    // Priority 3: new cards
    @Query("""
        SELECT cs.* FROM card_states cs
        INNER JOIN cards c ON cs.cardId = c.id
        WHERE c.deckId = :deckId AND cs.queue = 0 AND cs.cardId != :excludeCardId
        ORDER BY c.position ASC LIMIT 1
    """)
    suspend fun getNextNewByDeckId(deckId: Long, excludeCardId: Long = -1): CardStateEntity?

    // Sub-deck variants
    @Query("""
        SELECT cs.* FROM card_states cs
        INNER JOIN cards c ON cs.cardId = c.id
        WHERE c.deckId = :deckId AND c.subDeckIndex = :subDeckIndex AND cs.queue = 1 AND cs.due <= :now AND cs.cardId != :excludeCardId
        ORDER BY c.position ASC LIMIT 1
    """)
    suspend fun getNextLearningByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int, now: Long, excludeCardId: Long = -1): CardStateEntity?

    @Query("""
        SELECT cs.* FROM card_states cs
        INNER JOIN cards c ON cs.cardId = c.id
        WHERE c.deckId = :deckId AND c.subDeckIndex = :subDeckIndex AND cs.queue = 2 AND cs.due <= :now AND cs.cardId != :excludeCardId
        ORDER BY c.position ASC LIMIT 1
    """)
    suspend fun getNextReviewByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int, now: Long, excludeCardId: Long = -1): CardStateEntity?

    @Query("""
        SELECT cs.* FROM card_states cs
        INNER JOIN cards c ON cs.cardId = c.id
        WHERE c.deckId = :deckId AND c.subDeckIndex = :subDeckIndex AND cs.queue = 0 AND cs.cardId != :excludeCardId
        ORDER BY c.position ASC LIMIT 1
    """)
    suspend fun getNextNewByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int, excludeCardId: Long = -1): CardStateEntity?
}
