package net.biodiscus.androidcontroltv.feature.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.biodiscus.androidcontroltv.core.debug.DebugAction
import net.biodiscus.androidcontroltv.core.debug.DebugActionCatalog
import net.biodiscus.androidcontroltv.core.debug.DebugReportLogger
import net.biodiscus.androidcontroltv.core.repository.TvRepository

@HiltViewModel
internal class DebugViewModel @Inject constructor(
    private val tvRepository: TvRepository,
    private val reportLogger: DebugReportLogger,
    catalog: DebugActionCatalog,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(DebugUiState(actions = catalog.actions))
    val uiState: StateFlow<DebugUiState> = mutableUiState.asStateFlow()

    fun onRunClick(action: DebugAction) {
        if (mutableUiState.value.isRunningAction) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(runningActionName = action.name) }
            val report = tvRepository.runDebugAction(action)
            reportLogger.log(report.toText())
            mutableUiState.update { it.copy(runningActionName = null, report = report) }
        }
    }
}
