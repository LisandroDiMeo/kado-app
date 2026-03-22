package com.kado.app.presentation.screens.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kado.app.domain.model.Tutorial
import com.kado.app.domain.model.TutorialStep
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.TutorialDialog
import com.kado.app.presentation.localization.S

@Composable
private fun appTutorial(): Tutorial {
    val s = S()
    return remember(s) {
        Tutorial(
            title = s.howToUseApp,
            steps = listOf(
                TutorialStep(title = s.tutorialCreateDeckTitle, description = s.tutorialCreateDeckDesc),
                TutorialStep(title = s.tutorialAddCardsTitle, description = s.tutorialAddCardsDesc),
                TutorialStep(title = s.tutorialImportAnkiTitle, description = s.tutorialImportAnkiDesc),
                TutorialStep(title = s.tutorialReviewCardsTitle, description = s.tutorialReviewCardsDesc),
                TutorialStep(title = s.tutorialRateRecallTitle, description = s.tutorialRateRecallDesc),
            )
        )
    }
}

@Composable
private fun kadoLiteTutorial(): Tutorial {
    val s = S()
    return remember(s) {
        Tutorial(
            title = s.howToUseKadoLite,
            steps = listOf(
                TutorialStep(title = s.tutorialPowerOnTitle, description = s.tutorialPowerOnDesc),
                TutorialStep(title = s.tutorialConnectTitle, description = s.tutorialConnectDesc),
                TutorialStep(title = s.tutorialTransferDeckTitle, description = s.tutorialTransferDeckDesc),
                TutorialStep(title = s.tutorialReviewDeviceTitle, description = s.tutorialReviewDeviceDesc),
                TutorialStep(title = s.tutorialSyncBackTitle, description = s.tutorialSyncBackDesc),
            )
        )
    }
}

@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    var activeTutorial by remember { mutableStateOf<Tutorial?>(null) }

    val appTut = appTutorial()
    val kadoLiteTut = kadoLiteTutorial()

    activeTutorial?.let { tutorial ->
        TutorialDialog(
            tutorial = tutorial,
            onDismiss = { activeTutorial = null }
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = S().help,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { activeTutorial = appTut },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(S().howToUseApp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { activeTutorial = kadoLiteTut },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(S().howToUseKadoLite)
            }
        }
    }
}
