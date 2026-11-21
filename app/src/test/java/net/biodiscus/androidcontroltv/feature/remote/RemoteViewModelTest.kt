package net.biodiscus.androidcontroltv.feature.remote

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
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `pressing a key shows that it was sent to the TV`() = runTest {
        val viewModel = createViewModel()

        viewModel.messages.test {
            viewModel.onKeyClick(RemoteKey.VOLUME_UP)

            assertEquals(RemoteMessage.KeySent(RemoteKey.VOLUME_UP), awaitItem())
        }
    }

    @Test
    fun `a key that the TV did not accept shows nothing`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.messages.test {
            viewModel.onKeyClick(RemoteKey.VOLUME_UP)

            expectNoEvents()
        }
    }

    @Test
    fun `tapping power without a default action shows that the power key was sent`() = runTest {
        val viewModel = createViewModel()

        viewModel.messages.test {
            viewModel.onKeyClick(RemoteKey.POWER)

            assertEquals(RemoteMessage.KeySent(RemoteKey.POWER), awaitItem())
        }
    }

    @Test
    fun `tapping power with a default action shows that the action was sent`() = runTest {
        powerSettings = FakePowerSettingsRepository(PowerAction.SLEEP)
        val viewModel = createViewModel()

        viewModel.messages.test {
            viewModel.onKeyClick(RemoteKey.POWER)

            assertEquals(RemoteMessage.PowerActionSent(PowerAction.SLEEP), awaitItem())
        }
    }

    @Test
    fun `choosing a power action shows that it was sent`() = runTest {
        val viewModel = createViewModel()

        viewModel.messages.test {
            viewModel.onPowerActionClick(PowerAction.REBOOT)

            assertEquals(RemoteMessage.PowerActionSent(PowerAction.REBOOT), awaitItem())
        }
    }

    @Test
    fun `a power action that the TV did not accept shows nothing`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.messages.test {
            viewModel.onPowerActionClick(PowerAction.REBOOT)

            expectNoEvents()
        }
    }

    @Test
    fun `launching an app shows that it was launched`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(packageName = "com.netflix.ninja", label = "Netflix")

        viewModel.messages.test {
            viewModel.onAppClick(app)

            assertEquals(RemoteMessage.AppLaunched(app), awaitItem())
        }
    }

    @Test
    fun `an app launch that the TV did not accept shows nothing`() = runTest {
        val viewModel = createViewModel()
        tvRepository.commandsSucceed = false

        viewModel.messages.test {
            viewModel.onAppClick(createTvApp())

            expectNoEvents()
        }
    }

    @Test
    fun `adding a favorite shows that it was added`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(packageName = "com.netflix.ninja", label = "Netflix")

        viewModel.messages.test {
            viewModel.onAppFavoriteChange(app, isFavorite = true)

            assertEquals(RemoteMessage.FavoriteChanged(app, isFavorite = true), awaitItem())
        }
    }

    @Test
    fun `removing a favorite shows that it was removed`() = runTest {
        val viewModel = createViewModel()
        val app = createTvApp(packageName = "com.netflix.ninja", label = "Netflix")

        viewModel.messages.test {
            viewModel.onAppFavoriteChange(app, isFavorite = false)

            assertEquals(RemoteMessage.FavoriteChanged(app, isFavorite = false), awaitItem())
        }
    }

    @Test
    fun `choosing a default power action shows that it was set`() = runTest {
        val viewModel = createViewModel()

        viewModel.messages.test {
            viewModel.onDefaultPowerActionChange(PowerAction.SLEEP, isDefault = true)

            assertEquals(RemoteMessage.DefaultPowerActionChanged(PowerAction.SLEEP, isDefault = true), awaitItem())
        }
    }

    @Test
    fun `clearing the default power action shows that it was cleared`() = runTest {
        val viewModel = createViewModel()

        viewModel.messages.test {
            viewModel.onDefaultPowerActionChange(PowerAction.SLEEP, isDefault = false)

            assertEquals(RemoteMessage.DefaultPowerActionChanged(PowerAction.SLEEP, isDefault = false), awaitItem())
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
}
