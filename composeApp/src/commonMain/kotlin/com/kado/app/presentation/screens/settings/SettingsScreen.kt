package com.kado.app.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kado.app.AppInfo
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.localization.S

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
    onDonateClick: () -> Unit
) {
    Scaffold(
        topBar = { KadoTopBar(title = S().settings, onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SettingsRow(label = S().about, onClick = onAboutClick)
                HorizontalDivider()
                SettingsRow(label = S().appSettings, onClick = onAppSettingsClick)
                HorizontalDivider()
                SettingsRow(label = S().donate, onClick = onDonateClick)
                HorizontalDivider()
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = S().versionLabel(AppInfo.VERSION),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = ">", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
