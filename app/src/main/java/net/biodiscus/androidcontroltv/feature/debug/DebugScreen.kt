package net.biodiscus.androidcontroltv.feature.debug

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.debug.DebugAction
import net.biodiscus.androidcontroltv.core.debug.DebugReport
import net.biodiscus.androidcontroltv.core.debug.DebugStep
import net.biodiscus.androidcontroltv.core.debug.DebugStepResult
import net.biodiscus.androidcontroltv.core.debug.StepOutcome
import net.biodiscus.androidcontroltv.ui.ThemePreviews
import net.biodiscus.androidcontroltv.ui.theme.AndroidControlTVTheme

@Composable
internal fun DebugRoute(
    onBackClick: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DebugScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRunClick = viewModel::onRunClick,
    )
}

@Composable
internal fun DebugScreen(
    uiState: DebugUiState,
    onBackClick: () -> Unit,
    onRunClick: (DebugAction) -> Unit,
) {
    Scaffold(topBar = { DebugTopBar(onBackClick) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.actions, key = DebugAction::name) { action ->
                DebugActionCard(
                    action = action,
                    isRunning = uiState.runningActionName == action.name,
                    canRun = !uiState.isRunningAction,
                    onRunClick = { onRunClick(action) },
                )
            }
            val report = uiState.report
            if (report != null) {
                item { DebugReportCard(report) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.debug_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.debug_back),
                )
            }
        },
    )
}

@Composable
private fun DebugActionCard(action: DebugAction, isRunning: Boolean, canRun: Boolean, onRunClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = action.name, style = MaterialTheme.typography.titleMedium)
            Text(text = action.description, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRunClick, enabled = canRun) {
                Text(stringResource(if (isRunning) R.string.debug_running else R.string.debug_run))
            }
        }
    }
}

@Composable
private fun DebugReportCard(report: DebugReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.debug_report_title), style = MaterialTheme.typography.titleMedium)
            SelectionContainer {
                Text(text = report.toText(), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
            CopyReportButton(report)
        }
    }
}

@Composable
private fun CopyReportButton(report: DebugReport) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val reportLabel = stringResource(R.string.debug_report_title)
    val copiedMessage = stringResource(R.string.debug_copied)

    OutlinedButton(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(reportLabel, report.toText())))
                Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
            }
        },
    ) {
        Text(stringResource(R.string.debug_copy))
    }
}

private val previewActions = listOf(
    DebugAction("Connection check", "Checks that commands reach the TV and come back.", listOf(DebugStep.RunCommand("echo connection-ok"))),
    DebugAction("TV info", "Reads the model and the Android version.", listOf(DebugStep.RunCommand("getprop ro.product.model"))),
)

private val previewReport = DebugReport(
    actionName = "TV info",
    stepResults = listOf(
        DebugStepResult("getprop ro.product.model", StepOutcome.PASSED, "Chromecast"),
        DebugStepResult("getprop ro.build.version.release", StepOutcome.FAILED, "exit code 1"),
        DebugStepResult("id", StepOutcome.SKIPPED),
    ),
)

private class DebugUiStatePreviewProvider : PreviewParameterProvider<DebugUiState> {
    override val values = sequenceOf(
        DebugUiState(actions = previewActions),
        DebugUiState(actions = previewActions, report = previewReport),
        DebugUiState(actions = previewActions, runningActionName = "TV info"),
    )
}

@ThemePreviews
@Composable
private fun DebugScreenPreview(@PreviewParameter(DebugUiStatePreviewProvider::class) uiState: DebugUiState) {
    AndroidControlTVTheme {
        DebugScreen(uiState = uiState, onBackClick = {}, onRunClick = {})
    }
}
