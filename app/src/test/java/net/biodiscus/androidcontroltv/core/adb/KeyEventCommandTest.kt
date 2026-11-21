package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.RemoteKey
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyEventCommandTest {

    @Test
    fun `volume up becomes an input keyevent command`() {
        val command = createKeyEventCommand(RemoteKey.VOLUME_UP)

        assertEquals("input keyevent KEYCODE_VOLUME_UP", command)
    }

    @Test
    fun `mute uses the volume mute key and not the microphone mute key`() {
        val command = createKeyEventCommand(RemoteKey.MUTE)

        assertEquals("input keyevent KEYCODE_VOLUME_MUTE", command)
    }

    @Test
    fun `every remote key has a different android key name`() {
        val keyNames = RemoteKey.entries.map { it.androidKeyName }

        assertEquals(RemoteKey.entries.size, keyNames.toSet().size)
    }
}
