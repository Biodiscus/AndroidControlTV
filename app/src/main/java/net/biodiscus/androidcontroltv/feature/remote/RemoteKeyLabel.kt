package net.biodiscus.androidcontroltv.feature.remote

import androidx.annotation.StringRes
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.RemoteKey

@StringRes
internal fun labelFor(key: RemoteKey): Int = when (key) {
    RemoteKey.VOLUME_UP -> R.string.remote_key_volume_up
    RemoteKey.VOLUME_DOWN -> R.string.remote_key_volume_down
    RemoteKey.MUTE -> R.string.remote_key_mute
    RemoteKey.POWER -> R.string.remote_key_power
    RemoteKey.HOME -> R.string.remote_key_home
    RemoteKey.BACK -> R.string.remote_key_back
    RemoteKey.UP -> R.string.remote_key_up
    RemoteKey.DOWN -> R.string.remote_key_down
    RemoteKey.LEFT -> R.string.remote_key_left
    RemoteKey.RIGHT -> R.string.remote_key_right
    RemoteKey.SELECT -> R.string.remote_key_select
    RemoteKey.PLAY_PAUSE -> R.string.remote_key_play_pause
}
