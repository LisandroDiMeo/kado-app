package com.kado.app.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.util.openUrl

private const val DONATE_URL = "https://example.com/donate"

@Composable
fun DonateScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { KadoTopBar(title = "Donate", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Support Kado",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Kado is a free and open source project. " +
                        "If you find it useful and would like to support its continued development, " +
                        "you can make a voluntary donation.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Please note: donations are entirely voluntary and do not grant " +
                        "any digital content, features, services, or advantages within the app. " +
                        "All app features are and will remain free for everyone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { openUrl(DONATE_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Donate")
            }
        }
    }
}
