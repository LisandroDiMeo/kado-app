package com.kado.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kado.app.domain.model.Tutorial
import com.kado.app.presentation.localization.S
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

@Composable
fun TutorialDialog(
    tutorial: Tutorial,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val stepCount = tutorial.steps.size

    val currentIndex by remember(stepCount) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val mostVisible = layoutInfo.visibleItemsInfo.maxByOrNull { item ->
                val visiblePortion = min(
                    layoutInfo.viewportEndOffset,
                    item.offset + item.size
                ) - max(layoutInfo.viewportStartOffset, item.offset)
                visiblePortion
            }
            mostVisible?.index ?: 0
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            topBar = {
                KadoTopBar(
                    title = tutorial.title,
                    onBack = onDismiss
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                LazyRow(
                    state = listState,
                    flingBehavior = snapFlingBehavior,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stepCount) { index ->
                        val step = tutorial.steps[index]
                        Card(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(horizontal = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = S().stepOf(index + 1, stepCount),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = step.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(stepCount) { index ->
                        val color by animateColorAsState(
                            targetValue = if (index == currentIndex) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                listState.animateScrollToItem(currentIndex - 1)
                            }
                        },
                        enabled = currentIndex > 0
                    ) {
                        Text(S().back)
                    }

                    if (currentIndex < stepCount - 1) {
                        Button(
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(currentIndex + 1)
                                }
                            }
                        ) {
                            Text(S().next)
                        }
                    } else {
                        Button(onClick = onDismiss) {
                            Text(S().done)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
