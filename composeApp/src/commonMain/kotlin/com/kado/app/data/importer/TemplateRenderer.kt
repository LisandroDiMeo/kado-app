package com.kado.app.data.importer

object TemplateRenderer {

    private val FIELD_REGEX = Regex("\\{\\{([^{}#/^!]+?)\\}\\}")
    private val CONDITIONAL_REGEX = Regex("\\{\\{#(.+?)\\}\\}(.*?)\\{\\{/\\1\\}\\}", RegexOption.DOT_MATCHES_ALL)
    private val INVERSE_CONDITIONAL_REGEX =
        Regex("\\{\\{\\^(.+?)\\}\\}(.*?)\\{\\{/\\1\\}\\}", RegexOption.DOT_MATCHES_ALL)
    private val TYPE_FIELD_REGEX = Regex("\\{\\{type:(.+?)\\}\\}")
    private val HINT_REGEX = Regex("\\{\\{hint:(.+?)\\}\\}")
    private val EDIT_FIELD_REGEX = Regex("\\{\\{edit:(.+?)\\}\\}")
    private val SPECIAL_PREFIX_REGEX = Regex("\\{\\{(tags|Tags|Deck|Subdeck|CardFlag|Card|Type):?.*?\\}\\}")

    fun render(
        template: String,
        fields: Map<String, String>,
        frontSide: String? = null
    ): String {
        var result = template

        // Replace {{FrontSide}} with the pre-rendered front
        if (frontSide != null) {
            result = result.replace("{{FrontSide}}", frontSide)
        }

        // Process conditionals first (before field replacement)
        // {{#FieldName}}...{{/FieldName}} — show content if field is non-empty
        result = processConditionals(result, fields)

        // {{^FieldName}}...{{/FieldName}} — show content if field is empty
        result = processInverseConditionals(result, fields)

        // Replace {{type:FieldName}} with field value
        result = TYPE_FIELD_REGEX.replace(result) { match ->
            val fieldName = match.groupValues[1].trim()
            fields[fieldName] ?: ""
        }

        // Replace {{hint:FieldName}} with field value
        result = HINT_REGEX.replace(result) { match ->
            val fieldName = match.groupValues[1].trim()
            fields[fieldName] ?: ""
        }

        // Replace {{edit:FieldName}} with field value
        result = EDIT_FIELD_REGEX.replace(result) { match ->
            val fieldName = match.groupValues[1].trim()
            fields[fieldName] ?: ""
        }

        // Strip special Anki markers (tags, Deck, etc.)
        result = SPECIAL_PREFIX_REGEX.replace(result, "")

        // Replace {{FieldName}} with field values
        result = FIELD_REGEX.replace(result) { match ->
            val fieldName = match.groupValues[1].trim()
            fields[fieldName] ?: ""
        }

        return result
    }

    private fun processConditionals(template: String, fields: Map<String, String>): String {
        var result = template
        // Process repeatedly to handle nested conditionals
        var changed = true
        while (changed) {
            val newResult = CONDITIONAL_REGEX.replace(result) { match ->
                val fieldName = match.groupValues[1].trim()
                val content = match.groupValues[2]
                val fieldValue = fields[fieldName] ?: ""
                if (fieldValue.isNotBlank()) content else ""
            }
            changed = newResult != result
            result = newResult
        }
        return result
    }

    private fun processInverseConditionals(template: String, fields: Map<String, String>): String {
        var result = template
        var changed = true
        while (changed) {
            val newResult = INVERSE_CONDITIONAL_REGEX.replace(result) { match ->
                val fieldName = match.groupValues[1].trim()
                val content = match.groupValues[2]
                val fieldValue = fields[fieldName] ?: ""
                if (fieldValue.isBlank()) content else ""
            }
            changed = newResult != result
            result = newResult
        }
        return result
    }
}
