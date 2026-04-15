package com.kado.app.domain.repository

interface DeviceRepository {
    suspend fun getDecks(): DeviceDecksResponse
    suspend fun uploadDeck(aldBytes: ByteArray, filename: String): Boolean
    suspend fun deleteDeck(index: Int): Boolean
}

data class DeviceDecksResponse(val decks: List<DeviceDeck>, val freeMb: Float)

data class DeviceDeck(val name: String, val cards: Int)
