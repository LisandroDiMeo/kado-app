package com.kado.app.presentation.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.kado.app.domain.model.RatingBreakdown

private const val BAR_GAP_DP = 2
private const val ANIM_DURATION_MS = 500
private const val AXIS_STROKE_DP = 1
private const val AXIS_PADDING_DP = 28
private const val Y_AXIS_WIDTH_DP = 36
private const val GRID_LINES = 4
private const val GRID_ALPHA = 0.25f
private const val LABEL_PADDING_PX = 4f
private const val X_LABEL_TOP_PADDING_PX = 6f

@Composable
fun StackedBarChart(
    entries: List<RatingBreakdown>,
    yMax: Int,
    xLabels: Map<Int, String> = emptyMap(),
    modifier: Modifier = Modifier,
    againColor: Color = MaterialTheme.colorScheme.error,
    hardColor: Color = MaterialTheme.colorScheme.secondary,
    goodColor: Color = MaterialTheme.colorScheme.tertiary,
    easyColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.outline,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val progress by animateFloatAsState(
        targetValue = if (entries.isEmpty()) 0f else 1f,
        animationSpec = tween(ANIM_DURATION_MS),
        label = "stacked-progress"
    )
    val density = LocalDensity.current
    val gapPx = with(density) { BAR_GAP_DP.dp.toPx() }
    val axisStroke = with(density) { AXIS_STROKE_DP.dp.toPx() }
    val bottomPad = with(density) { AXIS_PADDING_DP.dp.toPx() }
    val leftPad = with(density) { Y_AXIS_WIDTH_DP.dp.toPx() }
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val plotH = size.height - bottomPad
        val plotW = size.width - leftPad
        drawLine(axisColor, Offset(leftPad, 0f), Offset(leftPad, plotH), axisStroke)
        drawLine(axisColor, Offset(leftPad, plotH), Offset(size.width, plotH), axisStroke)
        if (entries.isEmpty() || yMax == 0) return@Canvas

        for (i in 0..GRID_LINES) {
            val y = plotH - (plotH * i / GRID_LINES)
            drawLine(axisColor.copy(alpha = GRID_ALPHA), Offset(leftPad, y), Offset(leftPad + plotW, y), axisStroke)
            val value = (yMax.toFloat() * i / GRID_LINES).toInt()
            val layout = measurer.measure(value.toString(), labelStyle)
            drawText(
                layout,
                topLeft = Offset(leftPad - layout.size.width - LABEL_PADDING_PX, y - layout.size.height / 2f)
            )
        }

        val barWidth = (plotW - gapPx * (entries.size - 1)) / entries.size
        entries.forEachIndexed { index, entry ->
            val x = leftPad + index * (barWidth + gapPx)
            var cursorY = plotH
            val segments = listOf(
                entry.again to againColor,
                entry.hard to hardColor,
                entry.good to goodColor,
                entry.easy to easyColor
            )
            segments.forEach { (count, color) ->
                if (count <= 0) return@forEach
                val h = plotH * (count.toFloat() / yMax) * progress
                cursorY -= h
                drawRect(color, topLeft = Offset(x, cursorY), size = Size(barWidth, h))
            }
        }

        xLabels.forEach { (index, label) ->
            if (index !in entries.indices) return@forEach
            val centerX = leftPad + index * (barWidth + gapPx) + barWidth / 2f
            val layout = measurer.measure(label, labelStyle)
            drawText(layout, topLeft = Offset(centerX - layout.size.width / 2f, plotH + X_LABEL_TOP_PADDING_PX))
        }
    }
}
