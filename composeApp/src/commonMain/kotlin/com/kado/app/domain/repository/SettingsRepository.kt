package com.kado.app.domain.repository

import com.kado.app.domain.model.AppLanguage
import com.kado.app.domain.model.AppSettings
import com.kado.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateCardFontScale(scale: Float)
    suspend fun updateAppFontScale(scale: Float)
    suspend fun updateLanguage(language: AppLanguage)
}
