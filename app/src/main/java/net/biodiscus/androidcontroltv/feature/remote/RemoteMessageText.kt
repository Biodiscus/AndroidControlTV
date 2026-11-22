package net.biodiscus.androidcontroltv.feature.remote

import android.content.Context
import net.biodiscus.androidcontroltv.R

internal fun createMessageText(context: Context, message: RemoteMessage): String =
    when (message) {
        is RemoteMessage.AppUninstallRefused ->
            context.getString(R.string.remote_message_app_uninstall_refused, message.app.label)
    }
