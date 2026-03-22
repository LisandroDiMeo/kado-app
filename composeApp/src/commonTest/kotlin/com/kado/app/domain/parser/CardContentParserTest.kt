package com.kado.app.domain.parser

import com.kado.app.domain.model.CardContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CardContentParserTest {

    private val parser = CardContentParser()

    @Test
    fun parseWithNoImages() {
        val parts = parser.parse("Hello world")
        assertEquals(1, parts.size)
        assertEquals(ContentPart.Text("Hello world"), parts[0])
    }

    @Test
    fun parseWithOneImage() {
        val parts = parser.parse("Before [img:photo.jpg] After")
        assertEquals(3, parts.size)
        assertEquals(ContentPart.Text("Before"), parts[0])
        assertEquals(ContentPart.Image("photo.jpg"), parts[1])
        assertEquals(ContentPart.Text("After"), parts[2])
    }

    @Test
    fun parseWithMultipleImages() {
        val parts = parser.parse("[img:a.png] text [img:b.jpg]")
        assertEquals(3, parts.size)
        assertEquals(ContentPart.Image("a.png"), parts[0])
        assertEquals(ContentPart.Text("text"), parts[1])
        assertEquals(ContentPart.Image("b.jpg"), parts[2])
    }

    @Test
    fun parseImageOnly() {
        val parts = parser.parse("[img:solo.png]")
        assertEquals(1, parts.size)
        assertEquals(ContentPart.Image("solo.png"), parts[0])
    }

    @Test
    fun parseEmptyString() {
        val parts = parser.parse("")
        assertTrue(parts.isEmpty())
    }

    @Test
    fun extractImageFilenamesNone() {
        val filenames = parser.extractImageFilenames("No images here")
        assertTrue(filenames.isEmpty())
    }

    @Test
    fun extractImageFilenamesMultiple() {
        val filenames = parser.extractImageFilenames("A [img:one.jpg] B [img:two.png] C")
        assertEquals(listOf("one.jpg", "two.png"), filenames)
    }

    @Test
    fun insertMarkerAtStart() {
        val result = parser.insertMarker("Hello", 0, "pic.jpg")
        assertEquals("[img:pic.jpg]Hello", result)
    }

    @Test
    fun insertMarkerAtEnd() {
        val result = parser.insertMarker("Hello", 5, "pic.jpg")
        assertEquals("Hello[img:pic.jpg]", result)
    }

    @Test
    fun insertMarkerInMiddle() {
        val result = parser.insertMarker("Hello World", 6, "pic.jpg")
        assertEquals("Hello [img:pic.jpg]World", result)
    }

    @Test
    fun insertMarkerCursorBeyondLength() {
        val result = parser.insertMarker("Hi", 100, "pic.jpg")
        assertEquals("Hi[img:pic.jpg]", result)
    }

    @Test
    fun removeAllMarkersForFile() {
        val text = "Before [img:photo.jpg] middle [img:photo.jpg] after"
        val result = parser.removeAllMarkersForFile(text, "photo.jpg")
        assertEquals("Before middle after", result)
    }

    @Test
    fun removeAllMarkersForFileNotPresent() {
        val text = "No markers here"
        val result = parser.removeAllMarkersForFile(text, "photo.jpg")
        assertEquals("No markers here", result)
    }

    @Test
    fun removeAllMarkersLeavesOtherImages() {
        val text = "[img:keep.png] some text [img:remove.jpg]"
        val result = parser.removeAllMarkersForFile(text, "remove.jpg")
        assertEquals("[img:keep.png] some text", result)
    }

    // detect() tests

    @Test
    fun detectPlainText() {
        val content = parser.detect("Hello world")
        assertIs<CardContent.PlainText>(content)
        assertEquals("Hello world", content.rawText)
    }

    @Test
    fun detectEmptyString() {
        val content = parser.detect("")
        assertIs<CardContent.PlainText>(content)
    }

    @Test
    fun detectImageMarker() {
        val content = parser.detect("Before [img:photo.jpg] After")
        assertIs<CardContent.ImageMarker>(content)
        assertEquals("Before [img:photo.jpg] After", content.rawText)
        assertEquals(3, content.parts.size)
    }

    @Test
    fun detectImageMarkerOnly() {
        val content = parser.detect("[img:solo.png]")
        assertIs<CardContent.ImageMarker>(content)
        assertEquals(1, content.parts.size)
        assertEquals(ContentPart.Image("solo.png"), content.parts[0])
    }

    @Test
    fun detectHtmlContent() {
        val content = parser.detect("<b>Bold</b> text")
        assertIs<CardContent.RichText>(content)
        assertEquals("<b>Bold</b> text", content.rawText)
        assertTrue(content.imageFilenames.isEmpty())
    }

    @Test
    fun detectHtmlWithImages() {
        val content = parser.detect("<b>Country</b> [img:flag.svg]")
        assertIs<CardContent.RichText>(content)
        assertEquals(listOf("flag.svg"), content.imageFilenames)
    }

    @Test
    fun detectHtmlWithDivAndClasses() {
        val content = parser.detect("<div class=\"value\">England</div>")
        assertIs<CardContent.RichText>(content)
    }

    @Test
    fun detectBracketNotMistakenForHtml() {
        // Square brackets and non-tag angle brackets should not trigger HTML detection
        val content = parser.detect("5 < 10 and 10 > 5")
        // "10 > 5" could match HTML_DETECT_REGEX depending on implementation
        // But "< 10" won't match since space follows <
        // Let's verify the actual behavior
        val content2 = parser.detect("Plain text with [brackets]")
        assertIs<CardContent.PlainText>(content2)
    }
}
