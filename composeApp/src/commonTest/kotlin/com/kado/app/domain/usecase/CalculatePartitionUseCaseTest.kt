package com.kado.app.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatePartitionUseCaseTest {

    private val useCase = CalculatePartitionUseCase()

    @Test
    fun emptyDeck_returnsZeroSubDecks() {
        val result = useCase(totalCards = 0, batchSize = 10)
        assertEquals(0, result.subDeckCount)
        assertEquals(0, result.lastSubDeckSize)
    }

    @Test
    fun exactDivisor_allSubDecksEqualSize() {
        val result = useCase(totalCards = 30, batchSize = 10)
        assertEquals(3, result.subDeckCount)
        assertEquals(10, result.lastSubDeckSize)
    }

    @Test
    fun remainder_lastSubDeckSmaller() {
        val result = useCase(totalCards = 25, batchSize = 10)
        assertEquals(3, result.subDeckCount)
        assertEquals(5, result.lastSubDeckSize)
    }

    @Test
    fun singleCard_oneSubDeck() {
        val result = useCase(totalCards = 1, batchSize = 10)
        assertEquals(1, result.subDeckCount)
        assertEquals(1, result.lastSubDeckSize)
    }

    @Test
    fun batchSizeLargerThanCards_oneSubDeck() {
        val result = useCase(totalCards = 5, batchSize = 100)
        assertEquals(1, result.subDeckCount)
        assertEquals(5, result.lastSubDeckSize)
    }

    @Test
    fun batchSizeEqualsCards_oneSubDeck() {
        val result = useCase(totalCards = 10, batchSize = 10)
        assertEquals(1, result.subDeckCount)
        assertEquals(10, result.lastSubDeckSize)
    }

    @Test
    fun batchSizeOne_eachCardIsSubDeck() {
        val result = useCase(totalCards = 5, batchSize = 1)
        assertEquals(5, result.subDeckCount)
        assertEquals(1, result.lastSubDeckSize)
    }
}
