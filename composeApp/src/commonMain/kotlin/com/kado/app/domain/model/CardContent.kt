package com.kado.app.domain.model

import com.kado.app.domain.parser.ContentPart

sealed class CardContent {
    abstract val rawText: String

    data class PlainText(override val rawText: String) : CardContent()

    data class RichText(
        override val rawText: String,
        val imageFilenames: List<String> = emptyList()
    ) : CardContent()

    data class ImageMarker(
        override val rawText: String,
        val parts: List<ContentPart>
    ) : CardContent()
}
