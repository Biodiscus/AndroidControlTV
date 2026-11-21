package net.biodiscus.androidcontroltv.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.repository.PowerSettingsRepository

class FakePowerSettingsRepository(initialAction: PowerAction? = null) : PowerSettingsRepository {

    private val mutableDefaultPowerAction = MutableStateFlow(initialAction)
    override val defaultPowerAction: Flow<PowerAction?> = mutableDefaultPowerAction

    override suspend fun setDefaultPowerAction(action: PowerAction?) {
        mutableDefaultPowerAction.value = action
    }
}
