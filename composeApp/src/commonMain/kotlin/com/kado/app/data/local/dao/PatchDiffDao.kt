package com.kado.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kado.app.data.local.entity.PatchDiffEntryEntity

@Dao
interface PatchDiffDao {
    @Insert
    suspend fun insertAll(entries: List<PatchDiffEntryEntity>)

    @Query("SELECT COUNT(*) FROM patch_diff_entries WHERE sessionId = :sessionId AND kind = :kind")
    suspend fun countByKind(sessionId: String, kind: Int): Int

    @Query(
        "SELECT * FROM patch_diff_entries WHERE sessionId = :sessionId AND kind = :kind " +
            "ORDER BY id LIMIT :limit OFFSET :offset"
    )
    suspend fun page(sessionId: String, kind: Int, offset: Int, limit: Int): List<PatchDiffEntryEntity>

    @Query("SELECT * FROM patch_diff_entries WHERE sessionId = :sessionId AND kind = :kind ORDER BY id")
    suspend fun all(sessionId: String, kind: Int): List<PatchDiffEntryEntity>

    @Query("DELETE FROM patch_diff_entries WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM patch_diff_entries WHERE createdAt < :before")
    suspend fun vacuumOlderThan(before: Long)

    @Query(
        "SELECT existingCardId FROM patch_diff_entries " +
            "WHERE sessionId = :sessionId AND kind = :kind AND existingCardId IS NOT NULL " +
            "ORDER BY id LIMIT :limit OFFSET :offset"
    )
    suspend fun pageExistingCardIds(sessionId: String, kind: Int, offset: Int, limit: Int): List<Long>
}
