package net.biodiscus.androidcontroltv.core.model

sealed interface TvAppsState {
    data object Loading : TvAppsState
    data class Loaded(val apps: List<TvApp>) : TvAppsState
    data object Unavailable : TvAppsState
}
