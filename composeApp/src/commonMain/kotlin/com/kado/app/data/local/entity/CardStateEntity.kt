package com.kado.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "card_states",
    foreignKeys = [
        ForeignKey(
            entity = CardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardStateEntity(
    @PrimaryKey val cardId: Long,
    val due: Long = 0,
    val interval: Int = 0,
    val ease: Int = 25,
    val reps: Int = 0,
    val lapses: Int = 0,
    val queue: Int = 0, // 0=new, 1=learning, 2=review
    val stability: Double? = null,
    val difficulty: Double? = null,
    val fsrsState: Int? = null,
    val step: Int? = null,
    val lastReview: Long? = null
)
