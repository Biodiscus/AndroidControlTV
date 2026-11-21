package net.biodiscus.androidcontroltv.feature.overview

import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

internal data class OverviewUiState(
    val addressInput: String,
    val isAddressValid: Boolean,
    val connectionState: TvConnectionState,
    val savedAddresses: List<TvAddress>,
)

internal fun createOverviewUiState(
    addressInput: String,
    connectionState: TvConnectionState,
    savedAddresses: List<TvAddress>,
): OverviewUiState =
    OverviewUiState(
        addressInput = addressInput,
        isAddressValid = TvAddress.parse(addressInput) != null,
        connectionState = connectionState,
        savedAddresses = savedAddresses,
    )
