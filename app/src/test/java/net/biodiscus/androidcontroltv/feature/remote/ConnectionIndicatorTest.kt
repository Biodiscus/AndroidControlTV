package net.biodiscus.androidcontroltv.feature.remote

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ConnectionIndicatorTest {

    private val red = chooseIndicatorColor(isConnected = false, isCommandBlinking = false)
    private val green = chooseIndicatorColor(isConnected = true, isCommandBlinking = false)
    private val blue = chooseIndicatorColor(isConnected = true, isCommandBlinking = true)

    @Test
    fun `the indicator is red when not connected`() {
        assertEquals(Color(0xFFD32F2F), red)
    }

    @Test
    fun `the indicator is green when connected`() {
        assertEquals(Color(0xFF2E7D32), green)
    }

    @Test
    fun `the indicator is blue when a command was accepted`() {
        assertEquals(Color(0xFF40C4FF), blue)
    }

    @Test
    fun `the indicator stays red when not connected even while blinking`() {
        assertEquals(red, chooseIndicatorColor(isConnected = false, isCommandBlinking = true))
    }

    @Test
    fun `red green and blue are different colors`() {
        assertNotEquals(red, green)
        assertNotEquals(green, blue)
    }
}
