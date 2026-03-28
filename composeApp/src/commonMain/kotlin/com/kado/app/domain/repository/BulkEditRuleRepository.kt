package com.kado.app.domain.repository

import com.kado.app.domain.model.BulkEditRule
import kotlinx.coroutines.flow.Flow

interface BulkEditRuleRepository {
    fun observeAll(): Flow<List<BulkEditRule>>
    suspend fun getById(id: Long): BulkEditRule?
    suspend fun insert(rule: BulkEditRule): Long
    suspend fun deleteById(id: Long)
}
