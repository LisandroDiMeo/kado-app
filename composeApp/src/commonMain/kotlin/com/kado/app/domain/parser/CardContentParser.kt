package com.kado.app.domain.parser

import com.kado.app.domain.model.CardContent

class CardContentParser {

    companion object {
        val IMG_MARKER_REGEX = Regex("\\[img:([^]]+)]")
        private val HTML_DETECT_REGEX = Regex("<[a-zA-Z][^>]*>")
    }

    fun detect(raw: String): CardContent {
        return when {
            HTML_DETECT_REGEX.containsMatchIn(raw) -> CardContent.RichText(
                rawText = raw,
                imageFilenames = extractImageFilenames(raw)
            )
            IMG_MARKER_REGEX.containsMatchIn(raw) -> CardContent.ImageMarker(
                rawText = raw,
                parts = parse(raw)
            )
            else -> CardContent.PlainText(raw)
        }
    }

    fun parse(text: String): List<ContentPart> {
        val parts = mutableListOf<ContentPart>()
        var lastIndex = 0

        for (match in IMG_MARKER_REGEX.findAll(text)) {
            val beforeText = text.substring(lastIndex, match.range.first).trim()
            if (beforeText.isNotEmpty()) {
                parts.add(ContentPart.Text(beforeText))
            }
            parts.add(ContentPart.Image(match.groupValues[1]))
            lastIndex = match.range.last + 1
        }

        val remaining = text.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            parts.add(ContentPart.Text(remaining))
        }

        return parts
    }

    fun extractImageFilenames(text: String): List<String> {
        return IMG_MARKER_REGEX.findAll(text).map { it.groupValues[1] }.toList()
    }

    fun insertMarker(text: String, cursorPosition: Int, filename: String): String {
        val marker = "[img:$filename]"
        val pos = cursorPosition.coerceIn(0, text.length)
        return text.substring(0, pos) + marker + text.substring(pos)
    }

    fun removeAllMarkersForFile(text: String, filename: String): String {
        val pattern = Regex("\\[img:${Regex.escape(filename)}]")
        return pattern.replace(text, "").replace(Regex(" {2,}"), " ").trim()
    }
}

sealed class ContentPart {
    data class Text(val text: String) : ContentPart()
    data class Image(val filename: String) : ContentPart()
}
