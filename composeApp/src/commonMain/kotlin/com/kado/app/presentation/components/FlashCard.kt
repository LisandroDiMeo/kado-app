package com.kado.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kado.app.ui.theme.LocalCardFontScale
import kotlin.math.abs

@Composable
fun FlashCard(
    front: String,
    back: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )
    val cardScale = LocalCardFontScale.current
    val cardTextStyle = MaterialTheme.typography.headlineSmall.let { style ->
        if (cardScale == 1.0f) style
        else style.copy(fontSize = (style.fontSize.value * cardScale).sp)
    }

    var swipeFired by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .widthIn(max = 384.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(280.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { swipeFired = false },
                    onDragEnd = { swipeFired = false },
                    onDragCancel = { swipeFired = false },
                    onVerticalDrag = { _, dragAmount ->
                        if (!swipeFired && abs(dragAmount) > 10f) {
                            swipeFired = true
                            onFlip()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { onFlip() }
            }
            .graphicsLayer {
                rotationX = rotation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        SelectionContainer {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    Text(
                        text = front,
                        style = cardTextStyle,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = back,
                        style = cardTextStyle,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.graphicsLayer { rotationX = 180f }
                    )
                }
            }
        }
    }
}
