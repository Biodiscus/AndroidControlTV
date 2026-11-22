package net.biodiscus.androidcontroltv.feature.remote

import net.biodiscus.androidcontroltv.core.model.TvApp

/** Something that went wrong and that the user is told about once. A command that went well only blinks the indicator. */
internal sealed interface RemoteMessage {
    data class AppUninstallRefused(val app: TvApp) : RemoteMessage
}
