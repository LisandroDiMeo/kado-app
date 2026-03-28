package com.kado.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bulk_edit_rules")
data class BulkEditRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val frontFindPattern: String,
    val frontReplacePattern: String,
    val frontIsRegex: Boolean,
    val frontEnabled: Boolean,
    val backFindPattern: String,
    val backReplacePattern: String,
    val backIsRegex: Boolean,
    val backEnabled: Boolean,
    val createdAt: Long
)
