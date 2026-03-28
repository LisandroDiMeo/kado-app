package com.kado.app.domain.model

data class BulkEditRule(
    val id: Long = 0,
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
