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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

private const val BAR_GAP_DP = 2
private const val ANIM_DURATION_MS = 500
private const val LINE_STROKE_DP = 2
private const val AXIS_STROKE_DP = 1
private const val AXIS_PADDING_DP = 28
private const val Y_AXIS_WIDTH_DP = 36
private const val GRID_LINES = 4
private const val GRID_ALPHA = 0.25f
private const val LABEL_PADDING_PX = 4f
private const val X_LABEL_TOP_PADDING_PX = 6f

data class BarChartEntry(val value: Int, val cumulative: Int? = null)

@Composable
fun BarChart(
    bars: List<BarChartEntry>,
    yMax: Int,
    xLabels: Map<Int, String> = emptyMap(),
    yMaxSecondary: Int = 0,
    modifier: Modifier = Modifier,
    showCumulativeLine: Boolean = false,
    barColor: Color = MaterialTheme.colorScheme.primary,
    lineColor: Color = MaterialTheme.colorScheme.tertiary,
    axisColor: Color = MaterialTheme.colorScheme.outline,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val progress by animateFloatAsState(
        targetValue = if (bars.isEmpty()) 0f else 1f,
        animationSpec = tween(ANIM_DURATION_MS),
        label = "barchart-progress"
    )
    val density = LocalDensity.current
    val gapPx = with(density) { BAR_GAP_DP.dp.toPx() }
    val stroke = with(density) { LINE_STROKE_DP.dp.toPx() }
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
        if (bars.isEmpty() || yMax == 0) {
            drawAxes(leftPad, bottomPad, axisColor, axisStroke)
            return@Canvas
        }
        val plotW = size.width - leftPad
        val plotH = size.height - bottomPad

        drawGridAndYLabels(measurer, labelStyle, leftPad, plotW, plotH, yMax, axisColor, axisStroke)

        val barWidth = (plotW - gapPx * (bars.size - 1)) / bars.size
        bars.forEachIndexed { index, entry ->
            val normalized = entry.value.toFloat() / yMax
            val h = plotH * normalized * progress
            val x = leftPad + index * (barWidth + gapPx)
            drawRect(barColor, topLeft = Offset(x, plotH - h), size = Size(barWidth, h))
        }

        drawXLabels(measurer, labelStyle, bars.size, leftPad, plotH, barWidth, gapPx, xLabels)

        if (showCumulativeLine && yMaxSecondary > 0) {
            drawCumulativeLine(bars, leftPad, plotH, barWidth, gapPx, yMaxSecondary, progress, lineColor, stroke)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAxes(
    leftPad: Float,
    bottomPad: Float,
    axisColor: Color,
    axisStroke: Float
) {
    val plotH = size.height - bottomPad
    drawLine(axisColor, Offset(leftPad, 0f), Offset(leftPad, plotH), axisStroke)
    drawLine(axisColor, Offset(leftPad, plotH), Offset(size.width, plotH), axisStroke)
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridAndYLabels(
    measurer: TextMeasurer,
    style: TextStyle,
    leftPad: Float,
    plotW: Float,
    plotH: Float,
    yMax: Int,
    axisColor: Color,
    axisStroke: Float
) {
    for (i in 0..GRID_LINES) {
        val y = plotH - (plotH * i / GRID_LINES)
        drawLine(axisColor.copy(alpha = GRID_ALPHA), Offset(leftPad, y), Offset(leftPad + plotW, y), axisStroke)
        val value = (yMax.toFloat() * i / GRID_LINES).toInt()
        val layout = measurer.measure(value.toString(), style)
        drawText(
            layout,
            topLeft = Offset(leftPad - layout.size.width - LABEL_PADDING_PX, y - layout.size.height / 2f)
        )
    }
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawXLabels(
    measurer: TextMeasurer,
    style: TextStyle,
    barCount: Int,
    leftPad: Float,
    plotH: Float,
    barWidth: Float,
    gapPx: Float,
    xLabels: Map<Int, String>
) {
    xLabels.forEach { (index, label) ->
        if (index !in 0 until barCount) return@forEach
        val centerX = leftPad + index * (barWidth + gapPx) + barWidth / 2f
        val layout = measurer.measure(label, style)
        drawText(layout, topLeft = Offset(centerX - layout.size.width / 2f, plotH + X_LABEL_TOP_PADDING_PX))
    }
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCumulativeLine(
    bars: List<BarChartEntry>,
    leftPad: Float,
    plotH: Float,
    barWidth: Float,
    gapPx: Float,
    yMaxSecondary: Int,
    progress: Float,
    lineColor: Color,
    stroke: Float
) {
    val path = Path()
    bars.forEachIndexed { index, entry ->
        val cum = entry.cumulative ?: 0
        val x = leftPad + index * (barWidth + gapPx) + barWidth / 2f
        val y = plotH - plotH * (cum.toFloat() / yMaxSecondary) * progress
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, lineColor, style = Stroke(width = stroke))
}
