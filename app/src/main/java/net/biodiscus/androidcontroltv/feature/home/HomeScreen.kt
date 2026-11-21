package net.biodiscus.androidcontroltv.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

@Composable
internal fun HomeRoute(viewModel: HomeViewModel = hiltViewModel()) {
    RequestLocalNetworkPermission()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onAddressChange = viewModel::onAddressChange,
        onConnectClick = viewModel::onConnectClick,
        onDisconnectClick = viewModel::onDisconnectClick,
        onKeyClick = viewModel::onKeyClick,
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onAddressChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onKeyClick: (RemoteKey) -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
            AddressField(uiState, onAddressChange)
            ConnectionButton(uiState, onConnectClick, onDisconnectClick)
            ConnectionStatus(uiState.connectionState)
            if (uiState.connectionState is TvConnectionState.Connected) {
                RemoteControl(onKeyClick)
            }
        }
    }
}

@Composable
private fun AddressField(uiState: HomeUiState, onAddressChange: (String) -> Unit) {
    val isEditable = uiState.connectionState is TvConnectionState.Disconnected ||
        uiState.connectionState is TvConnectionState.Failed
    val showsError = uiState.addressInput.isNotBlank() && !uiState.isAddressValid

    OutlinedTextField(
        value = uiState.addressInput,
        onValueChange = onAddressChange,
        label = { Text(stringResource(R.string.home_address_label)) },
        singleLine = true,
        enabled = isEditable,
        isError = showsError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ConnectionButton(
    uiState: HomeUiState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    when (uiState.connectionState) {
        is TvConnectionState.Connected -> Button(onClick = onDisconnectClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_disconnect))
        }

        else -> Button(
            onClick = onConnectClick,
            enabled = uiState.isAddressValid && uiState.connectionState !is TvConnectionState.Connecting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_connect))
        }
    }
}

@Composable
private fun ConnectionStatus(connectionState: TvConnectionState) {
    val statusText = when (connectionState) {
        TvConnectionState.Disconnected -> stringResource(R.string.home_status_disconnected)
        TvConnectionState.Connecting -> stringResource(R.string.home_status_connecting)
        is TvConnectionState.Connected -> stringResource(R.string.home_status_connected, connectionState.address.displayText)
        is TvConnectionState.Failed -> stringResource(R.string.home_status_failed, connectionState.reason)
    }
    Text(text = statusText, style = MaterialTheme.typography.bodyMedium)
}

private class HomeUiStatePreviewProvider : PreviewParameterProvider<HomeUiState> {
    override val values = sequenceOf(
        createHomeUiState("", TvConnectionState.Disconnected),
        createHomeUiState("192.168.1.20", TvConnectionState.Connected(TvAddress("192.168.1.20", 5555))),
        createHomeUiState("192.168.1.20", TvConnectionState.Failed("Connection refused")),
    )
}

@ThemePreviews
@Composable
private fun HomeScreenPreview(@PreviewParameter(HomeUiStatePreviewProvider::class) uiState: HomeUiState) {
    AndroidControlTVTheme {
        HomeScreen(
            uiState = uiState,
            onAddressChange = {},
            onConnectClick = {},
            onDisconnectClick = {},
            onKeyClick = {},
        )
    }
}
