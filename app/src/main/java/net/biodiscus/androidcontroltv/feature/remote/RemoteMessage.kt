package net.biodiscus.androidcontroltv.feature.remote

import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvApp

/** Something that went well and that the user is told about once. */
internal sealed interface RemoteMessage {
    data class KeySent(val key: RemoteKey) : RemoteMessage
    data class PowerActionSent(val action: PowerAction) : RemoteMessage
    data class AppLaunched(val app: TvApp) : RemoteMessage
    data class FavoriteChanged(val app: TvApp, val isFavorite: Boolean) : RemoteMessage
    data class DefaultPowerActionChanged(val action: PowerAction, val isDefault: Boolean) : RemoteMessage
}
