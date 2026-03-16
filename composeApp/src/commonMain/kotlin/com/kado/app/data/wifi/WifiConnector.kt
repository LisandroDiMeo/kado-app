package com.kado.app.data.wifi

import com.kado.app.domain.model.ConnectionState

expect class WifiConnector {
    suspend fun connect(ssid: String): ConnectionState
    suspend fun disconnect()
    suspend fun getConnectionState(): ConnectionState
}
