package com.kado.app.data.converter

import com.kado.app.domain.model.Card
import com.kado.app.domain.model.Deck

object AldConverter {
    fun toAld(deck: Deck, cards: List<Card>): ByteArray {
        val sb = StringBuilder()
        sb.appendLine("ALD1")
        sb.appendLine(deck.name)
        sb.appendLine(cards.size)
        for (card in cards) {
            val front = card.front.replace("\t", " ").replace("\n", "\\n")
            val back = card.back.replace("\t", " ").replace("\n", "\\n")
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
}
