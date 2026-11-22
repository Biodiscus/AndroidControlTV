package net.biodiscus.androidcontroltv.core.adb

import org.junit.Assert.assertEquals
import org.junit.Test

class TextInputCommandTest {

    @Test
    fun `typing a word runs input text with that word`() {
        assertEquals("input text 'netflix'", createTextInputCommand("netflix"))
    }

    @Test
    fun `a space is typed as the percent s marker`() {
        assertEquals("input text 'star%swars'", createTextInputCommand("star wars"))
    }

    @Test
    fun `characters that the shell treats specially stay inside the quotes`() {
        assertEquals("input text 'a&b;\$HOME|c'", createTextInputCommand("a&b;\$HOME|c"))
    }

    @Test
    fun `a single quote is escaped so the quoted text does not end early`() {
        assertEquals("input text 'it'\\''s'", createTextInputCommand("it's"))
    }
}
