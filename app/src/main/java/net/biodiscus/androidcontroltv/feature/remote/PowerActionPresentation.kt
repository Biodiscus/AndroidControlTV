package net.biodiscus.androidcontroltv.feature.remote

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.ui.graphics.vector.ImageVector
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey

@StringRes
internal fun labelFor(action: PowerAction): Int = when (action) {
    PowerAction.REBOOT -> R.string.power_action_reboot
    PowerAction.SLEEP -> R.string.power_action_sleep
    PowerAction.POWER_OFF -> R.string.power_action_power_off
}

internal fun iconFor(action: PowerAction): ImageVector = when (action) {
    PowerAction.REBOOT -> Icons.Rounded.RestartAlt
    PowerAction.SLEEP -> Icons.Rounded.Bedtime
    PowerAction.POWER_OFF -> Icons.Rounded.PowerSettingsNew
}

internal fun iconForPowerButton(defaultAction: PowerAction?): ImageVector =
    if (defaultAction == null) Icons.Rounded.PowerSettingsNew else iconFor(defaultAction)

@StringRes
internal fun labelForPowerButton(defaultAction: PowerAction?): Int =
    if (defaultAction == null) labelFor(RemoteKey.POWER) else labelFor(defaultAction)
