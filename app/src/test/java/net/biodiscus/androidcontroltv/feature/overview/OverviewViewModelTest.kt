package net.biodiscus.androidcontroltv.feature.overview

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.testing.FakeSavedTvRepository
import net.biodiscus.androidcontroltv.testing.FakeTvRepository
import net.biodiscus.androidcontroltv.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OverviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tvRepository = FakeTvRepository()
    private val savedTvRepository = FakeSavedTvRepository()
    private lateinit var viewModel: OverviewViewModel

    @Before
    fun createViewModel() {
        viewModel = OverviewViewModel(tvRepository, savedTvRepository)
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

        viewModel.onConnectClick(onConnected = {})

        assertEquals(listOf(TvAddress("192.168.1.20", 5556)), tvRepository.connectedAddresses)
    }

    @Test
    fun `clicking connect with an invalid address does not connect`() {
        viewModel.onAddressChange("not an address")

        viewModel.onConnectClick(onConnected = {})

        assertTrue(tvRepository.connectedAddresses.isEmpty())
    }

    @Test
    fun `a successful connection saves the address of the TV`() = runTest {
        viewModel.onAddressChange("192.168.1.20")

        viewModel.onConnectClick(onConnected = {})

        assertEquals(listOf(TvAddress("192.168.1.20", 5555)), savedTvRepository.savedAddresses.first())
    }

    @Test
    fun `a successful connection tells the screen it is connected`() {
        var connectedCount = 0
        viewModel.onAddressChange("192.168.1.20")

        viewModel.onConnectClick(onConnected = { connectedCount++ })

        assertEquals(1, connectedCount)
    }

    @Test
    fun `a failed connection does not save the address of the TV`() = runTest {
        tvRepository.connectFailureReason = "Connection refused"
        viewModel.onAddressChange("192.168.1.20")

        viewModel.onConnectClick(onConnected = {})

        assertTrue(savedTvRepository.savedAddresses.first().isEmpty())
    }

    @Test
    fun `the ui state shows the TVs that were connected before`() = runTest {
        val savedTv = TvAddress("192.168.1.20", 5555)
        val viewModelWithSavedTv = OverviewViewModel(tvRepository, FakeSavedTvRepository(listOf(savedTv)))

        viewModelWithSavedTv.uiState.test {
            assertEquals(listOf(savedTv), expectMostRecentItem().savedAddresses)
        }
    }

    @Test
    fun `clicking a saved TV connects to that TV`() {
        val savedTv = TvAddress("192.168.1.30", 5556)

        viewModel.onSavedTvClick(savedTv, onConnected = {})

        assertEquals(listOf(savedTv), tvRepository.connectedAddresses)
    }

    @Test
    fun `clicking a saved TV tells the screen it is connected`() {
        var connectedCount = 0

        viewModel.onSavedTvClick(TvAddress("192.168.1.30", 5556), onConnected = { connectedCount++ })

        assertEquals(1, connectedCount)
    }

    @Test
    fun `connecting to a saved TV makes it the most recent one`() = runTest {
        val olderTv = TvAddress("192.168.1.20", 5555)
        val newerTv = TvAddress("192.168.1.30", 5555)
        val savedTvs = FakeSavedTvRepository(listOf(newerTv, olderTv))
        val viewModelWithSavedTvs = OverviewViewModel(tvRepository, savedTvs)

        viewModelWithSavedTvs.onSavedTvClick(olderTv, onConnected = {})

        assertEquals(listOf(olderTv, newerTv), savedTvs.savedAddresses.first())
    }

    @Test
    fun `a failed connection does not tell the screen it is connected`() {
        tvRepository.connectFailureReason = "Connection refused"
        var connectedCount = 0
        viewModel.onAddressChange("192.168.1.20")

        viewModel.onConnectClick(onConnected = { connectedCount++ })

        assertEquals(0, connectedCount)
    }
}
