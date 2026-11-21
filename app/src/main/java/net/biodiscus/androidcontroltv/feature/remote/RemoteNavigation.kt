package net.biodiscus.androidcontroltv.feature.remote

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
internal data object RemoteDestination

internal fun NavGraphBuilder.remoteScreen(onBackClick: () -> Unit, onDebugClick: () -> Unit) {
    composable<RemoteDestination> {
        RemoteRoute(onBackClick = onBackClick, onDebugClick = onDebugClick)
    }
}
