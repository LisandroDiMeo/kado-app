package com.kado.app.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kado.app.domain.model.Rating
import com.kado.app.presentation.localization.S

@Composable
fun RatingBar(
    onRate: (Rating) -> Unit,
    intervals: Map<Rating, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RatingButton(
            S().again,
            MaterialTheme.colorScheme.error,
            Rating.Again,
            onRate,
            intervals[Rating.Again],
            Modifier.weight(1f)
        )
        RatingButton(
            S().hard,
            MaterialTheme.colorScheme.secondary,
            Rating.Hard,
            onRate,
            intervals[Rating.Hard],
            Modifier.weight(1f)
        )
        RatingButton(
            S().good,
            MaterialTheme.colorScheme.tertiary,
            Rating.Good,
            onRate,
            intervals[Rating.Good],
            Modifier.weight(1f)
        )
        RatingButton(
            S().easy,
            MaterialTheme.colorScheme.primary,
            Rating.Easy,
            onRate,
            intervals[Rating.Easy],
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun RatingButton(
    label: String,
    color: Color,
    rating: Rating,
    onRate: (Rating) -> Unit,
    interval: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (interval != null) {
            Text(
                text = interval,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = { onRate(rating) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Text(
                text = label.lowercase(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
