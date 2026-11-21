package net.biodiscus.androidcontroltv.feature.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.ui.ConnectionStatus
import net.biodiscus.androidcontroltv.ui.RequestLocalNetworkPermission
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

@Composable
internal fun OverviewRoute(
    onConnected: () -> Unit,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    RequestLocalNetworkPermission()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OverviewScreen(
        uiState = uiState,
        onAddressChange = viewModel::onAddressChange,
        onConnectClick = { viewModel.onConnectClick(onConnected) },
        onSavedTvClick = { address -> viewModel.onSavedTvClick(address, onConnected) },
    )
}

@Composable
internal fun OverviewScreen(
    uiState: OverviewUiState,
    onAddressChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onSavedTvClick: (TvAddress) -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = stringResource(R.string.overview_title), style = MaterialTheme.typography.headlineMedium)
            AddressField(uiState, onAddressChange)
            ConnectButton(uiState, onConnectClick)
            ConnectionStatus(uiState.connectionState)
            if (uiState.savedAddresses.isNotEmpty()) {
                SavedTvList(uiState, onSavedTvClick)
            }
        }
    }
}

@Composable
private fun AddressField(uiState: OverviewUiState, onAddressChange: (String) -> Unit) {
    val isEditable = uiState.connectionState !is TvConnectionState.Connecting
    val showsError = uiState.addressInput.isNotBlank() && !uiState.isAddressValid

    OutlinedTextField(
        value = uiState.addressInput,
        onValueChange = onAddressChange,
        label = { Text(stringResource(R.string.overview_address_label)) },
        singleLine = true,
        enabled = isEditable,
        isError = showsError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ConnectButton(uiState: OverviewUiState, onConnectClick: () -> Unit) {
    Button(
        onClick = onConnectClick,
        enabled = uiState.isAddressValid && uiState.connectionState !is TvConnectionState.Connecting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.overview_connect))
    }
}

@Composable
private fun SavedTvList(uiState: OverviewUiState, onSavedTvClick: (TvAddress) -> Unit) {
    val isConnecting = uiState.connectionState is TvConnectionState.Connecting

    Text(text = stringResource(R.string.overview_saved_tvs_title), style = MaterialTheme.typography.titleMedium)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(uiState.savedAddresses) { address ->
            SavedTvCard(address = address, enabled = !isConnecting, onClick = { onSavedTvClick(address) })
        }
    }
}

@Composable
private fun SavedTvCard(address: TvAddress, enabled: Boolean, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Rounded.Tv, contentDescription = null)
            Text(text = address.displayText, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private val previewSavedAddresses = listOf(TvAddress("192.168.1.20", 5555), TvAddress("192.168.1.30", 5555))

private class OverviewUiStatePreviewProvider : PreviewParameterProvider<OverviewUiState> {
    override val values = sequenceOf(
        createOverviewUiState("", TvConnectionState.Disconnected, emptyList()),
        createOverviewUiState("", TvConnectionState.Disconnected, previewSavedAddresses),
        createOverviewUiState("192.168.1.20", TvConnectionState.Connecting, previewSavedAddresses),
        createOverviewUiState("192.168.1.20", TvConnectionState.Failed("Connection refused"), previewSavedAddresses),
    )
}

@ThemePreviews
@Composable
private fun OverviewScreenPreview(@PreviewParameter(OverviewUiStatePreviewProvider::class) uiState: OverviewUiState) {
    AndroidControlTVTheme {
        OverviewScreen(
            uiState = uiState,
            onAddressChange = {},
            onConnectClick = {},
            onSavedTvClick = {},
        )
    }
}
