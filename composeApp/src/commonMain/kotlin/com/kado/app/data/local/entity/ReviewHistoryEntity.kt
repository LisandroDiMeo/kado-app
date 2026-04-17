package com.kado.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_history",
    indices = [
        Index("deckId"),
        Index("cardId"),
        Index("reviewedAt"),
        Index(value = ["deckId", "reviewedAt"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val deckId: Long,
    val reviewedAt: Long,
    val rating: Int,
    val durationMs: Long,
    val previousInterval: Int,
    val newInterval: Int,
    val previousQueue: Int,
    val newQueue: Int
)
