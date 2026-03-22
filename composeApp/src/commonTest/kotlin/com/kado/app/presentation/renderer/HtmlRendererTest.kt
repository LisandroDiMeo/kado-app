package com.kado.app.presentation.renderer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HtmlRendererTest {

    private val renderer = HtmlRenderer()

    @Test
    fun renderPlainText() {
        val segments = renderer.render("Hello world")
        assertEquals(1, segments.size)
        assertIs<HtmlSegment.StyledText>(segments[0])
        assertEquals("Hello world", (segments[0] as HtmlSegment.StyledText).annotatedString.text)
    }

    @Test
    fun renderBoldTag() {
        val segments = renderer.render("<b>Bold</b> text")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Bold text", text.text)
    }

    @Test
    fun renderItalicTag() {
        val segments = renderer.render("<i>Italic</i> text")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Italic text", text.text)
    }

    @Test
    fun renderStrongTag() {
        val segments = renderer.render("<strong>Strong</strong>")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Strong", text.text)
    }

    @Test
    fun renderEmTag() {
        val segments = renderer.render("<em>Emphasis</em>")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Emphasis", text.text)
    }

    @Test
    fun renderUnderlineTag() {
        val segments = renderer.render("<u>Underlined</u>")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Underlined", text.text)
    }

    @Test
    fun renderBreakTag() {
        val segments = renderer.render("Line 1<br>Line 2")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Line 1\nLine 2", text.text)
    }

    @Test
    fun renderSelfClosingBreak() {
        val segments = renderer.render("A<br/>B")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("A\nB", text.text)
    }

    @Test
    fun renderDivClosingAddsNewline() {
        val segments = renderer.render("<div>Block 1</div><div>Block 2</div>")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertTrue(text.text.contains("Block 1\n"))
        assertTrue(text.text.contains("Block 2"))
    }

    @Test
    fun renderHrTag() {
        val segments = renderer.render("Before<hr>After")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertTrue(text.text.contains("———"))
    }

    @Test
    fun renderListItems() {
        val segments = renderer.render("<ul><li>Item 1</li><li>Item 2</li></ul>")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertTrue(text.text.contains("Item 1"))
        assertTrue(text.text.contains("Item 2"))
        assertTrue(text.text.contains("\u2022"))
    }

    @Test
    fun renderNestedTags() {
        val segments = renderer.render("<b><i>Bold Italic</i></b>")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Bold Italic", text.text)
    }

    @Test
    fun renderHtmlEntities() {
        val segments = renderer.render("&amp; &lt; &gt; &quot;")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("& < > \"", text.text)
    }

    @Test
    fun renderNumericEntity() {
        val segments = renderer.render("&#39;")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("'", text.text)
    }

    @Test
    fun renderImageMarkerSplitsSegments() {
        val segments = renderer.render("Text [img:photo.jpg] more text")
        assertEquals(3, segments.size)
        assertIs<HtmlSegment.StyledText>(segments[0])
        assertIs<HtmlSegment.ImageRef>(segments[1])
        assertIs<HtmlSegment.StyledText>(segments[2])
        assertEquals("photo.jpg", (segments[1] as HtmlSegment.ImageRef).filename)
    }

    @Test
    fun renderImageMarkerOnly() {
        val segments = renderer.render("[img:solo.png]")
        assertEquals(1, segments.size)
        assertIs<HtmlSegment.ImageRef>(segments[0])
        assertEquals("solo.png", (segments[0] as HtmlSegment.ImageRef).filename)
    }

    @Test
    fun renderSourceLineBreaksCollapsed() {
        val html = """<div class="value value--top">England</div>
  <div class="info">Constituent country of the United Kingdom.</div>
  <hr>
  <div class="type">Capital</div>
  <div class="value">?</div>"""
        val segments = renderer.render(html)
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString.text
        // Source newlines should not produce extra line breaks
        assertTrue(!text.contains("\n\n\n"), "Should not have triple newlines, got: $text")
        assertTrue(text.contains("England\n"))
        assertTrue(text.contains("Constituent country of the United Kingdom.\n"))
        assertTrue(text.contains("Capital\n"))
        assertTrue(text.contains("?"))
    }

    @Test
    fun renderEmptyString() {
        val segments = renderer.render("")
        assertTrue(segments.isEmpty())
    }

    @Test
    fun renderUnknownTagsStrippedGracefully() {
        val segments = renderer.render("<custom>Content</custom>")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Content", text.text)
    }

    @Test
    fun renderMalformedHtmlDoesNotCrash() {
        val segments = renderer.render("<b>Unclosed bold <i>and italic")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertEquals("Unclosed bold and italic", text.text)
    }

    @Test
    fun renderHeadingTag() {
        val segments = renderer.render("<h1>Title</h1>rest")
        assertEquals(1, segments.size)
        val text = (segments[0] as HtmlSegment.StyledText).annotatedString
        assertTrue(text.text.contains("Title"))
        assertTrue(text.text.contains("rest"))
    }

    @Test
    fun renderHtmlWithImageMarkers() {
        val segments = renderer.render("<b>Country</b> [img:flag.svg] <i>Info</i>")
        assertEquals(3, segments.size)
        assertIs<HtmlSegment.StyledText>(segments[0])
        assertIs<HtmlSegment.ImageRef>(segments[1])
        assertIs<HtmlSegment.StyledText>(segments[2])
        assertEquals("flag.svg", (segments[1] as HtmlSegment.ImageRef).filename)
    }
}
