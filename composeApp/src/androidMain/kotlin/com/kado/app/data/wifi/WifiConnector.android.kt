package com.kado.app.data.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import com.kado.app.domain.model.ConnectionState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class WifiConnector(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var currentNetwork: Network? = null

    actual suspend fun connect(ssid: String): ConnectionState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ConnectionState.Error("Requires Android 10+")
        }
        return suspendCancellableCoroutine { continuation ->
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.bindProcessToNetwork(network)
                    currentNetwork = network
                    if (continuation.isActive) {
                        continuation.resume(ConnectionState.Connected)
                    }
                }

                override fun onUnavailable() {
                    if (continuation.isActive) {
                        continuation.resume(ConnectionState.Error("Network unavailable"))
                    }
                }

                override fun onLost(network: Network) {
                    if (network == currentNetwork) {
                        connectivityManager.bindProcessToNetwork(null)
                        currentNetwork = null
                    }
                }
            }

            connectivityManager.requestNetwork(request, callback)

            continuation.invokeOnCancellation {
                connectivityManager.unregisterNetworkCallback(callback)
                connectivityManager.bindProcessToNetwork(null)
            }
        }
    }

    actual suspend fun disconnect() {
        connectivityManager.bindProcessToNetwork(null)
        currentNetwork = null
    }

    actual suspend fun getConnectionState(): ConnectionState {
        if (currentNetwork != null) {
            val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return ConnectionState.Connected
            }
        }
        return ConnectionState.Disconnected
    }
}
