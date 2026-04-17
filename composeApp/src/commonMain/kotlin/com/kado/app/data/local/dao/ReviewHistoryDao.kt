package com.kado.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kado.app.data.local.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

data class DeckRatingRow(val deckId: Long, val reviewedAt: Long, val rating: Int)

@Dao
interface ReviewHistoryDao {
    @Insert
    suspend fun insert(row: ReviewHistoryEntity): Long

    @Query("SELECT COUNT(*) FROM review_history")
    suspend fun totalCount(): Int

    @Query("SELECT COUNT(*) FROM review_history WHERE deckId IN (:deckIds)")
    suspend fun totalCountForDecks(deckIds: List<Long>): Int

    // All-deck variants: empty deck list means "every deck"
    @Query(
        "SELECT deckId, reviewedAt, rating FROM review_history " +
            "WHERE reviewedAt >= :fromEpoch AND reviewedAt <= :toEpoch " +
            "ORDER BY reviewedAt ASC"
    )
    fun observeAll(fromEpoch: Long, toEpoch: Long): Flow<List<DeckRatingRow>>

    @Query(
        "SELECT deckId, reviewedAt, rating FROM review_history " +
            "WHERE deckId IN (:deckIds) AND reviewedAt >= :fromEpoch AND reviewedAt <= :toEpoch " +
            "ORDER BY reviewedAt ASC"
    )
    fun observeForDecks(deckIds: List<Long>, fromEpoch: Long, toEpoch: Long): Flow<List<DeckRatingRow>>

    @Query("DELETE FROM review_history WHERE deckId = :deckId")
    suspend fun deleteByDeckId(deckId: Long)
}
