package com.kado.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.AppSettings
import com.kado.app.domain.model.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel : ViewModel() {

    private val repository = AppDependencies.settingsRepository

    val settings: StateFlow<AppSettings> = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.updateThemeMode(mode) }
    }

    private var cardFontJob: Job? = null
    fun setCardFontScale(scale: Float) {
        cardFontJob?.cancel()
        cardFontJob = viewModelScope.launch {
            delay(300)
            repository.updateCardFontScale(scale)
        }
    }

    private var appFontJob: Job? = null
    fun setAppFontScale(scale: Float) {
        appFontJob?.cancel()
        appFontJob = viewModelScope.launch {
            delay(300)
            repository.updateAppFontScale(scale)
        }
    }
}
