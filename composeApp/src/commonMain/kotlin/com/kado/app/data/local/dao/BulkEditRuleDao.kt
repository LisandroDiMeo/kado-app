package com.kado.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kado.app.data.local.entity.BulkEditRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BulkEditRuleDao {
    @Query("SELECT * FROM bulk_edit_rules ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BulkEditRuleEntity>>

    @Query("SELECT * FROM bulk_edit_rules WHERE id = :id")
    suspend fun getById(id: Long): BulkEditRuleEntity?

    @Insert
    suspend fun insert(rule: BulkEditRuleEntity): Long

    @Query("DELETE FROM bulk_edit_rules WHERE id = :id")
    suspend fun deleteById(id: Long)
}
