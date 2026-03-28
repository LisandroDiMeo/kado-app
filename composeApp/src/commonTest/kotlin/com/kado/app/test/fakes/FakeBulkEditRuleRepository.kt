package com.kado.app.test.fakes

import com.kado.app.domain.model.BulkEditRule
import com.kado.app.domain.repository.BulkEditRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBulkEditRuleRepository : BulkEditRuleRepository {

    private val rules = MutableStateFlow<List<BulkEditRule>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<BulkEditRule>> =
        rules.map { it.sortedByDescending { rule -> rule.createdAt } }

    override suspend fun getById(id: Long): BulkEditRule? =
        rules.value.find { it.id == id }

    override suspend fun insert(rule: BulkEditRule): Long {
        val id = nextId++
        val saved = rule.copy(id = id)
        rules.value = rules.value + saved
        return id
    }

    override suspend fun deleteById(id: Long) {
        rules.value = rules.value.filter { it.id != id }
    }
}
