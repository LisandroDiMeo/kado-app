package com.kado.app.domain.usecase

import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplyFindReplaceUseCaseTest {

    private val useCase = ApplyFindReplaceUseCase()

    private fun card(id: Long, front: String, back: String) = Card(
        id = id,
        deckId = 1,
        front = CardContent.PlainText(front),
        back = CardContent.PlainText(back)
    )

    private fun params(
        frontFind: String = "",
        frontReplace: String = "",
        frontIsRegex: Boolean = false,
        frontEnabled: Boolean = true,
        backFind: String = "",
        backReplace: String = "",
        backIsRegex: Boolean = false,
        backEnabled: Boolean = true
    ) = FindReplaceParams(
        frontFind,
        frontReplace,
        frontIsRegex,
        frontEnabled,
        backFind,
        backReplace,
        backIsRegex,
        backEnabled
    )

    @Test
    fun plainTextReplace_findsAndReplaces() {
        val cards = listOf(card(1, "hello world", "goodbye world"))
        val result = useCase(cards, params(frontFind = "hello", frontReplace = "hi"))

        assertEquals(1, result.size)
        assertEquals("hi world", result[0].newFront)
        assertEquals("goodbye world", result[0].newBack)
    }

    @Test
    fun regexReplace_appliesPattern() {
        val cards = listOf(card(1, "abc123def", "test"))
        val result = useCase(
            cards,
            params(
                frontFind = "\\d+",
                frontReplace = "NUM",
                frontIsRegex = true
            )
        )

        assertEquals(1, result.size)
        assertEquals("abcNUMdef", result[0].newFront)
    }

    @Test
    fun noChanges_returnsEmptyList() {
        val cards = listOf(card(1, "hello", "world"))
        val result = useCase(cards, params(frontFind = "xyz", frontReplace = "abc"))

        assertTrue(result.isEmpty())
    }

    @Test
    fun frontOnlyReplace_leavesBackUnchanged() {
        val cards = listOf(card(1, "find me", "find me"))
        val result = useCase(
            cards,
            params(
                frontFind = "find",
                frontReplace = "found",
                backEnabled = false
            )
        )

        assertEquals(1, result.size)
        assertEquals("found me", result[0].newFront)
        assertEquals("find me", result[0].newBack)
    }

    @Test
    fun backOnlyReplace_leavesFrontUnchanged() {
        val cards = listOf(card(1, "find me", "find me"))
        val result = useCase(
            cards,
            params(
                frontEnabled = false,
                backFind = "find",
                backReplace = "found"
            )
        )

        assertEquals(1, result.size)
        assertEquals("find me", result[0].newFront)
        assertEquals("found me", result[0].newBack)
    }

    @Test
    fun bothDisabled_returnsEmptyList() {
        val cards = listOf(card(1, "hello", "world"))
        val result = useCase(
            cards,
            params(
                frontFind = "hello",
                frontReplace = "hi",
                frontEnabled = false,
                backFind = "world",
                backReplace = "earth",
                backEnabled = false
            )
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun invalidRegex_returnsOriginalText() {
        val cards = listOf(card(1, "hello", "world"))
        val result = useCase(
            cards,
            params(
                frontFind = "[invalid",
                frontReplace = "x",
                frontIsRegex = true
            )
        )

        assertTrue(result.isEmpty()) // no change since regex failed and returned original
    }

    @Test
    fun multipleCards_onlySomeChange() {
        val cards = listOf(
            card(1, "apple", "fruit"),
            card(2, "banana", "fruit"),
            card(3, "carrot", "vegetable")
        )
        val result = useCase(cards, params(frontFind = "an", frontReplace = "AN"))

        assertEquals(1, result.size)
        assertEquals(2, result[0].card.id)
        assertEquals("bANANa", result[0].newFront)
    }

    @Test
    fun bothFrontAndBack_replaced() {
        val cards = listOf(card(1, "old front", "old back"))
        val result = useCase(
            cards,
            params(
                frontFind = "old",
                frontReplace = "new",
                backFind = "old",
                backReplace = "new"
            )
        )

        assertEquals(1, result.size)
        assertEquals("new front", result[0].newFront)
        assertEquals("new back", result[0].newBack)
    }
}
