package com.kado.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kado.app.presentation.localization.S
import com.kado.app.presentation.screens.card_edit.ActiveField

@Composable
fun CardEditor(
    front: String,
    back: String,
    onFrontChange: (String) -> Unit,
    onBackChange: (String) -> Unit,
    onCursorChange: (ActiveField, Int) -> Unit = { _, _ -> },
    onImageButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var frontTfv by remember(front) {
        mutableStateOf(TextFieldValue(front, TextRange(front.length)))
    }
    var backTfv by remember(back) {
        mutableStateOf(TextFieldValue(back, TextRange(back.length)))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(S().front, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                onCursorChange(ActiveField.FRONT, frontTfv.selection.start)
                onImageButtonClick()
            }, modifier = Modifier.size(32.dp)) {
                Text("\uD83D\uDDBC", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = frontTfv,
            onValueChange = { newValue ->
                frontTfv = newValue
                onFrontChange(newValue.text)
                onCursorChange(ActiveField.FRONT, newValue.selection.start)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onCursorChange(ActiveField.FRONT, frontTfv.selection.start) },
            minLines = 3,
            maxLines = 6,
            placeholder = { Text(S().questionPlaceholder) }
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(S().backSide, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                onCursorChange(ActiveField.BACK, backTfv.selection.start)
                onImageButtonClick()
            }, modifier = Modifier.size(32.dp)) {
                Text("\uD83D\uDDBC", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = backTfv,
            onValueChange = { newValue ->
                backTfv = newValue
                onBackChange(newValue.text)
                onCursorChange(ActiveField.BACK, newValue.selection.start)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onCursorChange(ActiveField.BACK, backTfv.selection.start) },
            minLines = 3,
            maxLines = 6,
            placeholder = { Text(S().answerPlaceholder) }
        )
    }
}
