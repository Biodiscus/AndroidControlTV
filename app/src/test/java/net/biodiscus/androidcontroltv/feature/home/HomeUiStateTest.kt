package net.biodiscus.androidcontroltv.feature.home

import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `a valid address is marked as valid`() {
        val uiState = createHomeUiState("192.168.1.20", TvConnectionState.Disconnected)

        assertTrue(uiState.isAddressValid)
    }

    @Test
    fun `an empty address is marked as invalid`() {
        val uiState = createHomeUiState("", TvConnectionState.Disconnected)

        assertFalse(uiState.isAddressValid)
    }

    @Test
    fun `the connection state is passed on`() {
        val uiState = createHomeUiState("", TvConnectionState.Connecting)

        assertEquals(TvConnectionState.Connecting, uiState.connectionState)
    }
}
