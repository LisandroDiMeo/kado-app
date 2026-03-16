package com.kado.app.data.importer

import com.kado.app.domain.repository.DeckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

enum class ImportPhase { Extracting, Parsing, Inserting, Done, Error }

data class ImportProgress(
    val phase: ImportPhase,
    val progress: Float = 0f,
    val deckName: String = "",
    val cardCount: Int = 0,
    val error: String? = null
)

class ApkgImporter(private val repository: DeckRepository) {

    suspend fun import(
        fileBytes: ByteArray,
        onProgress: (ImportProgress) -> Unit
    ): Long = withContext(Dispatchers.IO) {
        try {
            // Phase 1: Extract ZIP (0-20%)
            onProgress(ImportProgress(ImportPhase.Extracting, 0f))
            val entries = ZipExtractor.listEntries(fileBytes)
            val dbEntryName = entries.firstOrNull {
                it == "collection.anki21" || it == "collection.anki2" || it == "collection.anki21b"
            } ?: throw IllegalArgumentException("Not a valid APKG file — no Anki database found")

            val dbBytes = ZipExtractor.extractEntry(fileBytes, dbEntryName)
                ?: throw IllegalArgumentException("Failed to extract database from APKG")
            onProgress(ImportProgress(ImportPhase.Extracting, 0.2f))

            // Phase 2: Parse Anki DB (20-40%)
            onProgress(ImportProgress(ImportPhase.Parsing, 0.2f))
            val importData = ApkgParser.parse(dbBytes)
            if (importData.cards.isEmpty()) {
                throw IllegalArgumentException("No cards found in APKG file")
            }
            onProgress(ImportProgress(
                ImportPhase.Parsing, 0.4f,
                deckName = importData.deckName,
                cardCount = importData.cards.size
            ))

            // Phase 3: Insert into Kado DB (40-100%)
            val deckId = repository.importDeck(
                name = importData.deckName,
                cards = importData.cards
            ) { insertProgress ->
                onProgress(ImportProgress(
                    ImportPhase.Inserting,
                    0.4f + insertProgress * 0.6f,
                    deckName = importData.deckName,
                    cardCount = importData.cards.size
                ))
            }

            onProgress(ImportProgress(
                ImportPhase.Done, 1f,
                deckName = importData.deckName,
                cardCount = importData.cards.size
            ))

            deckId
        } catch (e: Exception) {
            onProgress(ImportProgress(
                ImportPhase.Error,
                error = e.message ?: "Unknown error during import"
            ))
            throw e
        }
    }
}
