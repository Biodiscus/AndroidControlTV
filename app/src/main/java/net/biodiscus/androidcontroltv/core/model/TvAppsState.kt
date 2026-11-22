package net.biodiscus.androidcontroltv.core.model

sealed interface TvAppsState {
    data object Loading : TvAppsState
    data class Loaded(val apps: List<TvApp>, val systemApps: List<TvSystemApp> = emptyList()) : TvAppsState
    data object Unavailable : TvAppsState
}

fun TvAppsState.withoutApp(app: TvApp): TvAppsState =
    if (this is TvAppsState.Loaded) copy(apps = apps.filterNot { it.packageName == app.packageName }) else this
