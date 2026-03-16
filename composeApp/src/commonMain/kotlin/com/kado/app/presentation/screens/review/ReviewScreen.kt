package com.kado.app.presentation.screens.review

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.presentation.components.FlashCard
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.LoadingState
import com.kado.app.presentation.components.RatingBar

@Composable
fun ReviewScreen(
    deckId: Long,
    subDeckIndex: Int? = null,
    onBack: () -> Unit,
    vm: ReviewViewModel = viewModel { ReviewViewModel(deckId, subDeckIndex) }
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = { KadoTopBar(title = "Review", onBack = onBack) }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(Modifier.padding(padding))
            uiState.isFinished -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Session Complete", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text("Reviewed: ${uiState.summary.reviewed}")
                    Text("Again: ${uiState.summary.again}  Hard: ${uiState.summary.hard}")
                    Text("Good: ${uiState.summary.good}  Easy: ${uiState.summary.easy}")
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onBack) { Text("Done") }
                }
            }
            uiState.currentCard != null -> {
                val card = uiState.currentCard!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedContent(
                        targetState = card.card.id,
                        transitionSpec = { fadeIn(initialAlpha = 0.3f) togetherWith fadeOut(targetAlpha = 0.3f) }
                    ) { cardId ->
                        // Capture card for this animation state
                        val animCard = if (cardId == card.card.id) card else card
                        FlashCard(
                            front = animCard.card.front,
                            back = animCard.card.back,
                            isFlipped = uiState.isFlipped,
                            onFlip = vm::flip
                        )
                    }
                    if (uiState.hasBeenFlipped) {
                        RatingBar(
                            onRate = vm::rate,
                            intervals = uiState.intervals
                        )
                    } else {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}
