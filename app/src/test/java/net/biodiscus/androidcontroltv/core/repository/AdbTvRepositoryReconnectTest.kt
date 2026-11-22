package net.biodiscus.androidcontroltv.core.repository

import java.io.IOException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.adb.CONNECTION_CHECK_COMMAND
import net.biodiscus.androidcontroltv.core.adb.TvCommandFailedException
import net.biodiscus.androidcontroltv.core.adb.createKeyEventCommand
import net.biodiscus.androidcontroltv.core.adb.createUninstallAppCommand
import net.biodiscus.androidcontroltv.core.model.AppUninstallResult
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.testing.FakeTvAppLoader
import net.biodiscus.androidcontroltv.testing.FakeTvAppsCache
import net.biodiscus.androidcontroltv.testing.FakeTvConnector
import net.biodiscus.androidcontroltv.testing.FakeTvShell
import net.biodiscus.androidcontroltv.testing.createTvApp
import net.biodiscus.androidcontroltv.testing.createTvSystemApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbTvRepositoryReconnectTest {

    private val tvAddress = TvAddress("192.168.1.20", 5555)
    private val firstShell = FakeTvShell()
    private val reconnectedShell = FakeTvShell()
    private val connector = FakeTvConnector(firstShell, reconnectedShell)
    private val appLoader = FakeTvAppLoader()
    private val repository = AdbTvRepository(connector, appLoader, FakeTvAppsCache())

    private val volumeUpCommand = createKeyEventCommand(RemoteKey.VOLUME_UP)

    private suspend fun connectAndBreakTheConnection() {
        repository.connect(tvAddress)
        firstShell.errorOnRunCommand = IOException("Broken pipe")
    }

    @Test
    fun `a command that hits a broken connection is sent again on a new connection`() = runTest {
        connectAndBreakTheConnection()

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(listOf(volumeUpCommand), reconnectedShell.receivedCommands)
    }

    @Test
    fun `a command that is sent again after reconnecting reports success`() = runTest {
        connectAndBreakTheConnection()

        assertTrue(repository.sendKey(RemoteKey.VOLUME_UP))
    }

    @Test
    fun `reconnecting leaves the connection state connected`() = runTest {
        connectAndBreakTheConnection()

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `reconnecting connects to the same TV again`() = runTest {
        connectAndBreakTheConnection()

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(listOf(tvAddress, tvAddress), connector.connectedAddresses)
    }

    @Test
    fun `the broken connection is closed when reconnecting`() = runTest {
        connectAndBreakTheConnection()

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertTrue(firstShell.isClosed)
    }

    @Test
    fun `later commands use the new connection without reconnecting again`() = runTest {
        connectAndBreakTheConnection()
        repository.sendKey(RemoteKey.VOLUME_UP)

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(2, connector.connectedAddresses.size)
        assertEquals(listOf(volumeUpCommand, volumeUpCommand), reconnectedShell.receivedCommands)
    }

    @Test
    fun `a TV that cannot be reached again marks the connection as failed`() = runTest {
        connectAndBreakTheConnection()
        connector.errorOnConnect = IOException("Connection refused")

        val wasSent = repository.sendKey(RemoteKey.VOLUME_UP)

        assertFalse(wasSent)
        assertEquals(TvConnectionState.Failed("Connection refused"), repository.connectionState.value)
    }

    @Test
    fun `a command that fails again on the new connection marks the connection as failed`() = runTest {
        connectAndBreakTheConnection()
        reconnectedShell.errorOnRunCommand = IOException("Connection reset")

        val wasSent = repository.sendKey(RemoteKey.VOLUME_UP)

        assertFalse(wasSent)
        assertEquals(TvConnectionState.Failed("Connection reset"), repository.connectionState.value)
    }

    @Test
    fun `a command that the TV refuses does not cause a reconnect`() = runTest {
        repository.connect(tvAddress)
        firstShell.errorOnRunCommand = TvCommandFailedException("TV command failed (exit code 1): not allowed")

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(1, connector.connectedAddresses.size)
    }

    @Test
    fun `nothing is reconnected while the user is disconnected`() = runTest {
        repository.sendKey(RemoteKey.VOLUME_UP)

        assertTrue(connector.connectedAddresses.isEmpty())
    }

    @Test
    fun `launching an app also reconnects`() = runTest {
        connectAndBreakTheConnection()

        assertTrue(repository.launchApp(createTvApp()))
    }

    @Test
    fun `an uninstall that hits a broken connection is sent again on a new connection`() = runTest {
        val app = createTvApp()
        connectAndBreakTheConnection()

        val result = repository.uninstallApp(app)

        assertEquals(AppUninstallResult.UNINSTALLED, result)
        assertEquals(listOf(createUninstallAppCommand(app)), reconnectedShell.receivedCommands)
    }

    @Test
    fun `an uninstall that cannot reconnect reports that nothing was sent`() = runTest {
        connectAndBreakTheConnection()
        connector.errorOnConnect = IOException("No route to host")

        assertEquals(AppUninstallResult.NOT_SENT, repository.uninstallApp(createTvApp()))
    }

    @Test
    fun `opening the info of a system app also reconnects`() = runTest {
        connectAndBreakTheConnection()

        assertTrue(repository.openSystemAppInfo(createTvSystemApp()))
    }

    @Test
    fun `a reboot checks the connection first and reconnects when it is broken`() = runTest {
        connectAndBreakTheConnection()

        repository.sendPowerAction(PowerAction.REBOOT)

        assertEquals(listOf(CONNECTION_CHECK_COMMAND, "reboot"), reconnectedShell.receivedCommands)
    }

    @Test
    fun `a reboot over a broken connection is reported as sent once it reached the TV`() = runTest {
        connectAndBreakTheConnection()

        assertTrue(repository.sendPowerAction(PowerAction.REBOOT))
        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `a reboot is not reported as sent when the TV cannot be reached again`() = runTest {
        connectAndBreakTheConnection()
        connector.errorOnConnect = IOException("Connection refused")

        assertFalse(repository.sendPowerAction(PowerAction.REBOOT))
        assertEquals(TvConnectionState.Failed("Connection refused"), repository.connectionState.value)
    }

    @Test
    fun `a sleep action does not need the extra connection check`() = runTest {
        repository.connect(tvAddress)

        repository.sendPowerAction(PowerAction.SLEEP)

        assertEquals(listOf("input keyevent KEYCODE_SLEEP"), firstShell.receivedCommands)
    }

    @Test
    fun `loading the apps reconnects when the connection is broken`() = runTest {
        val apps = listOf(createTvApp())
        appLoader.apps = apps
        appLoader.errorOnFirstLoad = IOException("Broken pipe")
        repository.connect(tvAddress)

        val states = repository.loadApps().toList()

        assertEquals(listOf(TvAppsState.Loaded(apps)), states)
        assertEquals(listOf(firstShell, reconnectedShell), appLoader.loadedWithShells)
        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `loading the apps marks the connection as failed when reconnecting fails`() = runTest {
        appLoader.errorOnFirstLoad = IOException("Broken pipe")
        repository.connect(tvAddress)
        connector.errorOnConnect = IOException("Connection refused")

        val states = repository.loadApps().toList()

        assertEquals(listOf(TvAppsState.Unavailable), states)
        assertEquals(TvConnectionState.Failed("Connection refused"), repository.connectionState.value)
    }
}
