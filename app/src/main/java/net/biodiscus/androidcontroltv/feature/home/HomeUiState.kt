package net.biodiscus.androidcontroltv.feature.home

import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

internal data class HomeUiState(
    val addressInput: String,
    val isAddressValid: Boolean,
    val connectionState: TvConnectionState,
)

internal fun createHomeUiState(addressInput: String, connectionState: TvConnectionState): HomeUiState =
    HomeUiState(
        addressInput = addressInput,
        isAddressValid = TvAddress.parse(addressInput) != null,
        connectionState = connectionState,
    )
