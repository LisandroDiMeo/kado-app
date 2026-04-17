package com.kado.app.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class DeckDetailRoute(val deckId: Long)

@Serializable
data class DeckEditRoute(val deckId: Long = -1L) // -1 = create new

@Serializable
data class CardEditRoute(val deckId: Long, val cardId: Long = -1L) // -1 = create new

@Serializable
data class BulkEditRoute(val deckId: Long)

@Serializable
data class ReviewRoute(val deckId: Long, val subDeckIndex: Int = -1)

@Serializable
data class StatsRoute(val deckIds: List<Long> = emptyList())

@Serializable
object ConnectionRoute

@Serializable
data class TransferRoute(val deckId: Long, val subDeckIndex: Int = -1)

@Serializable
object SettingsRoute

@Serializable
object AboutRoute

@Serializable
object AppSettingsRoute

@Serializable
data class PartitionRoute(val deckId: Long)

@Serializable
object DonateRoute

@Serializable
object HelpRoute

@Serializable
data class AlgorithmDetailRoute(val algorithmId: String, val focusParameter: String = "")
