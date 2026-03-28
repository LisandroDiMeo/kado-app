package com.kado.app.domain.usecase

import com.kado.app.data.converter.AldConverter
import com.kado.app.domain.repository.DeckRepository

data class TransferPayload(
    val aldBytes: ByteArray,
    val filename: String,
    val displayName: String
)

class PrepareTransferDeckUseCase(private val repository: DeckRepository) {

    suspend operator fun invoke(deckId: Long, subDeckIndex: Int?): TransferPayload {
        val deck = repository.getDeck(deckId) ?: throw IllegalStateException("Deck not found")
        val cards = if (subDeckIndex != null) {
            repository.getSubDeckCards(deckId, subDeckIndex)
        } else {
            repository.getCards(deckId)
        }
        val displayName = formatSubDeckName(deck.name, subDeckIndex)
        val transferDeck = deck.copy(name = displayName)
        val aldBytes = AldConverter.toAld(transferDeck, cards)
        val filename = AldConverter.toAldFilename(transferDeck.name)

        return TransferPayload(
            aldBytes = aldBytes,
            filename = filename,
            displayName = displayName
        )
    }

    companion object {
        fun formatSubDeckName(baseName: String, subDeckIndex: Int?): String {
            return if (subDeckIndex != null) "$baseName - Part ${subDeckIndex + 1}" else baseName
        }
    }
}
