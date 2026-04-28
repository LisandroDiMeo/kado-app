package com.kado.app.data.importer

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApkgParserTest {

    private fun SQLiteConnection.exec(sql: String) {
        val stmt = prepare(sql)
        try {
            stmt.step()
        } finally {
            stmt.close()
        }
    }

    @Test
    fun parseModelsJsonSingleNoteType() {
        val modelsJson = """
        {
            "1234": {
                "id": 1234,
                "name": "Basic",
                "flds": [
                    {"name": "Front", "ord": 0},
                    {"name": "Back", "ord": 1}
                ],
                "tmpls": [
                    {"ord": 0, "name": "Card 1", "qfmt": "{{Front}}", "afmt": "{{FrontSide}}<hr id=answer>{{Back}}"}
                ]
            }
        }
        """.trimIndent()

        val noteTypes = ApkgParser.parseModelsJson(modelsJson)
        assertEquals(1, noteTypes.size)

        val nt = noteTypes[1234L]!!
        assertEquals(listOf("Front", "Back"), nt.fields)
        assertEquals(1, nt.templates.size)
        assertEquals("{{Front}}", nt.templates[0].qfmt)
    }

    @Test
    fun parseModelsJsonMultipleTemplates() {
        val modelsJson = """
        {
            "5678": {
                "id": 5678,
                "name": "Geography",
                "flds": [
                    {"name": "Country", "ord": 0},
                    {"name": "Capital", "ord": 1},
                    {"name": "Flag", "ord": 2}
                ],
                "tmpls": [
                    {"ord": 0, "name": "Country-Capital", "qfmt": "{{Country}}", "afmt": "{{Capital}}"},
                    {"ord": 1, "name": "Capital-Country", "qfmt": "{{Capital}}", "afmt": "{{Country}}"},
                    {"ord": 2, "name": "Flag-Country", "qfmt": "{{Flag}}", "afmt": "{{Country}}"}
                ]
            }
        }
        """.trimIndent()

        val noteTypes = ApkgParser.parseModelsJson(modelsJson)
        assertEquals(1, noteTypes.size)

        val nt = noteTypes[5678L]!!
        assertEquals(3, nt.templates.size)
        assertEquals("Flag-Country", nt.templates[2].name)
    }

    @Test
    fun parseModelsJsonMultipleNoteTypes() {
        val modelsJson = """
        {
            "100": {
                "id": 100,
                "flds": [{"name": "Q", "ord": 0}, {"name": "A", "ord": 1}],
                "tmpls": [{"ord": 0, "name": "Card", "qfmt": "{{Q}}", "afmt": "{{A}}"}]
            },
            "200": {
                "id": 200,
                "flds": [{"name": "Word", "ord": 0}, {"name": "Meaning", "ord": 1}],
                "tmpls": [{"ord": 0, "name": "Card", "qfmt": "{{Word}}", "afmt": "{{Meaning}}"}]
            }
        }
        """.trimIndent()

        val noteTypes = ApkgParser.parseModelsJson(modelsJson)
        assertEquals(2, noteTypes.size)
        assertTrue(noteTypes.containsKey(100L))
        assertTrue(noteTypes.containsKey(200L))
    }

    @Test
    fun parseModelsJsonEmptyModels() {
        val noteTypes = ApkgParser.parseModelsJson("{}")
        assertEquals(0, noteTypes.size)
    }

    @Test
    fun parseModelsJsonInvalidJson() {
        val noteTypes = ApkgParser.parseModelsJson("not valid json")
        assertEquals(0, noteTypes.size)
    }

    @Test
    fun convertImagesToMarkersBasic() {
        val (result, media) = ApkgParser.convertImagesToMarkers(
            """<img src="flag.png" />"""
        )
        assertEquals("[img:flag.png]", result)
        assertEquals(setOf("flag.png"), media)
    }

    @Test
    fun convertImagesToMarkersMultipleImages() {
        val (result, media) = ApkgParser.convertImagesToMarkers(
            """<img src="flag.svg" /> and <img src="map.png">"""
        )
        assertEquals("[img:flag.svg] and [img:map.png]", result)
        assertEquals(setOf("flag.svg", "map.png"), media)
    }

    @Test
    fun convertImagesToMarkersNoImages() {
        val (result, media) = ApkgParser.convertImagesToMarkers("Just text content")
        assertEquals("Just text content", result)
        assertEquals(emptySet(), media)
    }

    @Test
    fun convertImagesToMarkersWithSrcVariants() {
        val (result, media) = ApkgParser.convertImagesToMarkers(
            """<img src='image.jpg'>"""
        )
        assertEquals("[img:image.jpg]", result)
        assertEquals(setOf("image.jpg"), media)
    }

    @Test
    fun stripHtmlBasic() {
        val result = ApkgParser.stripHtml("<b>Bold</b> and <i>italic</i>")
        assertEquals("Bold and italic", result)
    }

    @Test
    fun stripHtmlPreservesImageMarkers() {
        val result = ApkgParser.stripHtml("[img:flag.svg] <b>England</b>")
        assertEquals("[img:flag.svg] England", result)
    }

    @Test
    fun stripHtmlLineBreaks() {
        val result = ApkgParser.stripHtml("Line 1<br>Line 2<br/>Line 3")
        assertEquals("Line 1\nLine 2\nLine 3", result)
    }

    @Test
    fun stripHtmlEntities() {
        val result = ApkgParser.stripHtml("&amp; &lt; &gt; &quot; &#39; &nbsp;")
        assertEquals("& < > \" '", result)
    }

    @Test
    fun stripHtmlSoundDirectives() {
        val result = ApkgParser.stripHtml("[sound:audio.mp3] Some text")
        assertEquals("Some text", result)
    }

    @Test
    fun stripHtmlCollapsesMultipleNewlines() {
        val result = ApkgParser.stripHtml("A<br><br><br><br>B")
        assertEquals("A\n\nB", result)
    }

    @Test
    fun stripHtmlCollapsesWhitespaceBetweenTags() {
        // Whitespace between HTML tags should not produce extra line breaks
        val html = """
  <div class="value">England</div>
  <div class="info">Constituent country.</div>

  <hr>

  <div class="type">Capital</div>
  <div class="value">?</div>
"""
        val result = ApkgParser.stripHtml(html)
        assertEquals("England\nConstituent country.\n\nCapital\n?", result)
    }

    @Test
    fun stripHtmlGeographyAnswerTemplate() {
        // Real answer template output from Ultimate Geography deck
        val html = "<div class=\"value value--top\">England</div>\n" +
            "<div class=\"info\">Constituent country of the United Kingdom.</div>\n\n" +
            "<hr id=answer>\n\n" +
            "<div class=\"type\">Capital</div>\n" +
            "<div class=\"value\">London</div>\n"
        val result = ApkgParser.stripHtml(html)
        assertEquals("England\nConstituent country of the United Kingdom.\n\nCapital\nLondon", result)
    }

    @Test
    fun generateCardsSingleTemplate() {
        val fields = mapOf("Front" to "Hello", "Back" to "World")
        val rendered = TemplateRenderer.render("{{Front}}", fields)
        val stripped = ApkgParser.stripHtml(rendered)
        assertEquals("Hello", stripped)
    }

    @Test
    fun geographyDeckEndToEndPipeline() {
        // Simulates what happens when processing a geography deck card
        val fields = mapOf(
            "Country" to "England",
            "Country info" to "Constituent country of the United Kingdom.",
            "Capital" to "London",
            "Capital info" to "",
            "Capital hint" to "Not a sovereign country",
            "Flag" to """<img src="ug-flag-england.svg" />""",
            "Flag similarity" to "",
            "Map" to """<img src="ug-map-england.png" />"""
        )

        // Real templates from Ultimate Geography deck
        val qfmt0 =
            "{{#Capital}}\n  <div class=\"value value--top\">{{Country}}</div>\n  " +
                "{{#Country info}}<div class=\"info\">{{Country info}}</div>{{/Country info}}" +
                "\n\n  <hr>\n\n  <div class=\"type\">Capital</div>\n  " +
                "<div class=\"value\">?</div>\n{{/Capital}}"
        val afmt0 =
            "<div class=\"value value--top\">{{Country}}</div>\n" +
                "{{#Country info}}<div class=\"info\">{{Country info}}</div>{{/Country info}}" +
                "\n\n<hr id=answer>\n\n<div class=\"type\">Capital</div>\n" +
                "<div class=\"value\">{{Capital}}</div>\n" +
                "{{#Capital info}}<div class=\"info\">{{Capital info}}</div>{{/Capital info}}\n"

        // Template 0: Country -> Capital
        val q0 = TemplateRenderer.render(qfmt0, fields)
        val a0 = TemplateRenderer.render(afmt0, fields, frontSide = q0)
        val (q0m, q0media) = ApkgParser.convertImagesToMarkers(q0)
        val (a0m, _) = ApkgParser.convertImagesToMarkers(a0)
        val q0text = ApkgParser.stripHtml(q0m)
        val a0text = ApkgParser.stripHtml(a0m)

        // Front should be clean text without excessive line breaks
        assertEquals("England\nConstituent country of the United Kingdom.\n\nCapital\n?", q0text)
        // Answer should have country, info, separator, then capital
        assertEquals("England\nConstituent country of the United Kingdom.\n\nCapital\nLondon", a0text)
        assertTrue(q0media.isEmpty())

        // Template 1: Capital -> Country
        val q1 = TemplateRenderer.render("{{Capital}}", fields)
        val a1 = TemplateRenderer.render("{{Country}}", fields)
        assertEquals("London", ApkgParser.stripHtml(q1))
        assertEquals("England", ApkgParser.stripHtml(a1))

        // Template 2: Flag -> Country
        val q2 = TemplateRenderer.render("{{Flag}}", fields)
        val (q2m, q2media) = ApkgParser.convertImagesToMarkers(q2)
        assertEquals("[img:ug-flag-england.svg]", ApkgParser.stripHtml(q2m))
        assertEquals(setOf("ug-flag-england.svg"), q2media)

        // Template 3: Map -> Country
        val q3 = TemplateRenderer.render("{{Map}}", fields)
        val (q3m, q3media) = ApkgParser.convertImagesToMarkers(q3)
        assertEquals("[img:ug-map-england.png]", ApkgParser.stripHtml(q3m))
        assertEquals(setOf("ug-map-england.png"), q3media)
    }

    @Test
    fun cleanHtmlPreservesFormatting() {
        val html = "<b>Bold</b> and <i>italic</i>"
        val result = ApkgParser.cleanHtml(html)
        assertEquals("<b>Bold</b> and <i>italic</i>", result)
    }

    @Test
    fun cleanHtmlRemovesSoundDirectives() {
        val result = ApkgParser.cleanHtml("[sound:audio.mp3] Some text")
        assertEquals("Some text", result)
    }

    @Test
    fun cleanHtmlNormalizesNbsp() {
        val result = ApkgParser.cleanHtml("Hello&nbsp;World")
        assertEquals("Hello World", result)
    }

    @Test
    fun emptyFieldProducesBlankCard() {
        val fields = mapOf("Front" to "", "Back" to "Answer")
        val rendered = TemplateRenderer.render("{{Front}}", fields)
        val stripped = ApkgParser.stripHtml(rendered)
        assertTrue(stripped.isBlank())
    }

    @Test
    fun htmlOnlyFieldProducesBlankAfterStripping() {
        val fields = mapOf("Front" to "<div></div><br>", "Back" to "Answer")
        val rendered = TemplateRenderer.render("{{Front}}", fields)
        val stripped = ApkgParser.stripHtml(rendered)
        assertTrue(stripped.isBlank())
    }

    @Test
    fun fieldWithImageOnlyProducesImageMarker() {
        val fields = mapOf("Flag" to """<img src="flag.svg" />""")
        val rendered = TemplateRenderer.render("{{Flag}}", fields)
        val (withMarkers, media) = ApkgParser.convertImagesToMarkers(rendered)
        val stripped = ApkgParser.stripHtml(withMarkers)
        assertEquals("[img:flag.svg]", stripped)
        assertEquals(setOf("flag.svg"), media)
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun parseConnectionWithRealAnkiSchema() {
        // BundledSQLiteDriver requires native JNI — skip on JVM host tests
        val driver = BundledSQLiteDriver()
        val connection = try {
            driver.open(":memory:")
        } catch (_: UnsatisfiedLinkError) {
            return
        }
        // Create an in-memory SQLite database with real Anki schema and data

        try {
            // Create the col table with models JSON (same structure as real Anki DBs)
            connection.exec(
                "CREATE TABLE col (id INTEGER PRIMARY KEY, models TEXT, decks TEXT)"
            )

            val modelsJson = """{"1574587964637":{"id":1574587964637,"name":"Ultimate Geography","flds":[{"name":"Country","ord":0},{"name":"Country info","ord":1},{"name":"Capital","ord":2},{"name":"Capital info","ord":3},{"name":"Capital hint","ord":4},{"name":"Flag","ord":5},{"name":"Flag similarity","ord":6},{"name":"Map","ord":7}],"tmpls":[{"ord":0,"name":"Country - Capital","qfmt":"{{#Capital}}\n  <div class=\"value value--top\">{{Country}}</div>\n  {{#Country info}}<div class=\"info\">{{Country info}}</div>{{/Country info}}\n\n  <hr>\n\n  <div class=\"type\">Capital</div>\n  <div class=\"value\">?</div>\n{{/Capital}}","afmt":"<div class=\"value value--top\">{{Country}}</div>\n{{#Country info}}<div class=\"info\">{{Country info}}</div>{{/Country info}}\n\n<hr id=answer>\n\n<div class=\"type\">Capital</div>\n<div class=\"value\">{{Capital}}</div>\n{{#Capital info}}<div class=\"info\">{{Capital info}}</div>{{/Capital info}}\n"},{"ord":1,"name":"Capital - Country","qfmt":"{{#Capital}}\n  <div class=\"value value--top\">?</div>\n\n  <hr>\n\n  <div class=\"type\">Capital</div>\n  <div class=\"value\">{{Capital}}</div>\n  {{#Capital hint}}<div class=\"info\">Hint: {{Capital hint}}</div>{{/Capital hint}}\n{{/Capital}}","afmt":"<div id=answer class=\"value value--top\">{{Country}}</div>\n{{#Country info}}<div class=\"info\">{{Country info}}</div>{{/Country info}}\n\n<hr>\n\n<div class=\"type\">Capital</div>\n<div class=\"value\">{{Capital}}</div>\n{{#Capital hint}}<div class=\"info\">Hint: {{Capital hint}}</div>{{/Capital hint}}\n"},{"ord":2,"name":"Flag - Country","qfmt":"{{#Flag}}\n  <div class=\"value value--top\">?</div>\n\n  <hr>\n\n  <div class=\"type\">Flag</div>\n  <div class=\"value value--image value--front\">{{Flag}}</div>\n{{/Flag}}","afmt":"<div id=answer class=\"value value--top\">{{Country}}</div>\n{{#Country info}}<div class=\"info\">{{Country info}}</div>{{/Country info}}\n\n<hr>\n\n<div class=\"type\">Flag</div>\n<div class=\"value value--image value--back\">{{Flag}}</div>\n"},{"ord":3,"name":"Map - Country","qfmt":"{{#Map}}\n  <div class=\"value value--top\">?</div>\n\n  <hr>\n\n  <div class=\"type\">Location</div>\n  <div class=\"value value--image\">{{Map}}</div>\n{{/Map}}","afmt":"<div id=answer class=\"value value--top\">{{Country}}</div>\n{{#Country info}}<div class=\"info\">{{Country info}}</div>{{/Country info}}\n\n<hr>\n\n<div class=\"type\">Location</div>\n<div class=\"value value--image\">{{Map}}</div>\n"}]}}"""

            val decksJson = """{"1":{"name":"Default"},"123":{"name":"Ultimate Geography"}}"""

            val insertCol = connection.prepare(
                "INSERT INTO col (id, models, decks) VALUES (1, ?, ?)"
            )
            insertCol.bindText(1, modelsJson)
            insertCol.bindText(2, decksJson)
            try {
                insertCol.step()
            } finally {
                insertCol.close()
            }

            // Create notes table (includes guid as in real Anki schemas)
            connection.exec(
                "CREATE TABLE notes (id INTEGER PRIMARY KEY, mid INTEGER, guid TEXT, flds TEXT)"
            )

            val sep = '\u001F' // Anki field separator
            // England: Country, Country info, Capital, Capital info, Capital hint, Flag, Flag similarity, Map
            val englandFields = listOf(
                "England",
                "Constituent country of the United Kingdom.",
                "London",
                "",
                "Not a sovereign country",
                """<img src="ug-flag-england.svg" />""",
                "",
                """<img src="ug-map-england.png" />"""
            ).joinToString(sep.toString())

            // Japan: has Capital info filled in
            val japanFields = listOf(
                "Japan",
                "",
                "Tokyo",
                "Most populous metropolitan area in the world.",
                "",
                """<img src="ug-flag-japan.svg" />""",
                "",
                """<img src="ug-map-japan.png" />"""
            ).joinToString(sep.toString())

            val insertNote = connection.prepare(
                "INSERT INTO notes (id, mid, guid, flds) VALUES (?, ?, ?, ?)"
            )
            insertNote.bindLong(1, 1001)
            insertNote.bindLong(2, 1574587964637)
            insertNote.bindText(3, "guid-england")
            insertNote.bindText(4, englandFields)
            try {
                insertNote.step()
            } finally {
                insertNote.close()
            }

            val insertNote2 = connection.prepare(
                "INSERT INTO notes (id, mid, guid, flds) VALUES (?, ?, ?, ?)"
            )
            insertNote2.bindLong(1, 1002)
            insertNote2.bindLong(2, 1574587964637)
            insertNote2.bindText(3, "guid-japan")
            insertNote2.bindText(4, japanFields)
            try {
                insertNote2.step()
            } finally {
                insertNote2.close()
            }

            // Create cards table (4 templates per note)
            connection.exec(
                "CREATE TABLE cards (id INTEGER PRIMARY KEY, nid INTEGER, ord INTEGER)"
            )

            val cardData = listOf(
                Triple(2001L, 1001L, 0),
                Triple(2002L, 1001L, 1),
                Triple(2003L, 1001L, 2),
                Triple(2004L, 1001L, 3),
                Triple(2005L, 1002L, 0),
                Triple(2006L, 1002L, 1),
                Triple(2007L, 1002L, 2),
                Triple(2008L, 1002L, 3)
            )
            for ((id, nid, ord) in cardData) {
                val stmt = connection.prepare(
                    "INSERT INTO cards (id, nid, ord) VALUES (?, ?, ?)"
                )
                stmt.bindLong(1, id)
                stmt.bindLong(2, nid)
                stmt.bindLong(3, ord.toLong())
                try {
                    stmt.step()
                } finally {
                    stmt.close()
                }
            }

            // Run the full parsing pipeline
            val result = ApkgParser.parseConnection(connection)

            // Should get 8 cards (4 templates × 2 notes)
            assertEquals(8, result.cards.size)
            assertEquals("Ultimate Geography", result.deckName)

            // Verify England Country-Capital card (template 0) — HTML is now preserved
            val englandCapitalQ = result.cards[0].front
            val englandCapitalA = result.cards[0].back
            // Front/back now contain HTML tags (not stripped)
            assertTrue(englandCapitalQ.contains("England"))
            assertTrue(englandCapitalQ.contains("Capital"))
            assertTrue(englandCapitalA.contains("London"))

            // Stable guid is preserved per (note.guid, template.ord) since this note has 4 templates.
            assertEquals("guid-england#0", result.cards[0].ankiGuid)
            assertEquals("guid-england#1", result.cards[1].ankiGuid)

            // Verify England Flag-Country card (template 2) has image marker
            val englandFlagQ = result.cards[2].front
            assertTrue(englandFlagQ.contains("[img:ug-flag-england.svg]"))

            // Verify England Map-Country card (template 3) has image marker
            val englandMapQ = result.cards[3].front
            assertTrue(englandMapQ.contains("[img:ug-map-england.png]"))

            // Verify referenced media
            assertTrue(result.referencedMedia.contains("ug-flag-england.svg"))
            assertTrue(result.referencedMedia.contains("ug-map-england.png"))
            assertTrue(result.referencedMedia.contains("ug-flag-japan.svg"))
            assertTrue(result.referencedMedia.contains("ug-map-japan.png"))
        } finally {
            connection.close()
        }
    }
}
