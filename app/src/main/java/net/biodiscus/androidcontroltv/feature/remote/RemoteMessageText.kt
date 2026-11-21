package net.biodiscus.androidcontroltv.feature.remote

import android.content.Context
import net.biodiscus.androidcontroltv.R

internal fun createMessageText(context: Context, message: RemoteMessage): String =
    when (message) {
        is RemoteMessage.KeySent ->
            context.getString(R.string.remote_message_sent_to_tv, context.getString(labelFor(message.key)))

        is RemoteMessage.PowerActionSent ->
            context.getString(R.string.remote_message_sent_to_tv, context.getString(labelFor(message.action)))

        is RemoteMessage.AppLaunched ->
            context.getString(R.string.remote_message_app_launched, message.app.label)

        is RemoteMessage.FavoriteChanged -> createFavoriteChangedText(context, message)
        is RemoteMessage.DefaultPowerActionChanged -> createDefaultPowerActionChangedText(context, message)
    }

private fun createFavoriteChangedText(context: Context, message: RemoteMessage.FavoriteChanged): String {
    val textId = if (message.isFavorite) R.string.remote_message_favorite_added else R.string.remote_message_favorite_removed
    return context.getString(textId, message.app.label)
}

private fun createDefaultPowerActionChangedText(context: Context, message: RemoteMessage.DefaultPowerActionChanged): String {
    val textId = if (message.isDefault) R.string.remote_message_default_set else R.string.remote_message_default_cleared
    return context.getString(textId, context.getString(labelFor(message.action)))
}
