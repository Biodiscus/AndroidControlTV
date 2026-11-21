package net.biodiscus.androidcontroltv.feature.overview

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
internal data object OverviewDestination

internal fun NavGraphBuilder.overviewScreen(onConnected: () -> Unit) {
    composable<OverviewDestination> {
        OverviewRoute(onConnected = onConnected)
    }
}
