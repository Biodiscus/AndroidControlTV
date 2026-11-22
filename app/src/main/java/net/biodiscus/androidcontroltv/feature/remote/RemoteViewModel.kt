package net.biodiscus.androidcontroltv.feature.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.biodiscus.androidcontroltv.core.model.AppUninstallResult
import net.biodiscus.androidcontroltv.core.model.PowerAction
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.core.model.TvSystemApp
import net.biodiscus.androidcontroltv.core.model.withoutApp
import net.biodiscus.androidcontroltv.core.repository.FavoriteAppsRepository
import net.biodiscus.androidcontroltv.core.repository.PowerSettingsRepository
import net.biodiscus.androidcontroltv.core.repository.SavedTvRepository
import net.biodiscus.androidcontroltv.core.repository.TvRepository

internal val CommandBlinkDuration = 500.milliseconds

@HiltViewModel
internal class RemoteViewModel @Inject constructor(
    private val tvRepository: TvRepository,
    private val savedTvRepository: SavedTvRepository,
    private val powerSettingsRepository: PowerSettingsRepository,
    private val favoriteAppsRepository: FavoriteAppsRepository,
) : ViewModel() {

    private val tvApps = MutableStateFlow<TvAppsState>(TvAppsState.Loading)
    private val isCommandBlinking = MutableStateFlow(false)
    private var commandBlinkJob: Job? = null
    private val messageChannel = Channel<RemoteMessage>(Channel.BUFFERED)

    val messages: Flow<RemoteMessage> = messageChannel.receiveAsFlow()

    val uiState: StateFlow<RemoteUiState> = combine(
        tvRepository.connectionState,
        powerSettingsRepository.defaultPowerAction,
        tvApps,
        favoriteAppsRepository.favoritePackageNames,
        isCommandBlinking,
        ::RemoteUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RemoteUiState(tvRepository.connectionState.value),
    )

    init {
        viewModelScope.launch { connectToSavedTvWhenNotConnectedYet() }
        viewModelScope.launch { loadAppsEachTimeTheTvIsConnected() }
    }

    fun onRetryClick() {
        viewModelScope.launch { connectToSavedTv() }
    }

    fun onKeyClick(key: RemoteKey) {
        viewModelScope.launch {
            if (key == RemoteKey.POWER) pressPowerButton() else sendKey(key)
        }
    }

    fun onTextSend(text: String) {
        if (text.isEmpty()) return
        viewModelScope.launch {
            if (tvRepository.sendText(text)) signalCommandAccepted()
        }
    }

    fun onAppFavoriteChange(app: TvApp, isFavorite: Boolean) {
        viewModelScope.launch {
            favoriteAppsRepository.setFavorite(app.packageName, isFavorite)
        }
    }

    fun onAppClick(app: TvApp) {
        viewModelScope.launch {
            if (tvRepository.launchApp(app)) signalCommandAccepted()
        }
    }

    fun onSystemAppClick(app: TvSystemApp) {
        viewModelScope.launch {
            if (tvRepository.openSystemAppInfo(app)) signalCommandAccepted()
        }
    }

    fun onAppUninstallConfirm(app: TvApp) {
        viewModelScope.launch {
            when (tvRepository.uninstallApp(app)) {
                AppUninstallResult.UNINSTALLED -> {
                    tvApps.update { state -> state.withoutApp(app) }
                    signalCommandAccepted()
                }
                AppUninstallResult.REFUSED -> messageChannel.send(RemoteMessage.AppUninstallRefused(app))
                AppUninstallResult.NOT_SENT -> Unit
            }
        }
    }

    fun onPowerActionClick(action: PowerAction) {
        viewModelScope.launch { sendPowerAction(action) }
    }

    fun onDefaultPowerActionChange(action: PowerAction, isDefault: Boolean) {
        viewModelScope.launch {
            powerSettingsRepository.setDefaultPowerAction(if (isDefault) action else null)
        }
    }

    fun onBackClick(onDisconnected: () -> Unit) {
        viewModelScope.launch {
            tvRepository.disconnect()
            onDisconnected()
        }
    }

    private suspend fun loadAppsEachTimeTheTvIsConnected() {
        tvRepository.connectionState
            .filterIsInstance<TvConnectionState.Connected>()
            .collectLatest {
                tvRepository.loadApps().collect { state -> tvApps.value = state }
            }
    }

    private suspend fun pressPowerButton() {
        val defaultAction = powerSettingsRepository.defaultPowerAction.first()
        if (defaultAction == null) {
            sendKey(RemoteKey.POWER)
        } else {
            sendPowerAction(defaultAction)
        }
    }

    private suspend fun sendKey(key: RemoteKey) {
        if (tvRepository.sendKey(key)) signalCommandAccepted()
    }

    private suspend fun sendPowerAction(action: PowerAction) {
        if (tvRepository.sendPowerAction(action)) signalCommandAccepted()
    }

    // A new command restarts the blink, so a quick series of presses keeps the indicator blue until the last one.
    private fun signalCommandAccepted() {
        commandBlinkJob?.cancel()
        commandBlinkJob = viewModelScope.launch {
            isCommandBlinking.value = true
            delay(CommandBlinkDuration)
            isCommandBlinking.value = false
        }
    }

    private suspend fun connectToSavedTvWhenNotConnectedYet() {
        if (tvRepository.connectionState.value !is TvConnectionState.Disconnected) return
        connectToSavedTv()
    }

    private suspend fun connectToSavedTv() {
        val address = savedTvRepository.savedAddresses.first().firstOrNull() ?: return
        tvRepository.connect(address)
    }
}
