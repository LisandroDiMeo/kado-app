package com.kado.app.presentation.screens.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {
    companion object {
        const val SSID_PREFIX = "KadoLite-"
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _ssid = MutableStateFlow("")
    val ssid: StateFlow<String> = _ssid

    init {
        checkConnection()
    }

    private fun checkConnection() {
        if (!AppDependencies.isWifiConnectorInitialized) return
        viewModelScope.launch {
            _connectionState.value = AppDependencies.wifiConnector.getConnectionState()
        }
    }

    fun onSsidChange(ssid: String) {
        _ssid.value = ssid
    }

    fun connect() {
        val targetSsid = _ssid.value.ifBlank { "${SSID_PREFIX}0000" }
        if (!AppDependencies.isWifiConnectorInitialized) {
            _connectionState.value = ConnectionState.Error("WiFi not available")
            return
        }
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Connecting
            _connectionState.value = AppDependencies.wifiConnector.connect(targetSsid)
        }
    }

    fun disconnect() {
        if (!AppDependencies.isWifiConnectorInitialized) return
        viewModelScope.launch {
            AppDependencies.wifiConnector.disconnect()
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
