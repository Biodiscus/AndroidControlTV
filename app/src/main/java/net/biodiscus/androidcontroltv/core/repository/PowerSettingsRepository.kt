package net.biodiscus.androidcontroltv.core.repository

import kotlinx.coroutines.flow.Flow
import net.biodiscus.androidcontroltv.core.model.PowerAction

interface PowerSettingsRepository {
    /** The action a tap on the power button runs, or null when a tap sends the plain power key. */
    val defaultPowerAction: Flow<PowerAction?>

    suspend fun setDefaultPowerAction(action: PowerAction?)
}
