package net.biodiscus.androidcontroltv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

private val RoundButtonSize = 64.dp
private val DirectionPadSize = 240.dp
private val DirectionPadCenterSize = 88.dp
private val DirectionArrowSize = 72.dp

@Composable
internal fun RemoteControl(onKeyClick: (RemoteKey) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        PowerAndMuteRow(onKeyClick)
        DirectionPad(onKeyClick)
        BackPlayHomeRow(onKeyClick)
        VolumeRocker(onKeyClick)
    }
}

@Composable
private fun PowerAndMuteRow(onKeyClick: (RemoteKey) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        RemoteRoundButton(
            key = RemoteKey.POWER,
            onKeyClick = onKeyClick,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ) {
            KeyIcon(RemoteKey.POWER, Icons.Rounded.PowerSettingsNew)
        }
        RemoteRoundButton(key = RemoteKey.MUTE, onKeyClick = onKeyClick) {
            KeyIcon(RemoteKey.MUTE, Icons.AutoMirrored.Rounded.VolumeOff)
        }
    }
}

@Composable
private fun DirectionPad(onKeyClick: (RemoteKey) -> Unit) {
    Box(
        modifier = Modifier
            .size(DirectionPadSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        DirectionArrow(RemoteKey.UP, rotationDegrees = 0f, Alignment.TopCenter, onKeyClick)
        DirectionArrow(RemoteKey.RIGHT, rotationDegrees = 90f, Alignment.CenterEnd, onKeyClick)
        DirectionArrow(RemoteKey.DOWN, rotationDegrees = 180f, Alignment.BottomCenter, onKeyClick)
        DirectionArrow(RemoteKey.LEFT, rotationDegrees = 270f, Alignment.CenterStart, onKeyClick)
        RemoteRoundButton(
            key = RemoteKey.SELECT,
            onKeyClick = onKeyClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            size = DirectionPadCenterSize,
        ) {
            Text(text = stringResource(labelFor(RemoteKey.SELECT)), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun BoxScope.DirectionArrow(
    key: RemoteKey,
    rotationDegrees: Float,
    alignment: Alignment,
    onKeyClick: (RemoteKey) -> Unit,
) {
    IconButton(
        onClick = { onKeyClick(key) },
        modifier = Modifier
            .align(alignment)
            .size(DirectionArrowSize),
    ) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowUp,
            contentDescription = stringResource(labelFor(key)),
            modifier = Modifier
                .size(40.dp)
                .rotate(rotationDegrees),
        )
    }
}

@Composable
private fun BackPlayHomeRow(onKeyClick: (RemoteKey) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        RemoteRoundButton(key = RemoteKey.BACK, onKeyClick = onKeyClick) {
            KeyIcon(RemoteKey.BACK, Icons.AutoMirrored.Rounded.ArrowBack)
        }
        RemoteRoundButton(key = RemoteKey.PLAY_PAUSE, onKeyClick = onKeyClick) {
            PlayPauseIcon()
        }
        RemoteRoundButton(key = RemoteKey.HOME, onKeyClick = onKeyClick) {
            KeyIcon(RemoteKey.HOME, Icons.Rounded.Home)
        }
    }
}

@Composable
private fun PlayPauseIcon() {
    val description = stringResource(labelFor(RemoteKey.PLAY_PAUSE))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = description, modifier = Modifier.size(20.dp))
        Icon(Icons.Rounded.Pause, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun VolumeRocker(onKeyClick: (RemoteKey) -> Unit) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VolumeButton(RemoteKey.VOLUME_DOWN, Icons.AutoMirrored.Rounded.VolumeDown, onKeyClick)
            VolumeButton(RemoteKey.VOLUME_UP, Icons.AutoMirrored.Rounded.VolumeUp, onKeyClick)
        }
    }
}

@Composable
private fun VolumeButton(key: RemoteKey, icon: ImageVector, onKeyClick: (RemoteKey) -> Unit) {
    IconButton(onClick = { onKeyClick(key) }, modifier = Modifier.size(RoundButtonSize)) {
        KeyIcon(key, icon)
    }
}

@Composable
private fun KeyIcon(key: RemoteKey, icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = stringResource(labelFor(key)))
}

@Composable
private fun RemoteRoundButton(
    key: RemoteKey,
    onKeyClick: (RemoteKey) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: Dp = RoundButtonSize,
    content: @Composable () -> Unit,
) {
    FilledIconButton(
        onClick = { onKeyClick(key) },
        modifier = Modifier.size(size),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        content = content,
    )
}

@ThemePreviews
@Composable
private fun RemoteControlPreview() {
    AndroidControlTVTheme {
        Surface {
            RemoteControl(onKeyClick = {}, modifier = Modifier.padding(16.dp))
        }
    }
}
