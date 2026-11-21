package net.biodiscus.androidcontroltv.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

@Composable
internal fun ConnectionStatus(connectionState: TvConnectionState, modifier: Modifier = Modifier) {
    val statusText = when (connectionState) {
        TvConnectionState.Disconnected -> stringResource(R.string.connection_status_disconnected)
        TvConnectionState.Connecting -> stringResource(R.string.connection_status_connecting)
        is TvConnectionState.Connected -> stringResource(R.string.connection_status_connected, connectionState.address.displayText)
        is TvConnectionState.Failed -> stringResource(R.string.connection_status_failed, connectionState.reason)
    }
    Text(text = statusText, style = MaterialTheme.typography.bodyMedium, modifier = modifier)
}
