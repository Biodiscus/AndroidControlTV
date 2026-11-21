package net.biodiscus.androidcontroltv.core.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteAppsRepository {
    val favoritePackageNames: Flow<Set<String>>

    suspend fun setFavorite(packageName: String, isFavorite: Boolean)
}
