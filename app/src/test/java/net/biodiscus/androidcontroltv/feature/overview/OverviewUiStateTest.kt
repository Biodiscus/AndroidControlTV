package net.biodiscus.androidcontroltv.feature.overview

import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverviewUiStateTest {

    private fun createUiState(
        addressInput: String = "",
        connectionState: TvConnectionState = TvConnectionState.Disconnected,
        savedAddresses: List<TvAddress> = emptyList(),
    ) = createOverviewUiState(addressInput, connectionState, savedAddresses)

    @Test
    fun `a valid address is marked as valid`() {
        val uiState = createUiState(addressInput = "192.168.1.20")

        assertTrue(uiState.isAddressValid)
    }

    @Test
    fun `an empty address is marked as invalid`() {
        val uiState = createUiState(addressInput = "")

        assertFalse(uiState.isAddressValid)
    }

    @Test
    fun `the connection state is passed on`() {
        val uiState = createUiState(connectionState = TvConnectionState.Connecting)

        assertEquals(TvConnectionState.Connecting, uiState.connectionState)
    }

    @Test
    fun `the saved addresses are passed on`() {
        val savedAddresses = listOf(TvAddress("192.168.1.20", 5555))

        val uiState = createUiState(savedAddresses = savedAddresses)

        assertEquals(savedAddresses, uiState.savedAddresses)
    }
}
