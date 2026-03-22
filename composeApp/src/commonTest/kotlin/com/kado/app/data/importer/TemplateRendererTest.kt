package com.kado.app.data.importer

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateRendererTest {

    @Test
    fun simpleFieldReplacement() {
        val result = TemplateRenderer.render(
            "What is the capital of {{Country}}?",
            mapOf("Country" to "France")
        )
        assertEquals("What is the capital of France?", result)
    }

    @Test
    fun multipleFieldsInTemplate() {
        val result = TemplateRenderer.render(
            "{{Country}} - {{Capital}}",
            mapOf("Country" to "France", "Capital" to "Paris")
        )
        assertEquals("France - Paris", result)
    }

    @Test
    fun frontSideSubstitution() {
        val result = TemplateRenderer.render(
            "{{FrontSide}}<hr id=answer>{{Capital}}",
            mapOf("Capital" to "Paris"),
            frontSide = "What is the capital of France?"
        )
        assertEquals("What is the capital of France?<hr id=answer>Paris", result)
    }

    @Test
    fun conditionalWithNonEmptyField() {
        val result = TemplateRenderer.render(
            "{{#Country}}Country: {{Country}}{{/Country}}",
            mapOf("Country" to "France")
        )
        assertEquals("Country: France", result)
    }

    @Test
    fun conditionalWithEmptyField() {
        val result = TemplateRenderer.render(
            "{{#Country}}Country: {{Country}}{{/Country}}",
            mapOf("Country" to "")
        )
        assertEquals("", result)
    }

    @Test
    fun conditionalWithMissingField() {
        val result = TemplateRenderer.render(
            "{{#Country}}Country: {{Country}}{{/Country}}",
            emptyMap()
        )
        assertEquals("", result)
    }

    @Test
    fun inverseConditionalWithEmptyField() {
        val result = TemplateRenderer.render(
            "{{^Hint}}No hint available{{/Hint}}",
            mapOf("Hint" to "")
        )
        assertEquals("No hint available", result)
    }

    @Test
    fun inverseConditionalWithNonEmptyField() {
        val result = TemplateRenderer.render(
            "{{^Hint}}No hint available{{/Hint}}",
            mapOf("Hint" to "Think about Europe")
        )
        assertEquals("", result)
    }

    @Test
    fun typeFieldReplacement() {
        val result = TemplateRenderer.render(
            "Type the answer: {{type:Capital}}",
            mapOf("Capital" to "Paris")
        )
        assertEquals("Type the answer: Paris", result)
    }

    @Test
    fun hintFieldReplacement() {
        val result = TemplateRenderer.render(
            "{{Country}} {{hint:Capital hint}}",
            mapOf("Country" to "France", "Capital hint" to "Starts with P")
        )
        assertEquals("France Starts with P", result)
    }

    @Test
    fun specialMarkersStripped() {
        val result = TemplateRenderer.render(
            "{{edit:Country}} {{Tags}} {{Deck}}",
            mapOf("Country" to "France")
        )
        assertEquals("France  ", result)
    }

    @Test
    fun unknownFieldReplacedWithEmpty() {
        val result = TemplateRenderer.render(
            "Answer: {{NonExistent}}",
            mapOf("Country" to "France")
        )
        assertEquals("Answer: ", result)
    }

    @Test
    fun emptyTemplate() {
        val result = TemplateRenderer.render("", mapOf("Country" to "France"))
        assertEquals("", result)
    }

    @Test
    fun templateWithNoFieldMarkers() {
        val result = TemplateRenderer.render(
            "Hello World",
            mapOf("Country" to "France")
        )
        assertEquals("Hello World", result)
    }

    @Test
    fun complexGeographyTemplate() {
        val fields = mapOf(
            "Country" to "England",
            "Country info" to "Constituent country of the United Kingdom.",
            "Capital" to "London",
            "Capital info" to "",
            "Capital hint" to "Not a sovereign country",
            "Flag" to "<img src=\"ug-flag-england.svg\" />",
            "Flag similarity" to "",
            "Map" to "<img src=\"ug-map-england.png\" />"
        )

        // Country -> Capital template
        val front = TemplateRenderer.render("{{Country}}", fields)
        assertEquals("England", front)

        val back = TemplateRenderer.render(
            "{{FrontSide}}<hr id=answer>{{Capital}}{{#Country info}}<br>{{Country info}}{{/Country info}}",
            fields,
            frontSide = front
        )
        assertEquals(
            "England<hr id=answer>London<br>Constituent country of the United Kingdom.",
            back
        )
    }

    @Test
    fun flagTemplateWithImage() {
        val fields = mapOf(
            "Country" to "England",
            "Flag" to "<img src=\"ug-flag-england.svg\" />"
        )

        val front = TemplateRenderer.render("{{Flag}}", fields)
        assertEquals("<img src=\"ug-flag-england.svg\" />", front)
    }

    @Test
    fun nestedConditionals() {
        val result = TemplateRenderer.render(
            "{{#A}}outer{{#B}}inner{{/B}}{{/A}}",
            mapOf("A" to "yes", "B" to "yes")
        )
        assertEquals("outerinner", result)
    }

    @Test
    fun nestedConditionalInnerEmpty() {
        val result = TemplateRenderer.render(
            "{{#A}}outer{{#B}}inner{{/B}}{{/A}}",
            mapOf("A" to "yes", "B" to "")
        )
        assertEquals("outer", result)
    }

    @Test
    fun fieldWithWhitespaceOnlyTreatedAsEmpty() {
        val result = TemplateRenderer.render(
            "{{#Note}}Has note{{/Note}}",
            mapOf("Note" to "   ")
        )
        assertEquals("", result)
    }
}
