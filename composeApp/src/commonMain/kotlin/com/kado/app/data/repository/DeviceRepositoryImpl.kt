package com.kado.app.data.repository

import com.kado.app.data.datasource.DeviceApi
import com.kado.app.domain.repository.DeviceDecksResponse
import com.kado.app.domain.repository.DeviceRepository

class DeviceRepositoryImpl(private val deviceApi: DeviceApi) : DeviceRepository {

    override suspend fun getDecks(): DeviceDecksResponse =
        deviceApi.getDecks()

    override suspend fun uploadDeck(aldBytes: ByteArray, filename: String): Boolean =
        deviceApi.uploadDeck(aldBytes, filename)

    override suspend fun deleteDeck(index: Int): Boolean =
        deviceApi.deleteDeck(index)
}
