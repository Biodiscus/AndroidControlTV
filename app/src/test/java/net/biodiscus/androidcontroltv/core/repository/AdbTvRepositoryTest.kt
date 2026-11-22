package net.biodiscus.androidcontroltv.core.repository

import java.io.IOException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.adb.TvCommandFailedException
import net.biodiscus.androidcontroltv.core.adb.createLaunchAppCommand
import net.biodiscus.androidcontroltv.core.adb.createOpenAppInfoCommand
import net.biodiscus.androidcontroltv.core.adb.createUninstallAppCommand
import net.biodiscus.androidcontroltv.core.debug.DebugAction
import net.biodiscus.androidcontroltv.core.debug.DebugStep
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

class AdbTvRepositoryTest {

    private val tvAddress = TvAddress("192.168.1.20", 5555)
    private val shell = FakeTvShell()
    private val connector = FakeTvConnector(shell)
    private val appLoader = FakeTvAppLoader()
    private val appsCache = FakeTvAppsCache()
    private val repository = AdbTvRepository(connector, appLoader, appsCache)

    @Test
    fun `starts disconnected`() {
        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `connecting to a reachable TV ends up connected`() = runTest {
        repository.connect(tvAddress)

        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `connecting asks the connector for the given address`() = runTest {
        repository.connect(tvAddress)

        assertEquals(listOf(tvAddress), connector.connectedAddresses)
    }

    @Test
    fun `connecting to an unreachable TV fails with the error message`() = runTest {
        connector.errorOnConnect = IOException("Connection refused")

        repository.connect(tvAddress)

        assertEquals(TvConnectionState.Failed("Connection refused"), repository.connectionState.value)
    }

    @Test
    fun `a failure without a message is described by the error name`() = runTest {
        connector.errorOnConnect = IOException()

        repository.connect(tvAddress)

        assertEquals(TvConnectionState.Failed("IOException"), repository.connectionState.value)
    }

    @Test
    fun `sending a key runs the key event command on the TV`() = runTest {
        repository.connect(tvAddress)

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(listOf("input keyevent KEYCODE_VOLUME_UP"), shell.receivedCommands)
    }

    @Test
    fun `sending text runs the text input command on the TV`() = runTest {
        repository.connect(tvAddress)

        repository.sendText("star wars")

        assertEquals(listOf("input text 'star%swars'"), shell.receivedCommands)
    }

    @Test
    fun `sending text while disconnected does nothing`() = runTest {
        val wasSent = repository.sendText("star wars")

        assertFalse(wasSent)
        assertTrue(shell.receivedCommands.isEmpty())
    }

    @Test
    fun `sending a key while disconnected does nothing`() = runTest {
        repository.sendKey(RemoteKey.VOLUME_UP)

        assertTrue(shell.receivedCommands.isEmpty())
        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `a failing key press marks the connection as failed`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(TvConnectionState.Failed("Broken pipe"), repository.connectionState.value)
    }

    @Test
    fun `a failing key press closes the connection`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertTrue(shell.isClosed)
    }

    @Test
    fun `sleeping runs the sleep command on the TV`() = runTest {
        repository.connect(tvAddress)

        repository.sendPowerAction(PowerAction.SLEEP)

        assertEquals(listOf("input keyevent KEYCODE_SLEEP"), shell.receivedCommands)
    }

    @Test
    fun `sleeping keeps the connection`() = runTest {
        repository.connect(tvAddress)

        repository.sendPowerAction(PowerAction.SLEEP)

        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `a failing sleep marks the connection as failed`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        repository.sendPowerAction(PowerAction.SLEEP)

        assertEquals(TvConnectionState.Failed("Broken pipe"), repository.connectionState.value)
    }

    @Test
    fun `rebooting runs the reboot command on the TV`() = runTest {
        repository.connect(tvAddress)

        repository.sendPowerAction(PowerAction.REBOOT)

        assertEquals("reboot", shell.receivedCommands.last())
    }

    @Test
    fun `rebooting ends up disconnected`() = runTest {
        repository.connect(tvAddress)

        repository.sendPowerAction(PowerAction.REBOOT)

        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `powering off ends up disconnected when the TV drops the connection`() = runTest {
        repository.connect(tvAddress)
        shell.failOn("reboot -p", IOException("Connection reset"))

        repository.sendPowerAction(PowerAction.POWER_OFF)

        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `powering off closes the connection`() = runTest {
        repository.connect(tvAddress)

        repository.sendPowerAction(PowerAction.POWER_OFF)

        assertTrue(shell.isClosed)
    }

    @Test
    fun `a TV that refuses to power off marks the connection as failed`() = runTest {
        repository.connect(tvAddress)
        shell.failOn("reboot -p", TvCommandFailedException("TV command failed (exit code 1): not allowed"))

        repository.sendPowerAction(PowerAction.POWER_OFF)

        val expected = TvConnectionState.Failed("TV command failed (exit code 1): not allowed")
        assertEquals(expected, repository.connectionState.value)
    }

    @Test
    fun `sending a power action while disconnected does nothing`() = runTest {
        repository.sendPowerAction(PowerAction.REBOOT)

        assertTrue(shell.receivedCommands.isEmpty())
        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    private val freshApps = listOf(createTvApp(packageName = "fresh.app"))
    private val cachedApps = listOf(createTvApp(packageName = "cached.app"))

    private suspend fun loadAppStates(): List<TvAppsState> = repository.loadApps().toList()

    @Test
    fun `without cached apps only the fresh apps are shown`() = runTest {
        appLoader.apps = freshApps
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Loaded(freshApps)), loadAppStates())
    }

    @Test
    fun `cached apps are shown first and then replaced by the fresh apps`() = runTest {
        appsCache.cachedApps[tvAddress] = cachedApps
        appLoader.apps = freshApps
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Loaded(cachedApps), TvAppsState.Loaded(freshApps)), loadAppStates())
    }

    @Test
    fun `fresh apps with names and icons are cached`() = runTest {
        appLoader.apps = freshApps
        repository.connect(tvAddress)

        loadAppStates()

        assertEquals(freshApps, appsCache.cachedApps[tvAddress])
    }

    @Test
    fun `fresh apps without names and icons are not cached`() = runTest {
        appLoader.apps = freshApps
        appLoader.hasNamesAndIcons = false
        repository.connect(tvAddress)

        loadAppStates()

        assertTrue(appsCache.cachedApps.isEmpty())
    }

    @Test
    fun `fresh apps without names and icons do not replace cached apps`() = runTest {
        appsCache.cachedApps[tvAddress] = cachedApps
        appLoader.apps = freshApps
        appLoader.hasNamesAndIcons = false
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Loaded(cachedApps)), loadAppStates())
        assertEquals(cachedApps, appsCache.cachedApps[tvAddress])
    }

    @Test
    fun `fresh apps without names and icons are shown when nothing is cached`() = runTest {
        appLoader.apps = freshApps
        appLoader.hasNamesAndIcons = false
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Loaded(freshApps)), loadAppStates())
    }

    @Test
    fun `apps cached for another TV are not shown`() = runTest {
        appsCache.cachedApps[TvAddress("192.168.1.99", 5555)] = cachedApps
        appLoader.apps = freshApps
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Loaded(freshApps)), loadAppStates())
    }

    @Test
    fun `loading the apps while disconnected is unavailable`() = runTest {
        assertEquals(listOf(TvAppsState.Unavailable), loadAppStates())
    }

    @Test
    fun `a TV that refuses to list the apps is unavailable but stays connected`() = runTest {
        appLoader.error = TvCommandFailedException("TV command failed (exit code 255): unknown command")
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Unavailable), loadAppStates())
        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `cached apps stay shown when the TV refuses to list the apps`() = runTest {
        appsCache.cachedApps[tvAddress] = cachedApps
        appLoader.error = TvCommandFailedException("TV command failed (exit code 255): unknown command")
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Loaded(cachedApps)), loadAppStates())
    }

    @Test
    fun `a broken connection while loading the apps marks the connection as failed`() = runTest {
        appLoader.error = IOException("Broken pipe")
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Unavailable), loadAppStates())
        assertEquals(TvConnectionState.Failed("Broken pipe"), repository.connectionState.value)
    }

    @Test
    fun `launching an app runs the launch command on the TV`() = runTest {
        val app = createTvApp(packageName = "com.netflix.ninja")
        repository.connect(tvAddress)

        repository.launchApp(app)

        assertEquals(listOf(createLaunchAppCommand(app)), shell.receivedCommands)
    }

    @Test
    fun `launching an app while disconnected does nothing`() = runTest {
        repository.launchApp(createTvApp())

        assertTrue(shell.receivedCommands.isEmpty())
    }

    @Test
    fun `sending a key reports that the TV accepted it`() = runTest {
        repository.connect(tvAddress)

        assertTrue(repository.sendKey(RemoteKey.VOLUME_UP))
    }

    @Test
    fun `sending a key while disconnected reports that nothing was sent`() = runTest {
        assertFalse(repository.sendKey(RemoteKey.VOLUME_UP))
    }

    @Test
    fun `a failing key press reports that it was not accepted`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        assertFalse(repository.sendKey(RemoteKey.VOLUME_UP))
    }

    @Test
    fun `a sleep power action reports that the TV accepted it`() = runTest {
        repository.connect(tvAddress)

        assertTrue(repository.sendPowerAction(PowerAction.SLEEP))
    }

    @Test
    fun `a reboot reports success even though the TV drops the connection`() = runTest {
        repository.connect(tvAddress)
        shell.failOn("reboot", IOException("Connection reset"))

        assertTrue(repository.sendPowerAction(PowerAction.REBOOT))
    }

    @Test
    fun `a power off that the TV refuses reports that it was not accepted`() = runTest {
        repository.connect(tvAddress)
        shell.failOn("reboot -p", TvCommandFailedException("TV command failed (exit code 1): not allowed"))

        assertFalse(repository.sendPowerAction(PowerAction.POWER_OFF))
    }

    @Test
    fun `a power action while disconnected reports that nothing was sent`() = runTest {
        assertFalse(repository.sendPowerAction(PowerAction.SLEEP))
    }

    @Test
    fun `launching an app reports that the TV accepted it`() = runTest {
        repository.connect(tvAddress)

        assertTrue(repository.launchApp(createTvApp()))
    }

    @Test
    fun `a failing app launch reports that it was not accepted`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        assertFalse(repository.launchApp(createTvApp()))
    }

    @Test
    fun `launching an app while disconnected reports that nothing was sent`() = runTest {
        assertFalse(repository.launchApp(createTvApp()))
    }

    private val debugAction = DebugAction("Check", "A check", listOf(DebugStep.RunCommand("echo hi")))

    @Test
    fun `a debug action runs its steps on the connected TV`() = runTest {
        repository.connect(tvAddress)

        repository.runDebugAction(debugAction)

        assertEquals(listOf("echo hi"), shell.receivedCommands)
    }

    @Test
    fun `a debug action reports what its steps did`() = runTest {
        repository.connect(tvAddress)
        shell.respondTo("echo hi", "hi")

        val report = repository.runDebugAction(debugAction)

        assertEquals("hi", report.stepResults.single().output)
    }

    @Test
    fun `a debug action while disconnected runs nothing and reports it`() = runTest {
        val report = repository.runDebugAction(debugAction)

        assertTrue(shell.receivedCommands.isEmpty())
        assertFalse(report.hasPassed)
    }

    @Test
    fun `a failing debug step does not change the connection state`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        repository.runDebugAction(debugAction)

        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `disconnecting closes the connection`() = runTest {
        repository.connect(tvAddress)

        repository.disconnect()

        assertTrue(shell.isClosed)
    }

    @Test
    fun `disconnecting ends up disconnected`() = runTest {
        repository.connect(tvAddress)

        repository.disconnect()

        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `disconnecting while already disconnected closes nothing`() = runTest {
        repository.disconnect()

        assertFalse(shell.isClosed)
    }

    @Test
    fun `the fresh apps come with the system apps`() = runTest {
        val freshApps = listOf(createTvApp())
        val systemApps = listOf(createTvSystemApp())
        appLoader.apps = freshApps
        appLoader.systemApps = systemApps
        repository.connect(tvAddress)

        assertEquals(listOf(TvAppsState.Loaded(freshApps, systemApps)), loadAppStates())
    }

    @Test
    fun `the cached apps are shown before the system apps are known`() = runTest {
        val cachedApps = listOf(createTvApp(label = "Cached"))
        val freshApps = listOf(createTvApp(label = "Fresh"))
        val systemApps = listOf(createTvSystemApp())
        appsCache.cachedApps[tvAddress] = cachedApps
        appLoader.apps = freshApps
        appLoader.systemApps = systemApps
        repository.connect(tvAddress)

        val expected = listOf(TvAppsState.Loaded(cachedApps), TvAppsState.Loaded(freshApps, systemApps))
        assertEquals(expected, loadAppStates())
    }

    @Test
    fun `opening the info of a system app runs the command on the TV`() = runTest {
        val app = createTvSystemApp(packageName = "com.android.settings")
        repository.connect(tvAddress)

        repository.openSystemAppInfo(app)

        assertEquals(listOf(createOpenAppInfoCommand(app)), shell.receivedCommands)
    }

    @Test
    fun `opening the info of a system app reports that the TV accepted it`() = runTest {
        repository.connect(tvAddress)

        assertTrue(repository.openSystemAppInfo(createTvSystemApp()))
    }

    @Test
    fun `opening the info of a system app while disconnected reports that nothing was sent`() = runTest {
        assertFalse(repository.openSystemAppInfo(createTvSystemApp()))
    }

    @Test
    fun `uninstalling an app runs the uninstall command on the TV`() = runTest {
        val app = createTvApp(packageName = "com.netflix.ninja")
        repository.connect(tvAddress)

        repository.uninstallApp(app)

        assertEquals(listOf(createUninstallAppCommand(app)), shell.receivedCommands)
    }

    @Test
    fun `an uninstall that the TV accepts reports uninstalled`() = runTest {
        repository.connect(tvAddress)

        assertEquals(AppUninstallResult.UNINSTALLED, repository.uninstallApp(createTvApp()))
    }

    @Test
    fun `an uninstall that the TV refuses reports refused`() = runTest {
        val app = createTvApp()
        repository.connect(tvAddress)
        shell.failOn(createUninstallAppCommand(app), TvCommandFailedException("Failure [DELETE_FAILED_INTERNAL_ERROR]"))

        assertEquals(AppUninstallResult.REFUSED, repository.uninstallApp(app))
    }

    @Test
    fun `an uninstall that the TV refuses keeps the connection`() = runTest {
        val app = createTvApp()
        repository.connect(tvAddress)
        shell.failOn(createUninstallAppCommand(app), TvCommandFailedException("Failure [DELETE_FAILED_INTERNAL_ERROR]"))

        repository.uninstallApp(app)

        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `uninstalling an app while disconnected reports that nothing was sent`() = runTest {
        assertEquals(AppUninstallResult.NOT_SENT, repository.uninstallApp(createTvApp()))
    }

    @Test
    fun `an uninstalled app is removed from the cached apps`() = runTest {
        val removedApp = createTvApp(packageName = "com.removed")
        val keptApp = createTvApp(packageName = "com.kept")
        appsCache.cachedApps[tvAddress] = listOf(keptApp, removedApp)
        repository.connect(tvAddress)

        repository.uninstallApp(removedApp)

        assertEquals(listOf(keptApp), appsCache.cachedApps[tvAddress])
    }

    @Test
    fun `an app that stays installed stays in the cached apps`() = runTest {
        val app = createTvApp()
        appsCache.cachedApps[tvAddress] = listOf(app)
        repository.connect(tvAddress)
        shell.failOn(createUninstallAppCommand(app), TvCommandFailedException("Failure [DELETE_FAILED_INTERNAL_ERROR]"))

        repository.uninstallApp(app)

        assertEquals(listOf(app), appsCache.cachedApps[tvAddress])
    }
}
