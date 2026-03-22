package com.kado.app.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.localization.S

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { KadoTopBar(title = S().about, onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = S().appName,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = S().aboutDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = S().kadoLite,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = S().kadoLiteDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = S().contribute,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = S().contributeDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = S().builtWith,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = S().builtWithDescription,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
