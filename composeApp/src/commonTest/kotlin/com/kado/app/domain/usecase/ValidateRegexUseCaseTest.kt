package com.kado.app.domain.usecase

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidateRegexUseCaseTest {

    private val validate = ValidateRegexUseCase()

    @Test
    fun validRegex_returnsNull() {
        assertNull(validate("hello"))
        assertNull(validate("[a-z]+"))
        assertNull(validate("\\d{3}-\\d{4}"))
    }

    @Test
    fun invalidRegex_returnsErrorMessage() {
        val result = validate("[invalid")
        assertNotNull(result)
    }

    @Test
    fun emptyMatchingPattern_dotStar_returnsWarning() {
        val result = validate(".*")
        assertNotNull(result)
        assertTrue(result.contains("empty"))
    }

    @Test
    fun emptyMatchingPattern_optionalChar_returnsWarning() {
        val result = validate("a?")
        assertNotNull(result)
        assertTrue(result.contains("empty"))
    }

    @Test
    fun nonEmptyMatchingPattern_returnsNull() {
        assertNull(validate("a+"))
        assertNull(validate(".+"))
        assertNull(validate("[a-z]"))
    }
}
