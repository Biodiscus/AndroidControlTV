package net.biodiscus.androidcontroltv.feature.debug

import net.biodiscus.androidcontroltv.core.debug.DebugAction
import net.biodiscus.androidcontroltv.core.debug.DebugReport

internal data class DebugUiState(
    val actions: List<DebugAction>,
    val runningActionName: String? = null,
    val report: DebugReport? = null,
) {
    val isRunningAction: Boolean = runningActionName != null
}
