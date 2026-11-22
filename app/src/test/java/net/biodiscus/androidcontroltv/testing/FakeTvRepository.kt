package net.biodiscus.androidcontroltv.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import net.biodiscus.androidcontroltv.core.debug.DebugAction
import net.biodiscus.androidcontroltv.core.debug.DebugReport
import net.biodiscus.androidcontroltv.core.model.AppUninstallResult
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvSystemApp
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.core.repository.TvRepository

class FakeTvRepository : TvRepository {

    private val mutableConnectionState = MutableStateFlow<TvConnectionState>(TvConnectionState.Disconnected)
    override val connectionState: StateFlow<TvConnectionState> = mutableConnectionState

    var connectFailureReason: String? = null
    var commandsSucceed = true
    var debugReport = DebugReport("Fake action", emptyList())
    val ranDebugActions = mutableListOf<DebugAction>()
    var appsStates: List<TvAppsState> = listOf(TvAppsState.Loaded(emptyList()))
    val connectedAddresses = mutableListOf<TvAddress>()
    val sentKeys = mutableListOf<RemoteKey>()
    val sentPowerActions = mutableListOf<PowerAction>()
    val sentTexts = mutableListOf<String>()
    val launchedApps = mutableListOf<TvApp>()
    val uninstalledApps = mutableListOf<TvApp>()
    val openedSystemApps = mutableListOf<TvSystemApp>()
    var uninstallResult = AppUninstallResult.UNINSTALLED
    var disconnectCount = 0
        private set

    fun setConnectionState(state: TvConnectionState) {
        mutableConnectionState.value = state
    }

    override suspend fun connect(address: TvAddress) {
        connectedAddresses += address
        val failureReason = connectFailureReason
        mutableConnectionState.value =
            if (failureReason == null) TvConnectionState.Connected(address) else TvConnectionState.Failed(failureReason)
    }

    override suspend fun disconnect() {
        disconnectCount++
        mutableConnectionState.value = TvConnectionState.Disconnected
    }

    override suspend fun sendKey(key: RemoteKey): Boolean {
        sentKeys += key
        return commandsSucceed
    }

    override suspend fun sendPowerAction(action: PowerAction): Boolean {
        sentPowerActions += action
        return commandsSucceed
    }

    override suspend fun sendText(text: String): Boolean {
        sentTexts += text
        return commandsSucceed
    }

    override fun loadApps(): Flow<TvAppsState> = appsStates.asFlow()

    override suspend fun launchApp(app: TvApp): Boolean {
        launchedApps += app
        return commandsSucceed
    }

    override suspend fun openSystemAppInfo(app: TvSystemApp): Boolean {
        openedSystemApps += app
        return commandsSucceed
    }

    override suspend fun uninstallApp(app: TvApp): AppUninstallResult {
        uninstalledApps += app
        return uninstallResult
    }

    override suspend fun runDebugAction(action: DebugAction): DebugReport {
        ranDebugActions += action
        return debugReport
    }
}
