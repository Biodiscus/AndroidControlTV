package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.PowerAction

internal fun createPowerActionCommand(action: PowerAction): String = when (action) {
    PowerAction.REBOOT -> "reboot"
    PowerAction.SLEEP -> "input keyevent KEYCODE_SLEEP"
    PowerAction.POWER_OFF -> "reboot -p"
}
