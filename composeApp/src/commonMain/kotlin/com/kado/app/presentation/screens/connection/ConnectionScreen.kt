package com.kado.app.presentation.screens.connection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.domain.model.ConnectionState
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.localization.S

@Composable
fun ConnectionScreen(
    onBack: () -> Unit,
    vm: ConnectionViewModel = viewModel { ConnectionViewModel() }
) {
    val connectionState by vm.connectionState.collectAsState()
    val ssid by vm.ssid.collectAsState()

    Scaffold(
        topBar = { KadoTopBar(title = S().deviceConnection, onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                S().connectionDescription,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = ssid,
                onValueChange = vm::onSsidChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(S().deviceSsid) },
                placeholder = { Text(S().ssidPlaceholder) },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            when (connectionState) {
                is ConnectionState.Disconnected -> {
                    Button(
                        onClick = vm::connect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(S().connect)
                    }
                }
                is ConnectionState.Connecting -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text(S().connecting)
                }
                is ConnectionState.Connected -> {
                    Text(
                        S().connected,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = vm::disconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(S().disconnect)
                    }
                }
                is ConnectionState.Error -> {
                    Text(
                        (connectionState as ConnectionState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = vm::connect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(S().retry)
                    }
                }
            }
        }
    }
}
