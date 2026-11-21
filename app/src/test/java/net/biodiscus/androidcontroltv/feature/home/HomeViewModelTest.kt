package net.biodiscus.androidcontroltv.feature.home

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.testing.FakeTvRepository
import net.biodiscus.androidcontroltv.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tvRepository = FakeTvRepository()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun createViewModel() {
        viewModel = HomeViewModel(tvRepository)
    }

    @Test
    fun `typing an address shows it in the ui state`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onAddressChange("192.168.1.20")

            assertEquals("192.168.1.20", awaitItem().addressInput)
        }
    }

    @Test
    fun `the ui state follows the connection state of the repository`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            tvRepository.setConnectionState(TvConnectionState.Connecting)

            assertEquals(TvConnectionState.Connecting, awaitItem().connectionState)
        }
    }

    @Test
    fun `clicking connect connects to the typed address`() {
        viewModel.onAddressChange("192.168.1.20:5556")

        viewModel.onConnectClick()

        assertEquals(listOf(TvAddress("192.168.1.20", 5556)), tvRepository.connectedAddresses)
    }

    @Test
    fun `clicking connect with an invalid address does not connect`() {
        viewModel.onAddressChange("not an address")

        viewModel.onConnectClick()

        assertTrue(tvRepository.connectedAddresses.isEmpty())
    }

    @Test
    fun `clicking disconnect disconnects from the TV`() {
        viewModel.onDisconnectClick()

        assertEquals(1, tvRepository.disconnectCount)
    }

    @Test
    fun `clicking a key sends that key to the TV`() {
        viewModel.onKeyClick(RemoteKey.VOLUME_DOWN)

        assertEquals(listOf(RemoteKey.VOLUME_DOWN), tvRepository.sentKeys)
    }
}
