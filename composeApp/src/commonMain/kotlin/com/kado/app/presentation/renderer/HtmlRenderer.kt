package com.kado.app.presentation.renderer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

sealed class HtmlSegment {
    data class StyledText(val annotatedString: AnnotatedString) : HtmlSegment()
    data class ImageRef(val filename: String) : HtmlSegment()
}

class HtmlRenderer {

    private val imgMarkerRegex = Regex("\\[img:([^]]+)]")

    fun render(html: String): List<HtmlSegment> {
        val segments = mutableListOf<HtmlSegment>()
        val parts = splitByImageMarkers(html)

        for (part in parts) {
            if (part.startsWith("[img:") && part.endsWith("]")) {
                val filename = part.removePrefix("[img:").removeSuffix("]")
                segments.add(HtmlSegment.ImageRef(filename))
            } else if (part.isNotBlank()) {
                val annotated = parseHtmlToAnnotatedString(part)
                if (annotated.isNotEmpty()) {
                    segments.add(HtmlSegment.StyledText(annotated))
                }
            }
        }

        return segments
    }

    private fun splitByImageMarkers(html: String): List<String> {
        val parts = mutableListOf<String>()
        var lastIndex = 0

        for (match in imgMarkerRegex.findAll(html)) {
            val before = html.substring(lastIndex, match.range.first)
            if (before.isNotEmpty()) {
                parts.add(before)
            }
            parts.add(match.value)
            lastIndex = match.range.last + 1
        }

        val remaining = html.substring(lastIndex)
        if (remaining.isNotEmpty()) {
            parts.add(remaining)
        }

        return parts
    }

    internal fun parseHtmlToAnnotatedString(html: String): AnnotatedString = buildAnnotatedString {
        val styleStack = mutableListOf<SpanStyle>()
        val blockTags = setOf(
            "div", "p", "blockquote", "h1", "h2", "h3", "h4", "h5", "h6",
            "br", "br/", "hr", "li", "ul", "ol", "table", "tr", "td", "th",
            "section", "article", "header", "footer", "nav"
        )
        var i = 0
        val len = html.length
        var afterBlockTag = true // skip leading whitespace and whitespace between block-level tags

        while (i < len) {
            when {
                html[i] == '<' -> {
                    val tagEnd = html.indexOf('>', i)
                    if (tagEnd == -1) {
                        append(html[i])
                        i++
                        continue
                    }

                    val tagContent = html.substring(i + 1, tagEnd).trim()
                    val isClosing = tagContent.startsWith("/")
                    val tagName = (if (isClosing) tagContent.drop(1) else tagContent)
                        .split("\\s+".toRegex())
                        .firstOrNull()
                        ?.lowercase()
                        ?: ""

                    when {
                        !isClosing && (tagName == "b" || tagName == "strong") -> {
                            val style = SpanStyle(fontWeight = FontWeight.Bold)
                            pushStyle(style)
                            styleStack.add(style)
                        }
                        isClosing && (tagName == "b" || tagName == "strong") -> {
                            if (styleStack.isNotEmpty()) {
                                pop()
                                styleStack.removeLastOrNull()
                            }
                        }
                        !isClosing && (tagName == "i" || tagName == "em") -> {
                            val style = SpanStyle(fontStyle = FontStyle.Italic)
                            pushStyle(style)
                            styleStack.add(style)
                        }
                        isClosing && (tagName == "i" || tagName == "em") -> {
                            if (styleStack.isNotEmpty()) {
                                pop()
                                styleStack.removeLastOrNull()
                            }
                        }
                        !isClosing && tagName == "u" -> {
                            val style = SpanStyle(textDecoration = TextDecoration.Underline)
                            pushStyle(style)
                            styleStack.add(style)
                        }
                        isClosing && tagName == "u" -> {
                            if (styleStack.isNotEmpty()) {
                                pop()
                                styleStack.removeLastOrNull()
                            }
                        }
                        !isClosing && tagName.startsWith("h") && tagName.length == 2 && tagName[1].isDigit() -> {
                            val level = tagName[1].digitToInt().coerceIn(1, 6)
                            val fontSize = when (level) {
                                1 -> 28.sp
                                2 -> 24.sp
                                3 -> 20.sp
                                4 -> 18.sp
                                5 -> 16.sp
                                else -> 14.sp
                            }
                            val style = SpanStyle(fontSize = fontSize, fontWeight = FontWeight.Bold)
                            pushStyle(style)
                            styleStack.add(style)
                        }
                        isClosing && tagName.startsWith("h") && tagName.length == 2 && tagName[1].isDigit() -> {
                            if (styleStack.isNotEmpty()) {
                                pop()
                                styleStack.removeLastOrNull()
                            }
                            append("\n")
                        }
                        tagName == "br" || tagName == "br/" -> {
                            append("\n")
                        }
                        !isClosing && tagName == "hr" -> {
                            append("\n———\n")
                        }
                        isClosing && (tagName == "div" || tagName == "p" || tagName == "blockquote") -> {
                            append("\n")
                        }
                        isClosing && (tagName == "li") -> {
                            append("\n")
                        }
                        !isClosing && tagName == "li" -> {
                            append("  \u2022 ")
                        }
                        !isClosing && tagName.startsWith("span") -> {
                            val colorStyle = extractColorFromStyle(tagContent)
                            if (colorStyle != null) {
                                pushStyle(colorStyle)
                                styleStack.add(colorStyle)
                            }
                        }
                        isClosing && tagName == "span" -> {
                            if (styleStack.isNotEmpty()) {
                                pop()
                                styleStack.removeLastOrNull()
                            }
                        }
                        // All other tags: skip silently
                    }

                    if (tagName in blockTags) {
                        afterBlockTag = true
                    }
                    i = tagEnd + 1
                }
                html[i] == '&' -> {
                    afterBlockTag = false
                    val semicolon = html.indexOf(';', i)
                    if (semicolon != -1 && semicolon - i < 10) {
                        val entity = html.substring(i, semicolon + 1)
                        append(decodeEntity(entity))
                        i = semicolon + 1
                    } else {
                        append('&')
                        i++
                    }
                }
                else -> {
                    val ch = html[i]
                    if (ch == '\n' || ch == '\r' || ch == '\t' || (ch == ' ' && afterBlockTag)) {
                        // Skip whitespace between tags entirely
                    } else {
                        afterBlockTag = false
                        append(ch)
                    }
                    i++
                }
            }
        }
    }

    private fun extractColorFromStyle(tagContent: String): SpanStyle? {
        val styleMatch = Regex("""style\s*=\s*["']([^"']*)["']""").find(tagContent) ?: return null
        val styleValue = styleMatch.groupValues[1]
        val colorMatch = Regex("""color\s*:\s*([^;]+)""").find(styleValue) ?: return null
        val colorValue = colorMatch.groupValues[1].trim()
        val color = parseColor(colorValue) ?: return null
        return SpanStyle(color = color)
    }

    private fun parseColor(value: String): Color? = when {
        value.startsWith("#") && value.length == 7 -> {
            try {
                Color(
                    red = value.substring(1, 3).toInt(16) / 255f,
                    green = value.substring(3, 5).toInt(16) / 255f,
                    blue = value.substring(5, 7).toInt(16) / 255f
                )
            } catch (_: Exception) {
                null
            }
        }
        value.startsWith("rgb(") -> {
            try {
                val parts = value.removePrefix("rgb(").removeSuffix(")").split(",").map { it.trim().toInt() }
                if (parts.size == 3) Color(parts[0] / 255f, parts[1] / 255f, parts[2] / 255f) else null
            } catch (_: Exception) {
                null
            }
        }
        else -> NAMED_COLORS[value.lowercase()]
    }

    private fun decodeEntity(entity: String): String = when (entity) {
        "&amp;" -> "&"
        "&lt;" -> "<"
        "&gt;" -> ">"
        "&quot;" -> "\""
        "&#39;", "&apos;" -> "'"
        "&nbsp;" -> " "
        "&ndash;" -> "\u2013"
        "&mdash;" -> "\u2014"
        "&laquo;" -> "\u00AB"
        "&raquo;" -> "\u00BB"
        "&hellip;" -> "\u2026"
        else -> {
            if (entity.startsWith("&#x")) {
                try {
                    val code = entity.removePrefix("&#x").removeSuffix(";").toInt(16)
                    code.toChar().toString()
                } catch (_: Exception) {
                    entity
                }
            } else if (entity.startsWith("&#")) {
                try {
                    val code = entity.removePrefix("&#").removeSuffix(";").toInt()
                    code.toChar().toString()
                } catch (_: Exception) {
                    entity
                }
            } else {
                entity
            }
        }
    }

    companion object {
        private val NAMED_COLORS = mapOf(
            "red" to Color.Red,
            "blue" to Color.Blue,
            "green" to Color(0xFF008000),
            "yellow" to Color.Yellow,
            "orange" to Color(0xFFFFA500),
            "purple" to Color(0xFF800080),
            "white" to Color.White,
            "black" to Color.Black,
            "gray" to Color.Gray,
            "grey" to Color.Gray
        )
    }
}
