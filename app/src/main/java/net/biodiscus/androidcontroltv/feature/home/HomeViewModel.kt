package net.biodiscus.androidcontroltv.feature.home

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
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.repository.TvRepository

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val tvRepository: TvRepository,
) : ViewModel() {

    private val addressInput = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        addressInput,
        tvRepository.connectionState,
        ::createHomeUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = createHomeUiState(addressInput.value, tvRepository.connectionState.value),
    )

    fun onAddressChange(newAddressInput: String) {
        addressInput.value = newAddressInput
    }

    fun onConnectClick() {
        val address = TvAddress.parse(addressInput.value) ?: return
        viewModelScope.launch { tvRepository.connect(address) }
    }

    fun onDisconnectClick() {
        viewModelScope.launch { tvRepository.disconnect() }
    }

    fun onKeyClick(key: RemoteKey) {
        viewModelScope.launch { tvRepository.sendKey(key) }
    }
}
