package net.biodiscus.androidcontroltv.feature.debug

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
internal data object DebugDestination

internal fun NavGraphBuilder.debugScreen(onBackClick: () -> Unit) {
    composable<DebugDestination> {
        DebugRoute(onBackClick = onBackClick)
    }
}
