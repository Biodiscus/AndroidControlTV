package net.biodiscus.androidcontroltv.core.cache

import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp

internal interface TvAppsCache {
    suspend fun read(address: TvAddress): List<TvApp>?

    suspend fun write(address: TvAddress, apps: List<TvApp>)
}
