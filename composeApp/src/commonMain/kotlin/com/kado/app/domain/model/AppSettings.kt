package com.kado.app.domain.model

enum class ThemeMode { System, Light, Dark }

enum class AppLanguage(val tag: String, val displayName: String) {
    English("en", "English"),
    Spanish("es", "Español");

    companion object {
        fun fromTag(tag: String): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: English
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val cardFontScale: Float = 1.0f,
    val appFontScale: Float = 1.0f,
    val language: AppLanguage = AppLanguage.English
)
