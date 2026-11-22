package net.biodiscus.androidcontroltv.feature.remote

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

@Composable
internal fun UninstallAppDialog(app: TvApp, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.remote_uninstall_title, app.label)) },
        text = { Text(stringResource(R.string.remote_uninstall_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.remote_uninstall_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.remote_uninstall_cancel)) }
        },
    )
}

@ThemePreviews
@Composable
private fun UninstallAppDialogPreview() {
    AndroidControlTVTheme {
        UninstallAppDialog(
            app = TvApp(packageName = "com.netflix.ninja", activityName = "com.netflix.ninja.MainActivity", label = "Netflix"),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
