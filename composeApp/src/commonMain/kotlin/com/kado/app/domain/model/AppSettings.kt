package com.kado.app.domain.model

enum class ThemeMode { System, Light, Dark }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val cardFontScale: Float = 1.0f,
    val appFontScale: Float = 1.0f
)
