package com.kado.app.data.converter

import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardContent
import com.kado.app.domain.model.Deck
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AldConverterTest {

    private val deck = Deck(id = 1, name = "Test Deck", dailyLimit = 20, createdAt = 0)

    private fun plainCard(id: Long, front: String, back: String) = Card(
        id = id, deckId = 1,
        front = CardContent.PlainText(front),
        back = CardContent.PlainText(back)
    )

    @Test
    fun stripImagesRemovesMarkers() {
        assertEquals("England", AldConverter.stripImages("[img:flag.svg] England"))
    }

    @Test
    fun stripImagesMultipleMarkers() {
        assertEquals("text", AldConverter.stripImages("[img:a.png] text [img:b.svg]"))
    }

    @Test
    fun stripImagesNoMarkers() {
        assertEquals("Hello World", AldConverter.stripImages("Hello World"))
    }

    @Test
    fun stripImagesOnlyImage() {
        assertEquals("", AldConverter.stripImages("[img:flag.svg]"))
    }

    @Test
    fun toAldSkipsImageOnlyCards() {
        val cards = listOf(
            plainCard(1, "France", "Paris"),
            Card(id = 2, deckId = 1, front = CardContent.PlainText("[img:flag.svg]"), back = CardContent.PlainText("France")),
            plainCard(3, "Germany", "Berlin")
        )
        val ald = AldConverter.toAld(deck, cards).decodeToString()
        val lines = ald.lines()

        assertEquals("ALD1", lines[0])
        assertEquals("Test Deck", lines[1])
        assertEquals("2", lines[2])
        assertEquals("France\tParis", lines[3])
        assertEquals("Germany\tBerlin", lines[4])
    }

    @Test
    fun toAldSkipsCardsWithImageOnlyBack() {
        val cards = listOf(
            Card(id = 1, deckId = 1, front = CardContent.PlainText("France"), back = CardContent.PlainText("[img:map.png]"))
        )
        val ald = AldConverter.toAld(deck, cards).decodeToString()
        val lines = ald.lines()

        assertEquals("0", lines[2])
    }

    @Test
    fun toAldMixedContentPreservesText() {
        val cards = listOf(
            Card(id = 1, deckId = 1, front = CardContent.PlainText("[img:flag.svg] France"), back = CardContent.PlainText("Paris"))
        )
        val ald = AldConverter.toAld(deck, cards).decodeToString()
        val lines = ald.lines()

        assertEquals("1", lines[2])
        assertEquals("France\tParis", lines[3])
    }

    @Test
    fun toAldEscapesTabsAndNewlines() {
        val cards = listOf(
            plainCard(1, "Line1\nLine2", "A\tB")
        )
        val ald = AldConverter.toAld(deck, cards).decodeToString()
        val lines = ald.lines()

        assertEquals("Line1\\nLine2\tA B", lines[3])
    }

    @Test
    fun toAldFilename() {
        assertEquals("test_deck.ald", AldConverter.toAldFilename("Test Deck"))
        assertEquals("ultimate_geography.ald", AldConverter.toAldFilename("Ultimate Geography"))
    }

    @Test
    fun toAldAllImageCardsProducesEmptyDeck() {
        val cards = listOf(
            Card(id = 1, deckId = 1, front = CardContent.PlainText("[img:flag.svg]"), back = CardContent.PlainText("France")),
            Card(id = 2, deckId = 1, front = CardContent.PlainText("[img:map.png]"), back = CardContent.PlainText("France"))
        )
        val ald = AldConverter.toAld(deck, cards).decodeToString()
        val lines = ald.lines()

        assertEquals("0", lines[2])
    }

    @Test
    fun toPlainTextStripsHtmlTags() {
        assertEquals("Bold text", AldConverter.toPlainText("<b>Bold</b> text"))
    }

    @Test
    fun toPlainTextStripsImagesAndHtml() {
        assertEquals("France", AldConverter.toPlainText("[img:flag.svg] <b>France</b>"))
    }

    @Test
    fun toPlainTextPreservesLineBreaks() {
        assertEquals("Line1\nLine2", AldConverter.toPlainText("Line1<br>Line2"))
    }

    @Test
    fun toAldStripsHtmlFromRichContent() {
        val cards = listOf(
            Card(
                id = 1, deckId = 1,
                front = CardContent.RichText("<b>France</b>"),
                back = CardContent.RichText("<i>Paris</i>")
            )
        )
        val ald = AldConverter.toAld(deck, cards).decodeToString()
        val lines = ald.lines()

        assertEquals("1", lines[2])
        assertEquals("France\tParis", lines[3])
    }
}
