package com.kado.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.AppLanguage
import com.kado.app.domain.model.AppSettings
import com.kado.app.domain.model.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AppSettingsViewModel : ViewModel() {

    private val repository = AppDependencies.settingsRepository

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        repository.observeSettings()
            .onEach { _settings.value = it }
            .launchIn(viewModelScope)
    }

    fun setThemeMode(mode: ThemeMode) {
        _settings.value = _settings.value.copy(themeMode = mode)
        viewModelScope.launch { repository.updateThemeMode(mode) }
    }

    private var cardFontJob: Job? = null
    fun setCardFontScale(scale: Float) {
        _settings.value = _settings.value.copy(cardFontScale = scale)
        cardFontJob?.cancel()
        cardFontJob = viewModelScope.launch {
            delay(DELAY_SCALE)
            repository.updateCardFontScale(scale)
        }
    }

    private var appFontJob: Job? = null
    fun setAppFontScale(scale: Float) {
        _settings.value = _settings.value.copy(appFontScale = scale)
        appFontJob?.cancel()
        appFontJob = viewModelScope.launch {
            delay(DELAY_SCALE)
            repository.updateAppFontScale(scale)
        }
    }

    fun setLanguage(language: AppLanguage) {
        _settings.value = _settings.value.copy(language = language)
        viewModelScope.launch { repository.updateLanguage(language) }
    }

    companion object {
        const val DELAY_SCALE = 300L
    }
}
