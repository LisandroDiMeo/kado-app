package com.kado.app.data.importer

import androidx.sqlite.driver.bundled.BundledSQLiteDriver

data class ApkgImportData(
    val deckName: String,
    val cards: List<Pair<String, String>>
)

object ApkgParser {

    private val HTML_TAG_REGEX = Regex("<[^>]*>")
    private const val FIELD_SEPARATOR = '\u001F'

    fun parse(dbBytes: ByteArray, fallbackName: String = "Imported Deck"): ApkgImportData {
        val tempPath = TempFileWriter.writeTempFile(dbBytes, "anki_import.db")
        try {
            val driver = BundledSQLiteDriver()
            val connection = driver.open(tempPath)
            try {
                val deckName = readDeckName(connection, fallbackName)
                val cards = readCards(connection)
                return ApkgImportData(deckName, cards)
            } finally {
                connection.close()
            }
        } finally {
            TempFileWriter.deleteTempFile(tempPath)
        }
    }

    private fun readDeckName(
        connection: androidx.sqlite.SQLiteConnection,
        fallbackName: String
    ): String {
        return try {
            val stmt = connection.prepare("SELECT decks FROM col LIMIT 1")
            try {
                if (stmt.step()) {
                    val decksJson = stmt.getText(0)
                    extractDeckNameFromJson(decksJson) ?: fallbackName
                } else fallbackName
            } finally {
                stmt.close()
            }
        } catch (_: Exception) {
            fallbackName
        }
    }

    private fun extractDeckNameFromJson(json: String): String? {
        // The decks JSON is like {"1": {"name": "Default", ...}, "12345": {"name": "My Deck", ...}}
        // Extract deck names using simple string parsing to avoid JSON dependency overhead
        val nameRegex = Regex(""""name"\s*:\s*"([^"]+)"""")
        val names = nameRegex.findAll(json)
            .map { it.groupValues[1] }
            .filter { it != "Default" && it != "default" }
            .toList()
        return names.firstOrNull()
    }

    private fun readCards(connection: androidx.sqlite.SQLiteConnection): List<Pair<String, String>> {
        val cards = mutableListOf<Pair<String, String>>()
        val stmt = connection.prepare("SELECT flds FROM notes")
        try {
            while (stmt.step()) {
                val flds = stmt.getText(0)
                val fields = flds.split(FIELD_SEPARATOR)
                if (fields.size >= 2) {
                    val front = stripHtml(fields[0])
                    val back = stripHtml(fields[1])
                    if (front.isNotBlank() && back.isNotBlank()) {
                        cards.add(front to back)
                    }
                }
            }
        } finally {
            stmt.close()
        }
        return cards
    }

    private fun stripHtml(html: String): String {
        return html
            .replace(Regex("\\[sound:[^]]*]"), "")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</(div|p|li|tr|blockquote|h[1-6])>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<(div|p|li|tr|blockquote|h[1-6])\\b[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(HTML_TAG_REGEX, "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
