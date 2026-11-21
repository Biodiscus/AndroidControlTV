package net.biodiscus.androidcontroltv.core.repository

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.biodiscus.androidcontroltv.core.adb.TvConnector
import net.biodiscus.androidcontroltv.core.adb.TvShell
import net.biodiscus.androidcontroltv.core.adb.createKeyEventCommand
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

@Singleton
internal class AdbTvRepository @Inject constructor(
    private val connector: TvConnector,
) : TvRepository {

    private val mutableConnectionState = MutableStateFlow<TvConnectionState>(TvConnectionState.Disconnected)
    override val connectionState: StateFlow<TvConnectionState> = mutableConnectionState.asStateFlow()

    private var shell: TvShell? = null

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

    override suspend fun sendKey(key: RemoteKey) {
        val connectedShell = shell ?: return
        try {
            connectedShell.runCommand(createKeyEventCommand(key))
        } catch (error: IOException) {
            closeShell()
            mutableConnectionState.value = TvConnectionState.Failed(describe(error))
        }
    }

    private suspend fun closeShell() {
        val shellToClose = shell ?: return
        shell = null
        shellToClose.close()
    }

    private fun describe(error: IOException): String = error.message ?: error.javaClass.simpleName
}
