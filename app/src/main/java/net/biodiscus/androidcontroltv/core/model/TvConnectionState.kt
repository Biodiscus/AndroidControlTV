package net.biodiscus.androidcontroltv.core.model

sealed interface TvConnectionState {
    data object Disconnected : TvConnectionState
    data object Connecting : TvConnectionState
    data class Connected(val address: TvAddress) : TvConnectionState
    data class Failed(val reason: String) : TvConnectionState
}
