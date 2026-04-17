package com.kado.app.presentation.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kado.app.domain.model.HeatmapCell
import kotlin.math.max
import kotlinx.datetime.DayOfWeek

private const val DAYS_IN_WEEK = 7
private const val CELL_GAP_DP = 2
private const val ANIM_DURATION_MS = 500

@Composable
fun CalendarHeatmap(
    cells: List<HeatmapCell>,
    modifier: Modifier = Modifier
) {
    val baseColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val maxCount = remember(cells) { cells.maxOfOrNull { it.count } ?: 0 }
    val progress by animateFloatAsState(
        targetValue = if (cells.isEmpty()) 0f else 1f,
        animationSpec = tween(ANIM_DURATION_MS),
        label = "heatmap-progress"
    )

    val weeks = remember(cells) { groupIntoWeeks(cells) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height((DAYS_IN_WEEK * 16).dp)
    ) {
        if (weeks.isEmpty()) return@Canvas
        val gap = CELL_GAP_DP.dp.toPx()
        val columnCount = weeks.size
        val cellSize = ((size.width - gap * (columnCount - 1)) / columnCount)
            .coerceAtMost((size.height - gap * (DAYS_IN_WEEK - 1)) / DAYS_IN_WEEK)

        weeks.forEachIndexed { column, week ->
            week.forEachIndexed { row, cell ->
                val x = column * (cellSize + gap)
                val y = row * (cellSize + gap)
                val intensity = if (maxCount == 0 || cell == null) {
                    0f
                } else {
                    (cell.count.toFloat() / maxCount).coerceIn(0f, 1f)
                }
                val color = if (cell == null) {
                    Color.Transparent
                } else if (intensity == 0f) {
                    emptyColor.copy(alpha = 0.4f * progress)
                } else {
                    baseColor.copy(alpha = (0.2f + 0.8f * intensity) * progress)
                }
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )
            }
        }
    }
}

private fun groupIntoWeeks(cells: List<HeatmapCell>): List<List<HeatmapCell?>> {
    if (cells.isEmpty()) return emptyList()
    val weeks = mutableListOf<MutableList<HeatmapCell?>>()
    var current = MutableList<HeatmapCell?>(DAYS_IN_WEEK) { null }
    val firstDow = cells.first().date.dayOfWeek.isoIndex()
    // Pad the first week so that weekday rows align (Monday = row 0).
    for (i in 0 until firstDow) current[i] = null
    cells.forEachIndexed { index, cell ->
        val row = (firstDow + index) % DAYS_IN_WEEK
        current[row] = cell
        if (row == DAYS_IN_WEEK - 1) {
            weeks += current
            current = MutableList(DAYS_IN_WEEK) { null }
        }
    }
    if (current.any { it != null }) weeks += current
    return weeks
}

private fun DayOfWeek.isoIndex(): Int = max(0, this.ordinal)
