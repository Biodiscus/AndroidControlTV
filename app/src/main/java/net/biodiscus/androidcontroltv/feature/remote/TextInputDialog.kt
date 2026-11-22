package net.biodiscus.androidcontroltv.feature.remote

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

@Composable
internal fun TextInputDialog(onSend: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val textFieldFocus = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.remote_text_input_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.focusRequester(textFieldFocus),
                label = { Text(stringResource(R.string.remote_text_input_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (text.isNotEmpty()) onSend(text) }),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSend(text) }, enabled = text.isNotEmpty()) {
                Text(stringResource(R.string.remote_text_input_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.remote_text_input_cancel)) }
        },
    )

    ShowKeyboardWhenDialogOpens(textFieldFocus)
}

@Composable
private fun ShowKeyboardWhenDialogOpens(textFieldFocus: FocusRequester) {
    LaunchedEffect(Unit) { textFieldFocus.requestFocus() }
}

@ThemePreviews
@Composable
private fun TextInputDialogPreview() {
    AndroidControlTVTheme {
        TextInputDialog(onSend = {}, onDismiss = {})
    }
}
