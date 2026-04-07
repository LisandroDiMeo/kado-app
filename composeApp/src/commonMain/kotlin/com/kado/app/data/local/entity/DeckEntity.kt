package com.kado.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val dailyLimit: Int = 20,
    val schedulerType: String = "SM2",
    val fsrsParams: String? = null
)
