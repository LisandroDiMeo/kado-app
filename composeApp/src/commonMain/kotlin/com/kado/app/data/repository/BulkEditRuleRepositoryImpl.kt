package com.kado.app.data.repository

import com.kado.app.data.local.dao.BulkEditRuleDao
import com.kado.app.data.local.entity.BulkEditRuleEntity
import com.kado.app.domain.model.BulkEditRule
import com.kado.app.domain.repository.BulkEditRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BulkEditRuleRepositoryImpl(private val dao: BulkEditRuleDao) : BulkEditRuleRepository {

    override fun observeAll(): Flow<List<BulkEditRule>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): BulkEditRule? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(rule: BulkEditRule): Long =
        dao.insert(rule.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    private fun BulkEditRuleEntity.toDomain() = BulkEditRule(
        id = id,
        name = name,
        frontFindPattern = frontFindPattern,
        frontReplacePattern = frontReplacePattern,
        frontIsRegex = frontIsRegex,
        frontEnabled = frontEnabled,
        backFindPattern = backFindPattern,
        backReplacePattern = backReplacePattern,
        backIsRegex = backIsRegex,
        backEnabled = backEnabled,
        createdAt = createdAt
    )

    private fun BulkEditRule.toEntity() = BulkEditRuleEntity(
        id = id,
        name = name,
        frontFindPattern = frontFindPattern,
        frontReplacePattern = frontReplacePattern,
        frontIsRegex = frontIsRegex,
        frontEnabled = frontEnabled,
        backFindPattern = backFindPattern,
        backReplacePattern = backReplacePattern,
        backIsRegex = backIsRegex,
        backEnabled = backEnabled,
        createdAt = createdAt
    )
}
