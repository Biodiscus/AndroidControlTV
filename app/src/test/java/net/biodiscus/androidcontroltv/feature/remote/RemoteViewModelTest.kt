package net.biodiscus.androidcontroltv.feature.remote

import net.biodiscus.androidcontroltv.core.model.AppUninstallResult
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.testing.FakeFavoriteAppsRepository
import net.biodiscus.androidcontroltv.testing.FakePowerSettingsRepository
import net.biodiscus.androidcontroltv.testing.FakeSavedTvRepository
import net.biodiscus.androidcontroltv.testing.FakeTvRepository
import net.biodiscus.androidcontroltv.testing.MainDispatcherRule
import net.biodiscus.androidcontroltv.testing.createTvApp
import net.biodiscus.androidcontroltv.testing.createTvSystemApp
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RemoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedAddress = TvAddress("192.168.1.20", 5555)
    private val tvRepository = FakeTvRepository()

    private var powerSettings = FakePowerSettingsRepository()
    private val favoriteApps = FakeFavoriteAppsRepository()

    private fun createViewModel(savedTv: TvAddress? = savedAddress) =
        RemoteViewModel(tvRepository, FakeSavedTvRepository(listOfNotNull(savedTv)), powerSettings, favoriteApps)

    @Test
    fun `opening the remote connects to the saved TV`() {
        createViewModel()

        assertEquals(listOf(savedAddress), tvRepository.connectedAddresses)
    }

    @Test
    fun `opening the remote connects to the TV that was connected most recently`() {
        val olderTv = TvAddress("192.168.1.30", 5555)

        RemoteViewModel(tvRepository, FakeSavedTvRepository(listOf(savedAddress, olderTv)), powerSettings, favoriteApps)

        assertEquals(listOf(savedAddress), tvRepository.connectedAddresses)
    }

    @Test
    fun `opening the remote while already connected does not connect again`() {
        tvRepository.setConnectionState(TvConnectionState.Connected(savedAddress))

        createViewModel()

        assertTrue(tvRepository.connectedAddresses.isEmpty())
    }

    @Test
    fun `opening the remote without a saved TV does not connect`() {
        createViewModel(savedTv = null)

        assertTrue(tvRepository.connectedAddresses.isEmpty())
    }

    @Test
    fun `clicking retry connects to the saved TV again`() {
        tvRepository.connectFailureReason = "Connection refused"
        val viewModel = createViewModel()

        viewModel.onRetryClick()

        assertEquals(listOf(savedAddress, savedAddress), tvRepository.connectedAddresses)
    }

    @Test
    fun `clicking a key sends that key to the TV`() {
        val viewModel = createViewModel()

        viewModel.onKeyClick(RemoteKey.VOLUME_DOWN)

        assertEquals(listOf(RemoteKey.VOLUME_DOWN), tvRepository.sentKeys)
    }

    @Test
    fun `sending text sends that text to the TV`() {
        val viewModel = createViewModel()

        viewModel.onTextSend("star wars")

        assertEquals(listOf("star wars"), tvRepository.sentTexts)
    }

    @Test
    fun `sending empty text sends nothing to the TV`() {
        val viewModel = createViewModel()

        viewModel.onTextSend("")

        assertTrue(tvRepository.sentTexts.isEmpty())
    }

    @Test
    fun `sending text makes the indicator blink`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onTextSend("star wars")

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `text that the TV did not accept does not make the indicator blink`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.uiState.test {
            viewModel.onTextSend("star wars")

            assertFalse(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `tapping power without a default action sends the power key`() {
        val viewModel = createViewModel()

        viewModel.onKeyClick(RemoteKey.POWER)

        assertEquals(listOf(RemoteKey.POWER), tvRepository.sentKeys)
    }

    @Test
    fun `tapping power with a default action runs that action`() {
        powerSettings = FakePowerSettingsRepository(PowerAction.SLEEP)
        val viewModel = createViewModel()

        viewModel.onKeyClick(RemoteKey.POWER)

        assertEquals(listOf(PowerAction.SLEEP), tvRepository.sentPowerActions)
    }

    @Test
    fun `tapping power with a default action does not send the power key`() {
        powerSettings = FakePowerSettingsRepository(PowerAction.SLEEP)
        val viewModel = createViewModel()

        viewModel.onKeyClick(RemoteKey.POWER)

        assertEquals(emptyList<RemoteKey>(), tvRepository.sentKeys)
    }

    @Test
    fun `a default power action does not change the other keys`() {
        powerSettings = FakePowerSettingsRepository(PowerAction.SLEEP)
        val viewModel = createViewModel()

        viewModel.onKeyClick(RemoteKey.HOME)

        assertEquals(listOf(RemoteKey.HOME), tvRepository.sentKeys)
    }

    @Test
    fun `checking a power action makes it the default`() = runTest {
        val viewModel = createViewModel()

        viewModel.onDefaultPowerActionChange(PowerAction.REBOOT, isDefault = true)

        assertEquals(PowerAction.REBOOT, powerSettings.defaultPowerAction.first())
    }

    @Test
    fun `unchecking the default power action clears the default`() = runTest {
        powerSettings = FakePowerSettingsRepository(PowerAction.REBOOT)
        val viewModel = createViewModel()

        viewModel.onDefaultPowerActionChange(PowerAction.REBOOT, isDefault = false)

        assertNull(powerSettings.defaultPowerAction.first())
    }

    @Test
    fun `the ui state shows the default power action`() = runTest {
        powerSettings = FakePowerSettingsRepository(PowerAction.POWER_OFF)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(PowerAction.POWER_OFF, expectMostRecentItem().defaultPowerAction)
        }
    }

    @Test
    fun `the ui state shows the apps of the TV once they are loaded`() = runTest {
        val apps = TvAppsState.Loaded(listOf(createTvApp()))
        tvRepository.appsStates = listOf(apps)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(apps, expectMostRecentItem().tvApps)
        }
    }

    @Test
    fun `the ui state ends up with the fresh apps after the cached apps`() = runTest {
        val cachedApps = TvAppsState.Loaded(listOf(createTvApp(label = "Cached")))
        val freshApps = TvAppsState.Loaded(listOf(createTvApp(label = "Fresh")))
        tvRepository.appsStates = listOf(cachedApps, freshApps)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(freshApps, expectMostRecentItem().tvApps)
        }
    }

    @Test
    fun `marking an app as favorite saves its package name`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAppFavoriteChange(createTvApp(packageName = "com.netflix"), isFavorite = true)

        assertEquals(setOf("com.netflix"), favoriteApps.favoritePackageNames.first())
    }

    @Test
    fun `unmarking a favorite app removes its package name`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(packageName = "com.netflix")
        viewModel.onAppFavoriteChange(app, isFavorite = true)

        viewModel.onAppFavoriteChange(app, isFavorite = false)

        assertEquals(emptySet<String>(), favoriteApps.favoritePackageNames.first())
    }

    @Test
    fun `the ui state shows the favorite package names`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAppFavoriteChange(createTvApp(packageName = "com.netflix"), isFavorite = true)

        viewModel.uiState.test {
            assertEquals(setOf("com.netflix"), expectMostRecentItem().favoritePackageNames)
        }
    }

    @Test
    fun `pressing a key makes the indicator blink`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onKeyClick(RemoteKey.VOLUME_UP)

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `a key that the TV did not accept does not make the indicator blink`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.uiState.test {
            viewModel.onKeyClick(RemoteKey.VOLUME_UP)

            assertFalse(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `tapping power without a default action makes the indicator blink`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onKeyClick(RemoteKey.POWER)

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `tapping power with a default action makes the indicator blink`() = runTest {
        powerSettings = FakePowerSettingsRepository(PowerAction.SLEEP)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onKeyClick(RemoteKey.POWER)

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `choosing a power action makes the indicator blink`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onPowerActionClick(PowerAction.REBOOT)

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `a power action that the TV did not accept does not make the indicator blink`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.uiState.test {
            viewModel.onPowerActionClick(PowerAction.REBOOT)

            assertFalse(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `launching an app makes the indicator blink`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(packageName = "com.netflix.ninja", label = "Netflix")

        viewModel.uiState.test {
            viewModel.onAppClick(app)

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `an app launch that the TV did not accept does not make the indicator blink`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.uiState.test {
            viewModel.onAppClick(createTvApp())

            assertFalse(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `confirming an uninstall asks the TV to uninstall the app`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(packageName = "com.netflix.ninja")

        viewModel.onAppUninstallConfirm(app)

        assertEquals(listOf(app), tvRepository.uninstalledApps)
    }

    @Test
    fun `an uninstalled app is no longer shown`() = runTest {
        val keptApp = createTvApp(packageName = "com.kept")
        val removedApp = createTvApp(packageName = "com.removed")
        tvRepository.appsStates = listOf(TvAppsState.Loaded(listOf(keptApp, removedApp)))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onAppUninstallConfirm(removedApp)

            assertEquals(TvAppsState.Loaded(listOf(keptApp)), expectMostRecentItem().tvApps)
        }
    }

    @Test
    fun `uninstalling an app makes the indicator blink`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(label = "Netflix")

        viewModel.uiState.test {
            viewModel.onAppUninstallConfirm(app)

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `an app that the TV refuses to uninstall shows that it was refused`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(label = "Live TV")
        tvRepository.uninstallResult = AppUninstallResult.REFUSED

        viewModel.messages.test {
            viewModel.onAppUninstallConfirm(app)

            assertEquals(RemoteMessage.AppUninstallRefused(app), awaitItem())
        }
    }

    @Test
    fun `an app that the TV refuses to uninstall stays in the list`() = runTest {
        val app = createTvApp()
        val apps = TvAppsState.Loaded(listOf(app))
        tvRepository.appsStates = listOf(apps)
        tvRepository.uninstallResult = AppUninstallResult.REFUSED
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onAppUninstallConfirm(app)

            assertEquals(apps, expectMostRecentItem().tvApps)
        }
    }

    @Test
    fun `an uninstall that never reached the TV does not make the indicator blink`() = runTest {
        val viewModel = createViewModel()
        tvRepository.uninstallResult = AppUninstallResult.NOT_SENT

        viewModel.uiState.test {
            viewModel.onAppUninstallConfirm(createTvApp())

            assertFalse(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `clicking a system app opens its info on the TV`() = runTest {
        val viewModel = createViewModel()
        val app = createTvSystemApp(packageName = "com.android.settings")

        viewModel.onSystemAppClick(app)

        assertEquals(listOf(app), tvRepository.openedSystemApps)
    }

    @Test
    fun `opening the info of a system app makes the indicator blink`() = runTest {
        val viewModel = createViewModel()
        val app = createTvSystemApp(label = "Settings")

        viewModel.uiState.test {
            viewModel.onSystemAppClick(app)

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `a system app info that the TV did not accept does not make the indicator blink`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.uiState.test {
            viewModel.onSystemAppClick(createTvSystemApp())

            assertFalse(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `the ui state shows when the apps could not be loaded`() = runTest {
        tvRepository.appsStates = listOf(TvAppsState.Unavailable)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(TvAppsState.Unavailable, expectMostRecentItem().tvApps)
        }
    }

    @Test
    fun `clicking an app launches it on the TV`() {
        val viewModel = createViewModel()
        val app = createTvApp(packageName = "com.netflix.ninja")

        viewModel.onAppClick(app)

        assertEquals(listOf(app), tvRepository.launchedApps)
    }

    @Test
    fun `choosing a power action sends it to the TV`() {
        val viewModel = createViewModel()

        viewModel.onPowerActionClick(PowerAction.REBOOT)

        assertEquals(listOf(PowerAction.REBOOT), tvRepository.sentPowerActions)
    }

    @Test
    fun `clicking back disconnects from the TV`() {
        val viewModel = createViewModel()

        viewModel.onBackClick(onDisconnected = {})

        assertEquals(1, tvRepository.disconnectCount)
    }

    @Test
    fun `clicking back tells the screen after the TV is disconnected`() {
        val viewModel = createViewModel()
        var connectionStateWhenDisconnected: TvConnectionState? = null

        viewModel.onBackClick(onDisconnected = { connectionStateWhenDisconnected = tvRepository.connectionState.value })

        assertEquals(TvConnectionState.Disconnected, connectionStateWhenDisconnected)
    }

    @Test
    fun `the indicator stops blinking after a moment`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onKeyClick(RemoteKey.VOLUME_UP)
            advanceTimeBy(CommandBlinkDuration)
            runCurrent()

            assertFalse(expectMostRecentItem().isCommandBlinking)
        }
    }

    @Test
    fun `a new command restarts the blink`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onKeyClick(RemoteKey.VOLUME_UP)
            advanceTimeBy(CommandBlinkDuration / 2)
            viewModel.onKeyClick(RemoteKey.VOLUME_UP)
            advanceTimeBy(CommandBlinkDuration / 2 + CommandBlinkDuration / 4)
            runCurrent()

            assertTrue(expectMostRecentItem().isCommandBlinking)
        }
    }
}
