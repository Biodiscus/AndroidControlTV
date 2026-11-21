package net.biodiscus.androidcontroltv.core.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import net.biodiscus.androidcontroltv.core.debug.DebugAction
import net.biodiscus.androidcontroltv.core.debug.DebugReport
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

interface TvRepository {
    val connectionState: StateFlow<TvConnectionState>

    suspend fun connect(address: TvAddress)
    suspend fun disconnect()
    /** Each of these returns true when the TV accepted the command. */
    suspend fun sendKey(key: RemoteKey): Boolean
    suspend fun sendPowerAction(action: PowerAction): Boolean
    /** Emits the cached apps first when there are any, then the apps as the TV reports them now. */
    fun loadApps(): Flow<TvAppsState>
    suspend fun launchApp(app: TvApp): Boolean

    /** Runs the steps on the connected TV without changing the connection state, whatever the steps do. */
    suspend fun runDebugAction(action: DebugAction): DebugReport
}
