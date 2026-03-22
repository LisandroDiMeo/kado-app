package com.kado.app.data.converter

import com.kado.app.domain.model.Card
import com.kado.app.domain.model.Deck

object AldConverter {

    private val IMG_MARKER_REGEX = Regex("\\[img:[^]]*]")
    private val HTML_TAG_REGEX = Regex("<[^>]*>")

    fun toAld(deck: Deck, cards: List<Card>): ByteArray {
        val textCards = cards.mapNotNull { card ->
            val front = toPlainText(card.front.rawText).replace("\t", " ").replace("\n", "\\n")
            val back = toPlainText(card.back.rawText).replace("\t", " ").replace("\n", "\\n")
            if (front.isNotBlank() && back.isNotBlank()) front to back else null
        }
        val sb = StringBuilder()
        sb.appendLine("ALD1")
        sb.appendLine(deck.name)
        sb.appendLine(textCards.size)
        for ((front, back) in textCards) {
            sb.appendLine("$front\t$back")
        }
        return sb.toString().encodeToByteArray()
    }

    fun toAldFilename(deckName: String): String {
        val safe = deckName.lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return "$safe.ald"
    }

    internal fun toPlainText(raw: String): String {
        return raw
            .let { stripImages(it) }
            .let { stripHtmlTags(it) }
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    internal fun stripImages(text: String): String {
        return IMG_MARKER_REGEX.replace(text, "").trim()
    }

    internal fun stripHtmlTags(text: String): String {
        return text
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</(div|p|li|tr|blockquote|h[1-6])>", RegexOption.IGNORE_CASE), "\n")
            .let { HTML_TAG_REGEX.replace(it, "") }
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }
}
