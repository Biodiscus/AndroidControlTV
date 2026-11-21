package net.biodiscus.androidcontroltv.feature.remote

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.ui.ConnectionStatus
import net.biodiscus.androidcontroltv.ui.RequestLocalNetworkPermission
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

private val AppTileMinWidth = 96.dp

@Composable
internal fun RemoteRoute(
    onBackClick: () -> Unit,
    onDebugClick: () -> Unit,
    viewModel: RemoteViewModel = hiltViewModel(),
) {
    RequestLocalNetworkPermission()
    ShowMessagesAsToasts(viewModel.messages)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val goBackToOverview = { viewModel.onBackClick(onDisconnected = onBackClick) }

    BackHandler(onBack = goBackToOverview)
    RemoteScreen(
        uiState = uiState,
        onBackClick = goBackToOverview,
        onDebugClick = onDebugClick,
        onRetryClick = viewModel::onRetryClick,
        onKeyClick = viewModel::onKeyClick,
        onPowerActionClick = viewModel::onPowerActionClick,
        onDefaultPowerActionChange = viewModel::onDefaultPowerActionChange,
        onAppClick = viewModel::onAppClick,
        onAppFavoriteChange = viewModel::onAppFavoriteChange,
    )
}

@Composable
private fun ShowMessagesAsToasts(messages: Flow<RemoteMessage>) {
    val context = LocalContext.current

    LaunchedEffect(messages) {
        var visibleToast: Toast? = null
        messages.collect { message ->
            // Pressing keys quickly must replace the toast and not queue up one toast per press.
            visibleToast?.cancel()
            val newToast = Toast.makeText(context, createMessageText(context, message), Toast.LENGTH_SHORT)
            newToast.show()
            visibleToast = newToast
        }
    }
}

@Composable
internal fun RemoteScreen(
    uiState: RemoteUiState,
    onBackClick: () -> Unit,
    onDebugClick: () -> Unit,
    onRetryClick: () -> Unit,
    onKeyClick: (RemoteKey) -> Unit,
    onPowerActionClick: (PowerAction) -> Unit,
    onDefaultPowerActionChange: (PowerAction, Boolean) -> Unit,
    onAppClick: (TvApp) -> Unit,
    onAppFavoriteChange: (TvApp, Boolean) -> Unit,
) {
    var isPowerMenuVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { RemoteTopBar(onBackClick, onDebugClick) }) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)

        if (uiState.connectionState is TvConnectionState.Connected) {
            ConnectedContent(
                uiState = uiState,
                onKeyClick = onKeyClick,
                onPowerLongClick = { isPowerMenuVisible = true },
                onAppClick = onAppClick,
                onAppFavoriteChange = onAppFavoriteChange,
                modifier = contentModifier,
            )
        } else {
            ConnectionProblem(uiState.connectionState, onRetryClick, contentModifier)
        }
    }

    if (isPowerMenuVisible) {
        PowerMenuDialog(
            defaultAction = uiState.defaultPowerAction,
            onActionClick = { action ->
                isPowerMenuVisible = false
                onPowerActionClick(action)
            },
            onDefaultChange = onDefaultPowerActionChange,
            onDismiss = { isPowerMenuVisible = false },
        )
    }
}

@Composable
private fun ConnectedContent(
    uiState: RemoteUiState,
    onKeyClick: (RemoteKey) -> Unit,
    onPowerLongClick: () -> Unit,
    onAppClick: (TvApp) -> Unit,
    onAppFavoriteChange: (TvApp, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = AppTileMinWidth),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            RemoteControl(
                onKeyClick = onKeyClick,
                onPowerLongClick = onPowerLongClick,
                defaultPowerAction = uiState.defaultPowerAction,
            )
        }
        tvAppsSection(
            tvApps = uiState.tvApps,
            favoritePackageNames = uiState.favoritePackageNames,
            onAppClick = onAppClick,
            onAppFavoriteChange = onAppFavoriteChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoteTopBar(onBackClick: () -> Unit, onDebugClick: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.remote_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.remote_back_to_overview),
                )
            }
        },
        actions = {
            IconButton(onClick = onDebugClick) {
                Icon(
                    imageVector = Icons.Rounded.BugReport,
                    contentDescription = stringResource(R.string.remote_open_debug_tools),
                )
            }
        },
    )
}

@Composable
private fun ConnectionProblem(
    connectionState: TvConnectionState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ConnectionStatus(connectionState)
        if (connectionState is TvConnectionState.Connecting) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onRetryClick) { Text(stringResource(R.string.remote_retry)) }
        }
    }
}

private val previewApps = listOf(
    TvApp("com.google.android.youtube.tv", ".MainActivity", "YouTube"),
    TvApp("com.netflix.ninja", ".MainActivity", "Netflix"),
    TvApp("com.example.tv", ".MainActivity", "An app with a very long name"),
)

private class RemoteUiStatePreviewProvider : PreviewParameterProvider<RemoteUiState> {
    override val values = sequenceOf(
        RemoteUiState(
            connectionState = TvConnectionState.Connected(TvAddress("192.168.1.20", 5555)),
            tvApps = TvAppsState.Loaded(previewApps),
            favoritePackageNames = setOf("com.netflix.ninja"),
        ),
        RemoteUiState(TvConnectionState.Connecting),
        RemoteUiState(TvConnectionState.Failed("Connection refused")),
    )
}

@ThemePreviews
@Composable
private fun RemoteScreenPreview(@PreviewParameter(RemoteUiStatePreviewProvider::class) uiState: RemoteUiState) {
    AndroidControlTVTheme {
        RemoteScreen(
            uiState = uiState,
            onBackClick = {},
            onDebugClick = {},
            onRetryClick = {},
            onKeyClick = {},
            onPowerActionClick = {},
            onDefaultPowerActionChange = { _, _ -> },
            onAppClick = {},
            onAppFavoriteChange = { _, _ -> },
        )
    }
}
