package net.biodiscus.androidcontroltv.core.repository

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.biodiscus.androidcontroltv.core.adb.CONNECTION_CHECK_COMMAND
import net.biodiscus.androidcontroltv.core.adb.LoadedTvApps
import net.biodiscus.androidcontroltv.core.adb.TvAppLoader
import net.biodiscus.androidcontroltv.core.adb.TvCommandFailedException
import net.biodiscus.androidcontroltv.core.adb.TvConnector
import net.biodiscus.androidcontroltv.core.adb.TvShell
import net.biodiscus.androidcontroltv.core.adb.createKeyEventCommand
import net.biodiscus.androidcontroltv.core.adb.createLaunchAppCommand
import net.biodiscus.androidcontroltv.core.adb.createPowerActionCommand
import net.biodiscus.androidcontroltv.core.cache.TvAppsCache
import net.biodiscus.androidcontroltv.core.debug.DebugAction
import net.biodiscus.androidcontroltv.core.debug.DebugActionRunner
import net.biodiscus.androidcontroltv.core.debug.DebugReport
import net.biodiscus.androidcontroltv.core.debug.createNotConnectedReport
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

@Singleton
internal class AdbTvRepository @Inject constructor(
    private val connector: TvConnector,
    private val appLoader: TvAppLoader,
    private val appsCache: TvAppsCache,
) : TvRepository {

    private val mutableConnectionState = MutableStateFlow<TvConnectionState>(TvConnectionState.Disconnected)
    override val connectionState: StateFlow<TvConnectionState> = mutableConnectionState.asStateFlow()

    private var shell: TvShell? = null
    private val reconnectMutex = Mutex()

    override suspend fun connect(address: TvAddress) {
        disconnect()
        mutableConnectionState.value = TvConnectionState.Connecting
        try {
            shell = connector.connect(address)
            mutableConnectionState.value = TvConnectionState.Connected(address)
        } catch (error: IOException) {
            mutableConnectionState.value = TvConnectionState.Failed(describe(error))
        }
    }

    override suspend fun disconnect() {
        closeShell()
        mutableConnectionState.value = TvConnectionState.Disconnected
    }

    override suspend fun sendKey(key: RemoteKey): Boolean = runCommandOrFail(createKeyEventCommand(key))

    override suspend fun sendPowerAction(action: PowerAction): Boolean {
        val command = createPowerActionCommand(action)
        if (!action.endsConnection) return runCommandOrFail(command)
        return confirmConnectionWorks() && runCommandThatEndsTheConnection(command)
    }

    override fun loadApps(): Flow<TvAppsState> = flow {
        val address = (connectionState.value as? TvConnectionState.Connected)?.address
        if (address == null) {
            emit(TvAppsState.Unavailable)
            return@flow
        }

        val cachedApps = appsCache.read(address)
        if (cachedApps != null) emit(TvAppsState.Loaded(cachedApps))

        val refreshedState = refreshApps(address, cachedApps)
        if (refreshedState != null) emit(refreshedState)
    }

    /** The state to show after asking the TV, or null when what is already shown should stay. */
    private suspend fun refreshApps(address: TvAddress, cachedApps: List<TvApp>?): TvAppsState? {
        val freshApps = loadFreshApps()
        if (freshApps == null) return if (cachedApps == null) TvAppsState.Unavailable else null

        if (freshApps.hasNamesAndIcons) {
            appsCache.write(address, freshApps.apps)
            return TvAppsState.Loaded(freshApps.apps)
        }
        // A list without real names and icons must never replace a cached list that has them.
        return if (cachedApps == null) TvAppsState.Loaded(freshApps.apps) else null
    }

    private suspend fun loadFreshApps(): LoadedTvApps? {
        val connectedShell = shell ?: return null
        return try {
            appLoader.load(connectedShell)
        } catch (_: TvCommandFailedException) {
            // Not being able to list the apps must not take the remote control away.
            null
        } catch (_: IOException) {
            loadFreshAppsAfterReconnecting(brokenShell = connectedShell)
        }
    }

    private suspend fun loadFreshAppsAfterReconnecting(brokenShell: TvShell): LoadedTvApps? {
        val newShell = replaceBrokenShell(brokenShell) ?: return null
        return try {
            appLoader.load(newShell)
        } catch (_: TvCommandFailedException) {
            null
        } catch (error: IOException) {
            failWith(error)
            null
        }
    }

    override suspend fun launchApp(app: TvApp): Boolean = runCommandOrFail(createLaunchAppCommand(app))

    override suspend fun runDebugAction(action: DebugAction): DebugReport {
        val connectedShell = shell ?: return createNotConnectedReport(action)
        return DebugActionRunner(connectedShell).run(action)
    }

    // A broken connection looks the same as a TV that shuts down. Without this check a reboot sent over a dead
    // connection would be reported as sent, and the command would never reach the TV.
    private suspend fun confirmConnectionWorks(): Boolean = runCommandOrFail(CONNECTION_CHECK_COMMAND)

    private suspend fun runCommandOrFail(command: String): Boolean {
        val connectedShell = shell ?: return false
        return try {
            connectedShell.runCommand(command)
            true
        } catch (error: TvCommandFailedException) {
            failWith(error)
            false
        } catch (_: IOException) {
            runCommandAfterReconnecting(command, brokenShell = connectedShell)
        }
    }

    private suspend fun runCommandAfterReconnecting(command: String, brokenShell: TvShell): Boolean {
        val newShell = replaceBrokenShell(brokenShell) ?: return false
        return try {
            newShell.runCommand(command)
            true
        } catch (error: IOException) {
            failWith(error)
            false
        }
    }

    /**
     * Opens a new connection to the TV, unless another command already did. Returns the shell to use,
     * or null when reconnecting failed (the connection is then marked as failed) or the user disconnected meanwhile.
     */
    private suspend fun replaceBrokenShell(brokenShell: TvShell): TvShell? =
        reconnectMutex.withLock {
            if (shell === brokenShell) openNewShell() else shell
        }

    private suspend fun openNewShell(): TvShell? {
        val address = (connectionState.value as? TvConnectionState.Connected)?.address ?: return null
        closeShell()
        return try {
            val newShell = connector.connect(address)
            shell = newShell
            newShell
        } catch (error: IOException) {
            failWith(error)
            null
        }
    }

    private suspend fun runCommandThatEndsTheConnection(command: String): Boolean {
        val connectedShell = shell ?: return false
        try {
            connectedShell.runCommand(command)
        } catch (error: TvCommandFailedException) {
            failWith(error)
            return false
        } catch (_: IOException) {
            // The TV drops the connection while it shuts down, so a broken connection is the expected result.
        }
        disconnect()
        return true
    }

    private suspend fun failWith(error: IOException) {
        closeShell()
        mutableConnectionState.value = TvConnectionState.Failed(describe(error))
    }

    private suspend fun closeShell() {
        val shellToClose = shell ?: return
        shell = null
        shellToClose.close()
    }

    private fun describe(error: IOException): String = error.message ?: error.javaClass.simpleName
}
