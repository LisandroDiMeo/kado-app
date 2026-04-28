package com.kado.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kado.app.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY position ASC")
    fun observeByDeckId(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY position ASC")
    suspend fun getByDeckId(deckId: Long): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    suspend fun countByDeckId(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    fun observeCountByDeckId(deckId: Long): Flow<Int>

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY position ASC LIMIT :limit OFFSET :offset")
    suspend fun getByDeckIdPaged(deckId: Long, limit: Int, offset: Int): List<CardEntity>

    @Query("SELECT id FROM cards WHERE deckId = :deckId")
    suspend fun getIdsByDeckId(deckId: Long): List<Long>

    @Insert
    suspend fun insert(card: CardEntity): Long

    @Insert
    suspend fun insertAll(cards: List<CardEntity>): List<Long>

    @Update
    suspend fun update(card: CardEntity)

    @Update
    suspend fun updateAll(cards: List<CardEntity>)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM cards WHERE deckId = :deckId")
    suspend fun deleteByDeckId(deckId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM cards WHERE deckId = :deckId")
    suspend fun nextPosition(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex ORDER BY position ASC")
    suspend fun getByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int): List<CardEntity>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex ORDER BY position ASC")
    fun observeByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int): Flow<List<CardEntity>>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex")
    suspend fun countByDeckIdAndSubDeck(deckId: Long, subDeckIndex: Int): Int

    @Query(
        "SELECT DISTINCT subDeckIndex FROM cards WHERE deckId = :deckId AND subDeckIndex IS NOT NULL ORDER BY subDeckIndex ASC"
    )
    suspend fun getSubDeckIndices(deckId: Long): List<Int>

    @Query(
        "SELECT DISTINCT subDeckIndex FROM cards WHERE deckId = :deckId AND subDeckIndex IS NOT NULL ORDER BY subDeckIndex ASC"
    )
    fun observeSubDeckIndices(deckId: Long): Flow<List<Int>>

    @Query("UPDATE cards SET subDeckIndex = :subDeckIndex WHERE id = :cardId")
    suspend fun updateSubDeckIndex(cardId: Long, subDeckIndex: Int?)

    @Query("UPDATE cards SET subDeckIndex = NULL WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex")
    suspend fun clearSubDeckIndex(deckId: Long, subDeckIndex: Int)

    @Query("UPDATE cards SET subDeckIndex = NULL WHERE deckId = :deckId AND subDeckIndex IS NOT NULL")
    suspend fun clearAllSubDeckIndices(deckId: Long)

    @Query("UPDATE cards SET subDeckIndex = subDeckIndex - 1 WHERE deckId = :deckId AND subDeckIndex > :removedIndex")
    suspend fun shiftSubDeckIndicesDown(deckId: Long, removedIndex: Int)

    @Query("DELETE FROM cards WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND ankiGuid IS NULL")
    suspend fun countNullGuid(deckId: Long): Int

    @Query(
        "SELECT * FROM cards WHERE deckId = :deckId AND ankiGuid IS NULL AND id > :afterId " +
            "ORDER BY id ASC LIMIT :limit"
    )
    suspend fun pageNullGuidByDeck(deckId: Long, afterId: Long, limit: Int): List<CardEntity>

    @Query(
        "SELECT * FROM cards WHERE deckId = :deckId AND id > :afterId ORDER BY id ASC LIMIT :limit"
    )
    suspend fun pageByDeck(deckId: Long, afterId: Long, limit: Int): List<CardEntity>

    @Query("UPDATE cards SET ankiGuid = :guid WHERE id = :cardId")
    suspend fun setAnkiGuid(cardId: Long, guid: String)

    @Query("UPDATE cards SET front = :front, back = :back WHERE id = :cardId")
    suspend fun updateContent(cardId: Long, front: String, back: String)
}
