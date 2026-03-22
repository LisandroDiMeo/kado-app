package com.kado.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.AppSettings
import com.kado.app.domain.model.ThemeMode
import com.kado.app.presentation.localization.LocalAppLanguage
import com.kado.app.presentation.navigation.KadoNavHost
import com.kado.app.ui.theme.AppTheme
import com.kado.app.ui.theme.LocalCardFontScale

@Composable
fun App() {
    val settings by AppDependencies.settingsRepository
        .observeSettings()
        .collectAsState(initial = AppSettings())

    val darkTheme = when (settings.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    AppTheme(darkTheme = darkTheme, appFontScale = settings.appFontScale) {
        CompositionLocalProvider(
            LocalCardFontScale provides settings.cardFontScale,
            LocalAppLanguage provides settings.language
        ) {
            val navController = rememberNavController()
            KadoNavHost(navController = navController)
        }
    }
}
