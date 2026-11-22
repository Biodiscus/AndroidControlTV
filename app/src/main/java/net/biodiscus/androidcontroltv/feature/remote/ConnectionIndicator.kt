package net.biodiscus.androidcontroltv.feature.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.biodiscus.androidcontroltv.R

private val NotConnectedColor = Color(0xFFD32F2F)
private val ConnectedColor = Color(0xFF2E7D32)
private val CommandAcceptedColor = Color(0xFF40C4FF)

internal fun chooseIndicatorColor(isConnected: Boolean, isCommandBlinking: Boolean): Color =
    when {
        !isConnected -> NotConnectedColor
        isCommandBlinking -> CommandAcceptedColor
        else -> ConnectedColor
    }

@Composable
internal fun ConnectionIndicator(isConnected: Boolean, isCommandBlinking: Boolean, modifier: Modifier = Modifier) {
    val description = stringResource(
        if (isConnected) R.string.remote_indicator_connected else R.string.remote_indicator_not_connected,
    )
    Box(
        modifier = modifier
            .size(16.dp)
            .background(chooseIndicatorColor(isConnected, isCommandBlinking), CircleShape)
            .semantics { contentDescription = description },
    )
}
