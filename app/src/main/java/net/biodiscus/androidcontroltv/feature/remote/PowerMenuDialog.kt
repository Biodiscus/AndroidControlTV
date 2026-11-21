package net.biodiscus.androidcontroltv.feature.remote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

@Composable
internal fun PowerMenuDialog(
    defaultAction: PowerAction?,
    onActionClick: (PowerAction) -> Unit,
    onDefaultChange: (PowerAction, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.power_menu_title)) },
        text = {
            Column {
                Text(text = stringResource(R.string.power_menu_default_hint), style = MaterialTheme.typography.bodyMedium)
                PowerAction.entries.forEach { action ->
                    PowerMenuItem(action, isDefault = action == defaultAction, onActionClick, onDefaultChange)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.power_menu_cancel)) }
        },
    )
}

@Composable
private fun PowerMenuItem(
    action: PowerAction,
    isDefault: Boolean,
    onActionClick: (PowerAction) -> Unit,
    onDefaultChange: (PowerAction, Boolean) -> Unit,
) {
    val label = stringResource(labelFor(action))
    val defaultDescription = stringResource(R.string.power_menu_use_as_default, label)

    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(imageVector = iconFor(action), contentDescription = null) },
        trailingContent = {
            Checkbox(
                checked = isDefault,
                onCheckedChange = { isChecked -> onDefaultChange(action, isChecked) },
                modifier = Modifier.semantics { contentDescription = defaultDescription },
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onActionClick(action) },
    )
}

@ThemePreviews
@Composable
private fun PowerMenuDialogPreview() {
    AndroidControlTVTheme {
        PowerMenuDialog(
            defaultAction = PowerAction.SLEEP,
            onActionClick = {},
            onDefaultChange = { _, _ -> },
            onDismiss = {},
        )
    }
}
