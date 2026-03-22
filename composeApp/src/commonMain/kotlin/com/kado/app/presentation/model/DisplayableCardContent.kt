package com.kado.app.presentation.model

import com.kado.app.domain.model.CardContent
import com.kado.app.domain.parser.ContentPart
import com.kado.app.presentation.renderer.HtmlRenderer
import com.kado.app.presentation.renderer.HtmlSegment

sealed class DisplayableCardContent {
    data class PlainText(val text: String) : DisplayableCardContent()

    data class ImageMarker(val parts: List<ContentPart>) : DisplayableCardContent()

    data class RichText(val segments: List<HtmlSegment>) : DisplayableCardContent()

    companion object {
        fun from(content: CardContent, htmlRenderer: HtmlRenderer): DisplayableCardContent {
            return when (content) {
                is CardContent.PlainText -> PlainText(content.rawText)
                is CardContent.ImageMarker -> ImageMarker(content.parts)
                is CardContent.RichText -> RichText(htmlRenderer.render(content.rawText))
            }
        }
    }
}
