package com.kado.app.data.importer

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ApkgImportData(
    val deckName: String,
    val cards: List<ParsedCard>,
    val referencedMedia: Set<String> = emptySet()
)

data class ParsedCard(val front: String, val back: String, val ankiGuid: String? = null)

data class NoteType(
    val id: Long,
    val fields: List<String>, // field names in order
    val templates: List<CardTemplate>
)

data class CardTemplate(
    val ord: Int,
    val name: String,
    // question format
    val qfmt: String,
    // answer format
    val afmt: String
)

internal data class AnkiNote(val id: Long, val mid: Long, val guid: String, val fieldValues: List<String>)

internal data class AnkiCard(val nid: Long, val ord: Int)

object ApkgParser {

    private val HTML_TAG_REGEX = Regex("<[^>]*>")
    private val IMG_TAG_REGEX = Regex("""<img\s+[^>]*src\s*=\s*["']([^"']+)["'][^>]*/?>""", RegexOption.IGNORE_CASE)
    private const val FIELD_SEPARATOR = '\u001F'

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(dbBytes: ByteArray, fallbackName: String = "Imported Deck"): ApkgImportData {
        val tempPath = TempFileWriter.writeTempFile(dbBytes, "anki_import.db")
        try {
            val driver = BundledSQLiteDriver()
            val connection = driver.open(tempPath)
            try {
                return parseConnection(connection, fallbackName)
            } finally {
                connection.close()
            }
        } finally {
            TempFileWriter.deleteTempFile(tempPath)
        }
    }

    internal fun parseConnection(
        connection: SQLiteConnection,
        fallbackName: String = "Imported Deck"
    ): ApkgImportData {
        val deckName = readDeckName(connection, fallbackName)
        val noteTypes = readNoteTypes(connection)

        return if (noteTypes.isNotEmpty()) {
            val notes = readNotes(connection)
            val ankiCards = readAnkiCards(connection)
            val (cards, media) = generateCards(noteTypes, notes, ankiCards)
            ApkgImportData(deckName, cards, media)
        } else {
            val cards = readCardsLegacy(connection)
            ApkgImportData(deckName, cards)
        }
    }

    internal fun generateCards(
        noteTypes: Map<Long, NoteType>,
        notes: Map<Long, AnkiNote>,
        ankiCards: List<AnkiCard>
    ): Pair<List<ParsedCard>, Set<String>> {
        val cards = mutableListOf<ParsedCard>()
        val referencedMedia = mutableSetOf<String>()

        for (ankiCard in ankiCards) {
            val note = notes[ankiCard.nid] ?: continue
            val noteType = noteTypes[note.mid] ?: continue
            val template = noteType.templates.find { it.ord == ankiCard.ord } ?: continue

            // Build field map: fieldName -> fieldValue
            val fieldMap = mutableMapOf<String, String>()
            for ((index, fieldName) in noteType.fields.withIndex()) {
                fieldMap[fieldName] = if (index < note.fieldValues.size) note.fieldValues[index] else ""
            }

            // Render the question template
            val renderedFront = TemplateRenderer.render(template.qfmt, fieldMap)

            // Render the answer template with FrontSide support
            val renderedBack = TemplateRenderer.render(template.afmt, fieldMap, frontSide = renderedFront)

            // Extract image references and convert to markers
            val (frontWithMarkers, frontMedia) = convertImagesToMarkers(renderedFront)
            val (backWithMarkers, backMedia) = convertImagesToMarkers(renderedBack)
            referencedMedia.addAll(frontMedia)
            referencedMedia.addAll(backMedia)

            // Clean HTML but preserve formatting tags
            val front = cleanHtml(frontWithMarkers)
            val back = cleanHtml(backWithMarkers)

            if (front.isNotBlank() && back.isNotBlank()) {
                // Anki cards are uniquely identified by (note.guid, template.ord). For decks where
                // a note produces multiple cards we suffix the ord so each card gets a stable id.
                val ankiGuid = if (noteType.templates.size > 1) {
                    "${note.guid}#${template.ord}"
                } else {
                    note.guid
                }
                cards.add(ParsedCard(front = front, back = back, ankiGuid = ankiGuid))
            }
        }

        return cards to referencedMedia
    }

    private fun readNoteTypes(connection: SQLiteConnection): Map<Long, NoteType> {
        // Try col.models first (works for anki2 and most anki21 files)
        val fromCol = readNoteTypesFromCol(connection)
        if (fromCol.isNotEmpty()) return fromCol

        // Fallback: try notetypes table (anki21b format)
        return readNoteTypesFromTable(connection)
    }

    private fun readNoteTypesFromCol(connection: SQLiteConnection): Map<Long, NoteType> {
        return try {
            val stmt = connection.prepare("SELECT models FROM col LIMIT 1")
            try {
                if (!stmt.step()) return emptyMap()
                val modelsJson = stmt.getText(0)
                parseModelsJson(modelsJson)
            } finally {
                stmt.close()
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    internal fun parseModelsJson(modelsJson: String): Map<Long, NoteType> {
        val noteTypes = mutableMapOf<Long, NoteType>()
        try {
            val models = json.parseToJsonElement(modelsJson).jsonObject
            for ((key, modelElement) in models) {
                val model = modelElement.jsonObject
                val mid = key.toLongOrNull() ?: continue

                val fields = model["flds"]?.jsonArray?.map { fld ->
                    fld.jsonObject["name"]?.jsonPrimitive?.content ?: ""
                } ?: continue

                val templates = model["tmpls"]?.jsonArray?.mapNotNull { tmpl ->
                    val tmplObj = tmpl.jsonObject
                    val ord = tmplObj["ord"]?.jsonPrimitive?.int ?: return@mapNotNull null
                    val name = tmplObj["name"]?.jsonPrimitive?.content ?: ""
                    val qfmt = tmplObj["qfmt"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val afmt = tmplObj["afmt"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    CardTemplate(ord, name, qfmt, afmt)
                } ?: continue

                noteTypes[mid] = NoteType(mid, fields, templates)
            }
        } catch (_: Exception) {
            // JSON parsing failed
        }
        return noteTypes
    }

    private fun readNoteTypesFromTable(connection: SQLiteConnection): Map<Long, NoteType> {
        // anki21b stores note types in separate tables
        return try {
            val noteTypes = mutableMapOf<Long, NoteType>()

            // Check if notetypes table exists
            val checkStmt = connection.prepare(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='notetypes'"
            )
            val hasTable = try {
                checkStmt.step()
            } finally {
                checkStmt.close()
            }
            if (!hasTable) return emptyMap()

            // Read note type IDs
            val ntStmt = connection.prepare("SELECT id, name FROM notetypes")
            val ntIds = mutableListOf<Long>()
            try {
                while (ntStmt.step()) {
                    ntIds.add(ntStmt.getLong(0))
                }
            } finally {
                ntStmt.close()
            }

            for (ntId in ntIds) {
                // Read fields
                val fieldsStmt = connection.prepare(
                    "SELECT name FROM fields WHERE ntid = ? ORDER BY ord"
                )
                fieldsStmt.bindLong(1, ntId)
                val fields = mutableListOf<String>()
                try {
                    while (fieldsStmt.step()) {
                        fields.add(fieldsStmt.getText(0))
                    }
                } finally {
                    fieldsStmt.close()
                }

                // Read templates
                val tmplStmt = connection.prepare(
                    "SELECT ord, name, qfmt, afmt FROM templates WHERE ntid = ? ORDER BY ord"
                )
                tmplStmt.bindLong(1, ntId)
                val templates = mutableListOf<CardTemplate>()
                try {
                    while (tmplStmt.step()) {
                        templates.add(
                            CardTemplate(
                                ord = tmplStmt.getLong(0).toInt(),
                                name = tmplStmt.getText(1),
                                qfmt = tmplStmt.getText(2),
                                afmt = tmplStmt.getText(3)
                            )
                        )
                    }
                } finally {
                    tmplStmt.close()
                }

                if (fields.isNotEmpty() && templates.isNotEmpty()) {
                    noteTypes[ntId] = NoteType(ntId, fields, templates)
                }
            }

            noteTypes
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun readNotes(connection: SQLiteConnection): Map<Long, AnkiNote> {
        val notes = mutableMapOf<Long, AnkiNote>()
        val stmt = connection.prepare("SELECT id, mid, guid, flds FROM notes")
        try {
            while (stmt.step()) {
                val id = stmt.getLong(0)
                val mid = stmt.getLong(1)
                val guid = stmt.getText(2)
                val flds = stmt.getText(3)
                notes[id] = AnkiNote(id, mid, guid, flds.split(FIELD_SEPARATOR))
            }
        } finally {
            stmt.close()
        }
        return notes
    }

    private fun readAnkiCards(connection: SQLiteConnection): List<AnkiCard> {
        val cards = mutableListOf<AnkiCard>()
        val stmt = connection.prepare("SELECT nid, ord FROM cards")
        try {
            while (stmt.step()) {
                cards.add(AnkiCard(nid = stmt.getLong(0), ord = stmt.getLong(1).toInt()))
            }
        } finally {
            stmt.close()
        }
        return cards
    }

    internal fun convertImagesToMarkers(html: String): Pair<String, Set<String>> {
        val media = mutableSetOf<String>()
        val result = IMG_TAG_REGEX.replace(html) { match ->
            val filename = match.groupValues[1]
            media.add(filename)
            "[img:$filename]"
        }
        return result to media
    }

    private fun readDeckName(
        connection: SQLiteConnection,
        fallbackName: String
    ): String = try {
        val stmt = connection.prepare("SELECT decks FROM col LIMIT 1")
        try {
            if (stmt.step()) {
                val decksJson = stmt.getText(0)
                extractDeckNameFromJson(decksJson) ?: fallbackName
            } else {
                fallbackName
            }
        } finally {
            stmt.close()
        }
    } catch (_: Exception) {
        fallbackName
    }

    private fun extractDeckNameFromJson(json: String): String? {
        val nameRegex = Regex(""""name"\s*:\s*"([^"]+)"""")
        val names = nameRegex.findAll(json)
            .map { it.groupValues[1] }
            .filter { it != "Default" && it != "default" }
            .toList()
        return names.firstOrNull()
    }

    /** Legacy parsing: reads only notes table, field[0] as front, field[1] as back */
    private fun readCardsLegacy(connection: SQLiteConnection): List<ParsedCard> {
        val cards = mutableListOf<ParsedCard>()
        val stmt = connection.prepare("SELECT guid, flds FROM notes")
        try {
            while (stmt.step()) {
                val guid = stmt.getText(0)
                val flds = stmt.getText(1)
                val fields = flds.split(FIELD_SEPARATOR)
                if (fields.size >= 2) {
                    val front = cleanHtml(fields[0])
                    val back = cleanHtml(fields[1])
                    if (front.isNotBlank() && back.isNotBlank()) {
                        cards.add(ParsedCard(front = front, back = back, ankiGuid = guid))
                    }
                }
            }
        } finally {
            stmt.close()
        }
        return cards
    }

    internal fun cleanHtml(html: String): String = html
        .replace(Regex("\\[sound:[^]]*]"), "")
        .replace("&nbsp;", " ")
        .trim()

    internal fun stripHtml(html: String): String = html
        .replace(Regex("\\[sound:[^]]*]"), "")
        .replace(Regex(">\\s+<"), "><") // Collapse whitespace between HTML tags
        .replace(Regex("<hr\\b[^>]*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</(div|p|li|tr|blockquote|h[1-6])>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<(div|p|li|tr|blockquote|h[1-6])\\b[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(HTML_TAG_REGEX, "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("[ \\t]*\\n"), "\n") // Remove trailing spaces on lines
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}
