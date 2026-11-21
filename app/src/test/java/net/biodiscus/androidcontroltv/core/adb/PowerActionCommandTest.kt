package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.PowerAction
import org.junit.Assert.assertEquals
import org.junit.Test

class PowerActionCommandTest {

    @Test
    fun `rebooting runs the reboot command`() {
        assertEquals("reboot", createPowerActionCommand(PowerAction.REBOOT))
    }

    @Test
    fun `sleeping presses the sleep key`() {
        assertEquals("input keyevent KEYCODE_SLEEP", createPowerActionCommand(PowerAction.SLEEP))
    }

    @Test
    fun `powering off runs the reboot command with the power off flag`() {
        assertEquals("reboot -p", createPowerActionCommand(PowerAction.POWER_OFF))
    }

    @Test
    fun `every power action has a different command`() {
        val commands = PowerAction.entries.map(::createPowerActionCommand)

        assertEquals(PowerAction.entries.size, commands.toSet().size)
    }
}
