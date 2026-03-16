package com.kado.app.data.repository

import com.kado.app.data.local.dao.SettingsDao
import com.kado.app.data.local.entity.SettingsEntity
import com.kado.app.domain.model.AppSettings
import com.kado.app.domain.model.ThemeMode
import com.kado.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> =
        settingsDao.observeSettings().map { entity ->
            entity?.toDomain() ?: AppSettings()
        }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        val current = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.upsert(current.copy(themeMode = mode.toEntity()))
    }

    override suspend fun updateCardFontScale(scale: Float) {
        val current = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.upsert(current.copy(cardFontScale = scale))
    }

    override suspend fun updateAppFontScale(scale: Float) {
        val current = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.upsert(current.copy(appFontScale = scale))
    }

    private fun SettingsEntity.toDomain() = AppSettings(
        themeMode = when (themeMode) {
            "light" -> ThemeMode.Light
            "dark" -> ThemeMode.Dark
            else -> ThemeMode.System
        },
        cardFontScale = cardFontScale,
        appFontScale = appFontScale
    )

    private fun ThemeMode.toEntity() = when (this) {
        ThemeMode.System -> "system"
        ThemeMode.Light -> "light"
        ThemeMode.Dark -> "dark"
    }
}
