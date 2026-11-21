package net.biodiscus.androidcontroltv.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.biodiscus.androidcontroltv.core.repository.FavoriteAppsRepository

class FakeFavoriteAppsRepository(initialFavorites: Set<String> = emptySet()) : FavoriteAppsRepository {

    private val mutableFavoritePackageNames = MutableStateFlow(initialFavorites)
    override val favoritePackageNames: Flow<Set<String>> = mutableFavoritePackageNames

    override suspend fun setFavorite(packageName: String, isFavorite: Boolean) {
        val current = mutableFavoritePackageNames.value
        mutableFavoritePackageNames.value = if (isFavorite) current + packageName else current - packageName
    }
}
