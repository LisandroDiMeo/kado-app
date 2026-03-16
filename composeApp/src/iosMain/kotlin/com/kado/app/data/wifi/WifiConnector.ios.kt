package com.kado.app.data.wifi

import com.kado.app.domain.model.ConnectionState
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.NetworkExtension.NEHotspotConfiguration
import platform.NetworkExtension.NEHotspotConfigurationManager
import kotlin.coroutines.resume

actual class WifiConnector {
    private var connectedSsid: String? = null

    actual suspend fun connect(ssid: String): ConnectionState {
        return suspendCancellableCoroutine { continuation ->
            val configuration = NEHotspotConfiguration(sSID = ssid)
            configuration.setJoinOnce(true)

            NEHotspotConfigurationManager.sharedManager.applyConfiguration(configuration) { error ->
                if (error == null) {
                    connectedSsid = ssid
                    if (continuation.isActive) {
                        continuation.resume(ConnectionState.Connected)
                    }
                } else {
                    connectedSsid = null
                    if (continuation.isActive) {
                        continuation.resume(ConnectionState.Error(error.localizedDescription ?: "Connection failed"))
                    }
                }
            }
        }
    }

    actual suspend fun disconnect() {
        connectedSsid?.let {
            NEHotspotConfigurationManager.sharedManager.removeConfigurationForSSID(it)
        }
        connectedSsid = null
    }

    actual suspend fun getConnectionState(): ConnectionState {
        return if (connectedSsid != null) ConnectionState.Connected else ConnectionState.Disconnected
    }
}
