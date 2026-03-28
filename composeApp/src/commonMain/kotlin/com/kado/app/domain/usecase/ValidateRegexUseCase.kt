package com.kado.app.domain.usecase

class ValidateRegexUseCase {

    operator fun invoke(pattern: String): String? {
        val regex = try {
            Regex(pattern)
        } catch (e: Exception) {
            return e.message ?: "Invalid regex"
        }
        if (regex.containsMatchIn("") || regex.find("")?.value == "") {
            return "Pattern must not match empty strings (e.g. .* or a?)"
        }
        return null
    }
}
