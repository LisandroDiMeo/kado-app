package com.kado.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            enabled = value < range.last,
            modifier = Modifier.size(32.dp)
        ) {
            Text("\u25B2", style = MaterialTheme.typography.labelSmall)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            enabled = value > range.first,
            modifier = Modifier.size(32.dp)
        ) {
            Text("\u25BC", style = MaterialTheme.typography.labelSmall)
        }
    }
}
