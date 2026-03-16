package com.kado.app.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.domain.model.ThemeMode
import com.kado.app.presentation.components.KadoTopBar
import kotlin.math.roundToInt

@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    vm: AppSettingsViewModel = viewModel { AppSettingsViewModel() }
) {
    val settings by vm.settings.collectAsState()

    Scaffold(
        topBar = { KadoTopBar(title = "App Settings", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme mode
            Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { vm.setThemeMode(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    ThemeMode.System -> "System"
                                    ThemeMode.Light -> "Light"
                                    ThemeMode.Dark -> "Dark"
                                }
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Card font scale
            Text(text = "Card Font Size", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(settings.cardFontScale * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = settings.cardFontScale,
                onValueChange = { vm.setCardFontScale(it) },
                valueRange = 0.8f..1.5f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // App font scale
            Text(text = "App Font Size", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(settings.appFontScale * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = settings.appFontScale,
                onValueChange = { vm.setAppFontScale(it) },
                valueRange = 0.8f..1.5f,
                steps = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
