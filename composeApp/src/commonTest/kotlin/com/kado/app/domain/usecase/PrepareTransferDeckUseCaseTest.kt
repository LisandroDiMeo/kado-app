package com.kado.app.domain.usecase

import com.kado.app.domain.model.Card
import com.kado.app.domain.model.CardContent
import com.kado.app.domain.model.Deck
import com.kado.app.test.fakes.FakeDeckRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PrepareTransferDeckUseCaseTest {

    private fun card(id: Long, front: String, back: String) = Card(
        id = id,
        deckId = 1,
        front = CardContent.PlainText(front),
        back = CardContent.PlainText(back)
    )

    @Test
    fun fullDeck_usesOriginalName() = runTest {
        val repo = FakeDeckRepository().apply {
            decks.add(Deck(id = 1, name = "My Deck"))
            cards.add(card(1, "front", "back"))
        }
        val useCase = PrepareTransferDeckUseCase(repo)
        val result = useCase(deckId = 1, subDeckIndex = null)

        assertEquals("My Deck", result.displayName)
        assertTrue(result.aldBytes.isNotEmpty())
        assertEquals("my_deck.ald", result.filename)
    }

    @Test
    fun subDeck_appendsPartNumber() = runTest {
        val repo = FakeDeckRepository().apply {
            decks.add(Deck(id = 1, name = "My Deck"))
            cards.add(card(1, "front", "back").copy(subDeckIndex = 2))
        }
        val useCase = PrepareTransferDeckUseCase(repo)
        val result = useCase(deckId = 1, subDeckIndex = 2)

        assertEquals("My Deck - Part 3", result.displayName)
    }

    @Test
    fun subDeckIndex0_appendsPart1() = runTest {
        val repo = FakeDeckRepository().apply {
            decks.add(Deck(id = 1, name = "Test"))
            cards.add(card(1, "a", "b").copy(subDeckIndex = 0))
        }
        val useCase = PrepareTransferDeckUseCase(repo)
        val result = useCase(deckId = 1, subDeckIndex = 0)

        assertEquals("Test - Part 1", result.displayName)
    }

    @Test
    fun deckNotFound_throwsException() = runTest {
        val repo = FakeDeckRepository()
        val useCase = PrepareTransferDeckUseCase(repo)

        assertFailsWith<IllegalStateException> {
            useCase(deckId = 999, subDeckIndex = null)
        }
    }

    @Test
    fun formatSubDeckName_noSubDeck_returnsBaseName() {
        assertEquals("My Deck", PrepareTransferDeckUseCase.formatSubDeckName("My Deck", null))
    }

    @Test
    fun formatSubDeckName_withSubDeck_appendsPart() {
        assertEquals("My Deck - Part 1", PrepareTransferDeckUseCase.formatSubDeckName("My Deck", 0))
        assertEquals("My Deck - Part 5", PrepareTransferDeckUseCase.formatSubDeckName("My Deck", 4))
    }
}
