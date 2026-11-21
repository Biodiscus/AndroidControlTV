package net.biodiscus.androidcontroltv.core.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.biodiscus.androidcontroltv.core.di.SavedTvDataStore
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.addAsMostRecent
import net.biodiscus.androidcontroltv.core.model.decodeSavedAddresses
import net.biodiscus.androidcontroltv.core.model.encodeSavedAddresses

internal class DataStoreSavedTvRepository @Inject constructor(
    @param:SavedTvDataStore private val dataStore: DataStore<Preferences>,
) : SavedTvRepository {

    override val savedAddresses: Flow<List<TvAddress>> = dataStore.data.map(::readAddresses)

    override suspend fun saveAddress(address: TvAddress) {
        dataStore.edit { preferences ->
            val updatedAddresses = addAsMostRecent(readAddresses(preferences), address)
            preferences[ADDRESSES_KEY] = encodeSavedAddresses(updatedAddresses)
        }
    }

    private fun readAddresses(preferences: Preferences): List<TvAddress> {
        val text = preferences[ADDRESSES_KEY] ?: return emptyList()
        return decodeSavedAddresses(text)
    }

    private companion object {
        val ADDRESSES_KEY = stringPreferencesKey("saved_tv_addresses")
    }
}
