package net.biodiscus.androidcontroltv.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SavedTvAddressesTest {

    private val livingRoomTv = TvAddress("192.168.1.20", 5555)
    private val bedroomTv = TvAddress("192.168.1.30", 5555)

    @Test
    fun `a new address is placed in front of the older ones`() {
        val result = addAsMostRecent(listOf(livingRoomTv), bedroomTv)

        assertEquals(listOf(bedroomTv, livingRoomTv), result)
    }

    @Test
    fun `an address that was saved before moves to the front`() {
        val result = addAsMostRecent(listOf(livingRoomTv, bedroomTv), bedroomTv)

        assertEquals(listOf(bedroomTv, livingRoomTv), result)
    }

    @Test
    fun `an address is never saved twice`() {
        val result = addAsMostRecent(listOf(livingRoomTv), livingRoomTv)

        assertEquals(listOf(livingRoomTv), result)
    }

    @Test
    fun `only the most recent addresses are kept`() {
        val fullList = (1..MAX_SAVED_TV_ADDRESSES).map { TvAddress("192.168.1.$it", 5555) }

        val result = addAsMostRecent(fullList, bedroomTv)

        assertEquals(MAX_SAVED_TV_ADDRESSES, result.size)
        assertEquals(bedroomTv, result.first())
        assertEquals(fullList[MAX_SAVED_TV_ADDRESSES - 2], result.last())
    }

    @Test
    fun `encoded addresses are decoded into the same list`() {
        val encoded = encodeSavedAddresses(listOf(livingRoomTv, bedroomTv))

        assertEquals(listOf(livingRoomTv, bedroomTv), decodeSavedAddresses(encoded))
    }

    @Test
    fun `an empty text is decoded into an empty list`() {
        assertEquals(emptyList<TvAddress>(), decodeSavedAddresses(""))
    }

    @Test
    fun `lines that are not an address are skipped when decoding`() {
        val decoded = decodeSavedAddresses("192.168.1.20:5555\nnot an address")

        assertEquals(listOf(livingRoomTv), decoded)
    }
}
