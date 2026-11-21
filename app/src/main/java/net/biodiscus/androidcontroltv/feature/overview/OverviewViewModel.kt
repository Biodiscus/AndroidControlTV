package net.biodiscus.androidcontroltv.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.core.repository.SavedTvRepository
import net.biodiscus.androidcontroltv.core.repository.TvRepository

@HiltViewModel
internal class OverviewViewModel @Inject constructor(
    private val tvRepository: TvRepository,
    private val savedTvRepository: SavedTvRepository,
) : ViewModel() {

    private val addressInput = MutableStateFlow("")

    val uiState: StateFlow<OverviewUiState> = combine(
        addressInput,
        tvRepository.connectionState,
        savedTvRepository.savedAddresses,
        ::createOverviewUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = createOverviewUiState(
            addressInput = addressInput.value,
            connectionState = tvRepository.connectionState.value,
            savedAddresses = emptyList(),
        ),
    )

    fun onAddressChange(newAddressInput: String) {
        addressInput.value = newAddressInput
    }

    fun onConnectClick(onConnected: () -> Unit) {
        val address = TvAddress.parse(addressInput.value) ?: return
        connectAndSave(address, onConnected)
    }

    fun onSavedTvClick(address: TvAddress, onConnected: () -> Unit) {
        connectAndSave(address, onConnected)
    }

    private fun connectAndSave(address: TvAddress, onConnected: () -> Unit) {
        viewModelScope.launch {
            tvRepository.connect(address)
            if (isConnected()) {
                savedTvRepository.saveAddress(address)
                onConnected()
            }
        }
    }

    private fun isConnected(): Boolean = tvRepository.connectionState.value is TvConnectionState.Connected
}
