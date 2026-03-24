package com.kado.app.data.importer

import com.kado.app.domain.repository.DeckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class ImportPhase { Extracting, Parsing, ExtractingMedia, Inserting, Done, Error }

data class ImportProgress(
    val phase: ImportPhase,
    val progress: Float = 0f,
    val deckName: String = "",
    val cardCount: Int = 0,
    val error: String? = null
)

class ApkgImporter(private val repository: DeckRepository) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun import(
        fileBytes: ByteArray,
        onProgress: (ImportProgress) -> Unit
    ): Long = withContext(Dispatchers.IO) {
        try {
            // Phase 1: Extract ZIP (0-15%)
            onProgress(ImportProgress(ImportPhase.Extracting, 0f))
            val entries = ZipExtractor.listEntries(fileBytes)
            val dbEntryName = entries.firstOrNull {
                it == "collection.anki21" || it == "collection.anki2" || it == "collection.anki21b"
            } ?: throw IllegalArgumentException("Not a valid APKG file — no Anki database found")

            // Extract DB and media manifest in a single pass
            val initialEntries = ZipExtractor.extractEntries(fileBytes, setOf(dbEntryName, "media"))
            val dbBytes = initialEntries[dbEntryName]
                ?: throw IllegalArgumentException("Failed to extract database from APKG")
            onProgress(ImportProgress(ImportPhase.Extracting, 0.15f))

            // Phase 2: Parse Anki DB (15-35%)
            onProgress(ImportProgress(ImportPhase.Parsing, 0.15f))
            val importData = ApkgParser.parse(dbBytes)
            if (importData.cards.isEmpty()) {
                throw IllegalArgumentException("No cards found in APKG file")
            }
            onProgress(ImportProgress(
                ImportPhase.Parsing, 0.35f,
                deckName = importData.deckName,
                cardCount = importData.cards.size
            ))

            // Phase 3: Insert into Kado DB (35-70%)
            val deckId = repository.importDeck(
                name = importData.deckName,
                cards = importData.cards
            ) { insertProgress ->
                onProgress(ImportProgress(
                    ImportPhase.Inserting,
                    0.35f + insertProgress * 0.35f,
                    deckName = importData.deckName,
                    cardCount = importData.cards.size
                ))
            }

            // Phase 4: Extract media files (70-95%)
            if (importData.referencedMedia.isNotEmpty()) {
                extractMedia(fileBytes, entries, deckId, importData.referencedMedia, initialEntries["media"]) { mediaProgress ->
                    onProgress(ImportProgress(
                        ImportPhase.ExtractingMedia,
                        0.70f + mediaProgress * 0.25f,
                        deckName = importData.deckName,
                        cardCount = importData.cards.size
                    ))
                }
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

    private fun extractMedia(
        fileBytes: ByteArray,
        entries: List<String>,
        deckId: Long,
        referencedMedia: Set<String>,
        mediaJsonBytes: ByteArray?,
        onProgress: (Float) -> Unit
    ) {
        // Parse the media JSON mapping (numeric key -> filename)
        val mediaJson = (mediaJsonBytes ?: return).decodeToString()
        val mediaMap: Map<String, String> = try {
            val obj = json.parseToJsonElement(mediaJson).jsonObject
            obj.mapValues { it.value.jsonPrimitive.content }
        } catch (_: Exception) {
            return
        }

        // Build reverse map: filename -> numeric key in ZIP
        val filenameToKey = mutableMapOf<String, String>()
        for ((key, filename) in mediaMap) {
            filenameToKey[filename] = key
        }

        // Determine which ZIP keys to extract
        val entrySet = entries.toSet()
        val keyToFilename = mutableMapOf<String, String>()
        for (filename in referencedMedia) {
            val key = filenameToKey[filename] ?: continue
            if (key in entrySet) {
                keyToFilename[key] = filename
            }
        }

        // Single-pass extraction of all media files
        val extractedMedia = ZipExtractor.extractEntries(fileBytes, keyToFilename.keys)

        // Save extracted media to disk
        var saved = 0
        val total = keyToFilename.size
        for ((key, filename) in keyToFilename) {
            val mediaBytes = extractedMedia[key]
            if (mediaBytes != null) {
                MediaStorage.saveMedia(deckId, filename, mediaBytes)
            }
            saved++
            onProgress(saved.toFloat() / total)
        }
    }
}
