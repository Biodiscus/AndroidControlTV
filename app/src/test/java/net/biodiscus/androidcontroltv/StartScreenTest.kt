package net.biodiscus.androidcontroltv

import net.biodiscus.androidcontroltv.core.model.TvAddress
import org.junit.Assert.assertEquals
import org.junit.Test

class StartScreenTest {

    @Test
    fun `without a saved TV the app starts on the overview`() {
        assertEquals(StartScreen.Overview, createStartScreen(savedAddresses = emptyList()))
    }

    @Test
    fun `with a saved TV the app starts on the remote`() {
        assertEquals(StartScreen.Remote, createStartScreen(savedAddresses = listOf(TvAddress("192.168.1.20", 5555))))
    }
}
