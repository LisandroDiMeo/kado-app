package com.kado.app.presentation.screens.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.presentation.components.FlashCard
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.LoadingState
import com.kado.app.presentation.components.RatingBar
import com.kado.app.presentation.localization.S

@Composable
fun ReviewScreen(
    deckId: Long,
    subDeckIndex: Int? = null,
    onBack: () -> Unit,
    onEditCard: (Long) -> Unit = {},
    vm: ReviewViewModel = viewModel { ReviewViewModel(deckId, subDeckIndex) }
) {
    val uiState by vm.uiState.collectAsState()
    val animatedAlpha by animateFloatAsState(if (uiState.hasBeenFlipped) 1f else 0f)

    Scaffold(
        topBar = {
            KadoTopBar(
                title = S().review,
                onBack = onBack,
                actions = {
                    val currentCardId = uiState.currentCard?.card?.id
                    if (currentCardId != null && !uiState.isFinished) {
                        IconButton(onClick = { onEditCard(currentCardId) }) {
                            Text("✏️") // pencil emoji, matches the existing emoji-icon style (eg. preview eye 👁️)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(Modifier.padding(padding))
            uiState.isFinished -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(S().sessionComplete, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text(S().reviewedCount(uiState.summary.reviewed))
                    Text(S().againHardCount(uiState.summary.again, uiState.summary.hard))
                    Text(S().goodEasyCount(uiState.summary.good, uiState.summary.easy))
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onBack) { Text(S().done) }
                }
            }

            uiState.currentCard != null && uiState.frontContent != null && uiState.backContent != null -> {
                val card = uiState.currentCard!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    FlashCard(
                        modifier = Modifier.align(Alignment.CenterHorizontally).weight(1f),
                        front = uiState.frontContent!!,
                        back = uiState.backContent!!,
                        isFlipped = uiState.isFlipped,
                        onFlip = vm::flip,
                        deckId = card.card.deckId
                    )
                    Spacer(Modifier.weight(1f))
                    RatingBar(
                        onRate = vm::rate,
                        intervals = uiState.intervals,
                        modifier = Modifier.graphicsLayer {
                            alpha = animatedAlpha
                        }
                    )
                }
            }
        }
    }
}
