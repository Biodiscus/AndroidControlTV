package net.biodiscus.androidcontroltv.feature.remote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RestartAlt
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.PowerAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PowerActionPresentationTest {

    @Test
    fun `without a default action the power button shows the power icon`() {
        assertEquals(Icons.Rounded.PowerSettingsNew, iconForPowerButton(defaultAction = null))
    }

    @Test
    fun `a reboot default shows the restart icon`() {
        assertEquals(Icons.Rounded.RestartAlt, iconForPowerButton(PowerAction.REBOOT))
    }

    @Test
    fun `a sleep default shows the sleep icon`() {
        assertEquals(Icons.Rounded.Bedtime, iconForPowerButton(PowerAction.SLEEP))
    }

    @Test
    fun `a power off default shows the power icon`() {
        assertEquals(Icons.Rounded.PowerSettingsNew, iconForPowerButton(PowerAction.POWER_OFF))
    }

    @Test
    fun `every power action has a different icon`() {
        val icons = PowerAction.entries.map(::iconFor)

        assertEquals(PowerAction.entries.size, icons.toSet().size)
    }

    @Test
    fun `without a default action the power button is labelled power`() {
        assertEquals(R.string.remote_key_power, labelForPowerButton(defaultAction = null))
    }

    @Test
    fun `with a default action the power button is labelled with that action`() {
        assertNotEquals(R.string.remote_key_power, labelForPowerButton(PowerAction.SLEEP))
        assertEquals(R.string.power_action_sleep, labelForPowerButton(PowerAction.SLEEP))
    }
}
