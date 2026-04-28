package com.kado.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlin.collections.plus
import kotlin.math.abs

private const val STROKE_SMOOTHNESS = 5

@Composable
fun DrawingCanvas(
    cardId: Long,
    modifier: Modifier = Modifier
) {
    var paths by remember(cardId) { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var currentPath by remember(cardId) { mutableStateOf<List<Offset>?>(null) }

    val strokeColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(cardId) {
                    awaitEachGesture {
                        consumePointerEvent(
                            pathChange = {
                                currentPath = it
                            },
                            pathsChange = {
                                paths = paths + it
                            },
                            onFinish = { currentPath = null }
                        )
                    }
                }
        ) {
            paths.fastForEach { points ->
                drawSmoothPath(points, strokeColor)
            }
            currentPath?.let { drawSmoothPath(it, strokeColor) }
        }

        if (paths.isNotEmpty() || currentPath != null) {
            TextButton(
                onClick = {
                    paths = emptyList()
                    currentPath = null
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Text("🧹")
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.consumePointerEvent(
    pathChange: (List<Offset>) -> Unit,
    pathsChange: (List<List<Offset>>) -> Unit,
    onFinish: () -> Unit
) {
    val down = awaitFirstDown(requireUnconsumed = false)
    var points = listOf(down.position)
    pathChange(points)
    down.consume()
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == down.id } ?: break
        if (!change.pressed) {
            change.consume()
            break
        }
        if (change.position != points.last()) {
            points = points + change.position
            pathChange(points)
        }
        change.consume()
    }
    pathsChange(listOf(points))
    onFinish()
}

private fun DrawScope.drawSmoothPath(
    points: List<Offset>,
    color: Color,
    thickness: Float = 8f
) {
    if (points.isEmpty()) return
    val smoothed = Path().apply {
        moveTo(points.first().x, points.first().y)
        val smoothness = STROKE_SMOOTHNESS
        for (i in 1..points.lastIndex) {
            val from = points[i - 1]
            val to = points[i]
            val dx = abs(from.x - to.x)
            val dy = abs(from.y - to.y)
            if (dx >= smoothness || dy >= smoothness) {
                quadraticTo(
                    x1 = (from.x + to.x) / 2f,
                    y1 = (from.y + to.y) / 2f,
                    x2 = to.x,
                    y2 = to.y
                )
            }
        }
    }
    drawPath(
        path = smoothed,
        color = color,
        style = Stroke(
            width = thickness,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
