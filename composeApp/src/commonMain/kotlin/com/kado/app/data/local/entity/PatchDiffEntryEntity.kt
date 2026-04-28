package com.kado.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patch_diff_entries",
    indices = [Index(value = ["sessionId", "kind"])]
)
data class PatchDiffEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val kind: Int,
    val existingCardId: Long? = null,
    val newAnkiGuid: String? = null,
    val newFront: String? = null,
    val newBack: String? = null,
    val createdAt: Long
) {
    companion object {
        const val KIND_ADDED = 0
        const val KIND_MODIFIED = 1
        const val KIND_REMOVED = 2
    }
}
