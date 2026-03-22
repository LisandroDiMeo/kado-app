package com.kado.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import com.kado.app.data.importer.MediaStorage
import com.kado.app.domain.parser.ContentPart
import com.kado.app.presentation.model.DisplayableCardContent
import com.kado.app.presentation.renderer.HtmlSegment

@Composable
fun CardContentRenderer(
    content: DisplayableCardContent,
    deckId: Long,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    when (content) {
        is DisplayableCardContent.PlainText -> {
            Text(
                text = content.text,
                style = textStyle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }

        is DisplayableCardContent.ImageMarker -> {
            ImageMarkerContent(content.parts, deckId, textStyle, modifier)
        }

        is DisplayableCardContent.RichText -> {
            RichTextContent(content.segments, deckId, textStyle, modifier)
        }
    }
}

@Composable
private fun ImageMarkerContent(
    parts: List<ContentPart>,
    deckId: Long,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (part in parts) {
            when (part) {
                is ContentPart.Text -> {
                    if (part.text.isNotBlank()) {
                        Text(
                            text = part.text,
                            style = textStyle,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                is ContentPart.Image -> {
                    CardImage(deckId, part.filename)
                }
            }
        }
    }
}

@Composable
private fun RichTextContent(
    segments: List<HtmlSegment>,
    deckId: Long,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (segment in segments) {
            when (segment) {
                is HtmlSegment.StyledText -> {
                    Text(
                        text = segment.annotatedString,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                is HtmlSegment.ImageRef -> {
                    CardImage(deckId, segment.filename)
                }
            }
        }
    }
}

@Composable
private fun CardImage(deckId: Long, filename: String) {
    val mediaPath = MediaStorage.getMediaPath(deckId, filename)
    val context = LocalPlatformContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("file://$mediaPath")
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = filename,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .padding(vertical = 4.dp)
    )
}
