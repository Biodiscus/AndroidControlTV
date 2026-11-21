package net.biodiscus.androidcontroltv.feature.remote

import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

internal data class RemoteUiState(
    val connectionState: TvConnectionState,
    val defaultPowerAction: PowerAction? = null,
    val tvApps: TvAppsState = TvAppsState.Loading,
    val favoritePackageNames: Set<String> = emptySet(),
)
