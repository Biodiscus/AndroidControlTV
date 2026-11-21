package net.biodiscus.androidcontroltv.core.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.biodiscus.androidcontroltv.core.di.SettingsDataStore

internal class DataStoreFavoriteAppsRepository @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
) : FavoriteAppsRepository {

    override val favoritePackageNames: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[FAVORITES_KEY] ?: emptySet()
    }

    override suspend fun setFavorite(packageName: String, isFavorite: Boolean) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            preferences[FAVORITES_KEY] = if (isFavorite) currentFavorites + packageName else currentFavorites - packageName
        }
    }

    private companion object {
        val FAVORITES_KEY = stringSetPreferencesKey("favorite_app_package_names")
    }
}
