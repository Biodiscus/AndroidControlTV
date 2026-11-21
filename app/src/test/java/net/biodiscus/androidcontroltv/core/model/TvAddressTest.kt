package net.biodiscus.androidcontroltv.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TvAddressTest {

    @Test
    fun `an address without a port uses the default adb port`() {
        val address = TvAddress.parse("192.168.1.20")

        assertEquals(TvAddress("192.168.1.20", 5555), address)
    }

    @Test
    fun `an address with a port keeps that port`() {
        val address = TvAddress.parse("192.168.1.20:5556")

        assertEquals(TvAddress("192.168.1.20", 5556), address)
    }

    @Test
    fun `surrounding spaces are ignored`() {
        val address = TvAddress.parse("  192.168.1.20  ")

        assertEquals(TvAddress("192.168.1.20", 5555), address)
    }

    @Test
    fun `a host name is accepted`() {
        val address = TvAddress.parse("living-room-tv.local")

        assertEquals(TvAddress("living-room-tv.local", 5555), address)
    }

    @Test
    fun `an empty text is not an address`() {
        assertNull(TvAddress.parse(""))
    }

    @Test
    fun `a text with only spaces is not an address`() {
        assertNull(TvAddress.parse("   "))
    }

    @Test
    fun `a missing host is not an address`() {
        assertNull(TvAddress.parse(":5555"))
    }

    @Test
    fun `a port that is not a number is not an address`() {
        assertNull(TvAddress.parse("192.168.1.20:abc"))
    }

    @Test
    fun `a port of zero is not an address`() {
        assertNull(TvAddress.parse("192.168.1.20:0"))
    }

    @Test
    fun `a port above 65535 is not an address`() {
        assertNull(TvAddress.parse("192.168.1.20:65536"))
    }

    @Test
    fun `a host with a space inside is not an address`() {
        assertNull(TvAddress.parse("192.168 .1.20"))
    }

    @Test
    fun `more than one colon is not an address`() {
        assertNull(TvAddress.parse("192.168.1.20:5555:1"))
    }

    @Test
    fun `the display text shows host and port`() {
        val address = TvAddress("192.168.1.20", 5555)

        assertEquals("192.168.1.20:5555", address.displayText)
    }
}
