package com.kado.app.presentation.screens.card_edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.presentation.components.CardEditor
import com.kado.app.presentation.components.ConfirmDialog
import com.kado.app.presentation.components.FlashCard
import com.kado.app.presentation.components.ImageManagerDialog
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.rememberImagePickerLauncher
import com.kado.app.presentation.localization.S

@Composable
fun CardEditScreen(
    deckId: Long,
    cardId: Long,
    onBack: () -> Unit,
    vm: CardEditViewModel = viewModel { CardEditViewModel(deckId, cardId) }
) {
    val uiState by vm.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isPreviewFlipped by remember { mutableStateOf(false) }

    val launchImagePicker = rememberImagePickerLauncher { bytes ->
        bytes?.let { vm.onImagePicked(it) }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = S().deleteCard,
            message = S().deleteCardMessage,
            confirmLabel = S().delete,
            onConfirm = {
                showDeleteDialog = false
                vm.delete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (uiState.showImageDialog) {
        ImageManagerDialog(
            images = uiState.attachedImages,
            onImageTap = { filename ->
                vm.insertImageMarker(filename)
                vm.dismissImageDialog()
            },
            onDeleteImage = { filename -> vm.deleteImage(filename) },
            onAddImage = {
                vm.dismissImageDialog()
                launchImagePicker()
            },
            onDismiss = { vm.dismissImageDialog() }
        )
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = if (uiState.isNew) S().newCard else S().editCard,
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        isPreviewFlipped = false
                        vm.togglePreview()
                    }) {
                        Text("\uD83D\uDC41")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.showPreview) {
                FlashCard(
                    front = uiState.frontDisplayable,
                    back = uiState.backDisplayable,
                    isFlipped = isPreviewFlipped,
                    onFlip = { isPreviewFlipped = !isPreviewFlipped },
                    deckId = deckId,
                    modifier = Modifier.align(Alignment.CenterHorizontally).height(256.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
            CardEditor(
                front = uiState.front,
                back = uiState.back,
                onFrontChange = vm::onFrontChange,
                onBackChange = vm::onBackChange,
                onCursorChange = vm::onCursorPositionChange,
                onImageButtonClick = vm::showImageDialog
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.front.isNotBlank() && uiState.back.isNotBlank() && !uiState.isLoading
            ) {
                Text(if (uiState.isNew) S().addCard else S().saveCard)
            }
            if (!uiState.isNew) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(S().deleteCard, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
