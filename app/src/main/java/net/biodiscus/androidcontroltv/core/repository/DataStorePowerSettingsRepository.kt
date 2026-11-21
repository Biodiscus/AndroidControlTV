package net.biodiscus.androidcontroltv.core.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.biodiscus.androidcontroltv.core.di.SettingsDataStore
import net.biodiscus.androidcontroltv.core.model.PowerAction

internal class DataStorePowerSettingsRepository @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
) : PowerSettingsRepository {

    override val defaultPowerAction: Flow<PowerAction?> = dataStore.data.map { preferences ->
        val actionName = preferences[DEFAULT_POWER_ACTION_KEY]
        PowerAction.entries.firstOrNull { it.name == actionName }
    }

    override suspend fun setDefaultPowerAction(action: PowerAction?) {
        dataStore.edit { preferences ->
            if (action == null) {
                preferences.remove(DEFAULT_POWER_ACTION_KEY)
            } else {
                preferences[DEFAULT_POWER_ACTION_KEY] = action.name
            }
        }
    }

    private companion object {
        val DEFAULT_POWER_ACTION_KEY = stringPreferencesKey("default_power_action")
    }
}
