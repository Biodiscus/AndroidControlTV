package net.biodiscus.androidcontroltv.testing

import net.biodiscus.androidcontroltv.core.cache.TvAppsCache
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp

internal class FakeTvAppsCache : TvAppsCache {

    val cachedApps = mutableMapOf<TvAddress, List<TvApp>>()

    override suspend fun read(address: TvAddress): List<TvApp>? = cachedApps[address]

    override suspend fun write(address: TvAddress, apps: List<TvApp>) {
        cachedApps[address] = apps
    }
}
