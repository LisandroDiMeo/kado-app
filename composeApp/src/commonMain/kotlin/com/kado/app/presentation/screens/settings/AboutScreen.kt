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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.kado.app.presentation.components.KadoTopBar

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { KadoTopBar(title = "About", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Kado",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Kado is a fully open source and minimal alternative to flashcard software. " +
                        "Our goal is to provide a simple, distraction-free way to study using spaced repetition.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Kado Lite",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "We also developed a Lite version for ESP32 microcontrollers " +
                        "(specifically the ESP32-C6 with Waveshare 1.47\" Touch LCD) " +
                        "so you can study your flashcards on the go with a dedicated device.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Contribute",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("Kado is open to contributions! Visit our repository at:\n")
                    withLink(
                        LinkAnnotation.Url(
                            "https://github.com/LisandroDiMeo/kado",
                            TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary))
                        )
                    ) {
                        append("https://github.com/LisandroDiMeo/kado")
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Built With",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "This app is built using Compose Multiplatform, " +
                        "sharing a single codebase for both Android and iOS.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
