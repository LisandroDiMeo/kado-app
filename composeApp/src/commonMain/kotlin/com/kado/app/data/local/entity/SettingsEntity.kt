package com.kado.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "system",
    val cardFontScale: Float = 1.0f,
    val appFontScale: Float = 1.0f,
    val language: String = "en"
)
