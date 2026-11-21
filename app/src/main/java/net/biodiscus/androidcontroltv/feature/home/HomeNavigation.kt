package net.biodiscus.androidcontroltv.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
internal data object HomeDestination

internal fun NavGraphBuilder.homeScreen() {
    composable<HomeDestination> {
        HomeRoute()
    }
}
