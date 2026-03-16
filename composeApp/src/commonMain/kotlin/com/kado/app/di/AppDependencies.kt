package com.kado.app.di

import com.kado.app.data.datasource.DeviceApi
import com.kado.app.data.importer.ApkgImporter
import com.kado.app.data.local.KadoDatabase
import com.kado.app.data.repository.DeckRepositoryImpl
import com.kado.app.data.repository.DeviceRepositoryImpl
import com.kado.app.data.repository.SettingsRepositoryImpl
import com.kado.app.data.wifi.WifiConnector
import com.kado.app.domain.repository.DeckRepository
import com.kado.app.domain.repository.DeviceRepository
import com.kado.app.domain.repository.SettingsRepository

object AppDependencies {
    lateinit var database: KadoDatabase

    private val deviceApi by lazy { DeviceApi() }

    val deckRepository: DeckRepository by lazy {
        DeckRepositoryImpl(
            deckDao = database.deckDao(),
            cardDao = database.cardDao(),
            cardStateDao = database.cardStateDao()
        )
    }

    val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(deviceApi)
    }

    val apkgImporter: ApkgImporter by lazy {
        ApkgImporter(deckRepository)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(database.settingsDao())
    }

    lateinit var wifiConnector: WifiConnector

    val isWifiConnectorInitialized: Boolean
        get() = ::wifiConnector.isInitialized
}
