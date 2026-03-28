package com.kado.app.domain.usecase

data class PartitionInfo(
    val subDeckCount: Int,
    val lastSubDeckSize: Int
)

class CalculatePartitionUseCase {

    operator fun invoke(totalCards: Int, batchSize: Int): PartitionInfo {
        if (totalCards == 0) return PartitionInfo(subDeckCount = 0, lastSubDeckSize = 0)
        val subDeckCount = (totalCards + batchSize - 1) / batchSize
        val remainder = totalCards % batchSize
        val lastSubDeckSize = if (remainder == 0) batchSize else remainder
        return PartitionInfo(subDeckCount = subDeckCount, lastSubDeckSize = lastSubDeckSize)
    }
}
