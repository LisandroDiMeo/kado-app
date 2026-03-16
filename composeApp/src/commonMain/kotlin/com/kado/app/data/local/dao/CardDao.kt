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

    @Insert
    suspend fun insert(card: CardEntity): Long

    @Insert
    suspend fun insertAll(cards: List<CardEntity>)

    @Update
    suspend fun update(card: CardEntity)

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

    @Query("SELECT DISTINCT subDeckIndex FROM cards WHERE deckId = :deckId AND subDeckIndex IS NOT NULL ORDER BY subDeckIndex ASC")
    suspend fun getSubDeckIndices(deckId: Long): List<Int>

    @Query("SELECT DISTINCT subDeckIndex FROM cards WHERE deckId = :deckId AND subDeckIndex IS NOT NULL ORDER BY subDeckIndex ASC")
    fun observeSubDeckIndices(deckId: Long): Flow<List<Int>>

    @Query("UPDATE cards SET subDeckIndex = :subDeckIndex WHERE id = :cardId")
    suspend fun updateSubDeckIndex(cardId: Long, subDeckIndex: Int?)

    @Query("UPDATE cards SET subDeckIndex = NULL WHERE deckId = :deckId AND subDeckIndex = :subDeckIndex")
    suspend fun clearSubDeckIndex(deckId: Long, subDeckIndex: Int)

    @Query("UPDATE cards SET subDeckIndex = NULL WHERE deckId = :deckId AND subDeckIndex IS NOT NULL")
    suspend fun clearAllSubDeckIndices(deckId: Long)

    @Query("UPDATE cards SET subDeckIndex = subDeckIndex - 1 WHERE deckId = :deckId AND subDeckIndex > :removedIndex")
    suspend fun shiftSubDeckIndicesDown(deckId: Long, removedIndex: Int)
}
