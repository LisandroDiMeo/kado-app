package com.kado.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.kado.app.presentation.localization.S

data class ImageInfo(
    val filename: String,
    val path: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerDialog(
    images: List<ImageInfo>,
    onImageTap: (String) -> Unit,
    onDeleteImage: (String) -> Unit,
    onAddImage: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = S().images,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (images.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(images, key = { it.filename }) { image ->
                        ImageThumbnail(
                            image = image,
                            onTap = { onImageTap(image.filename) },
                            onDelete = { onDeleteImage(image.filename) }
                        )
                    }
                }
            } else {
                Text(
                    text = S().noImagesAttached,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp).align(Alignment.CenterHorizontally)
                )
            }

            Button(
                onClick = onAddImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(S().addImage)
            }
        }
    }
}

@Composable
private fun ImageThumbnail(
    image: ImageInfo,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalPlatformContext.current
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onTap() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file://${image.path}")
                    .build(),
                contentDescription = image.filename,
                contentScale = ContentScale.Crop,
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        "✕",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
