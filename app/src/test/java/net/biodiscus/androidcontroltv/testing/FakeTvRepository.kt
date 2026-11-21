package net.biodiscus.androidcontroltv.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.core.repository.TvRepository

class FakeTvRepository : TvRepository {

    private val mutableConnectionState = MutableStateFlow<TvConnectionState>(TvConnectionState.Disconnected)
    override val connectionState: StateFlow<TvConnectionState> = mutableConnectionState

    val connectedAddresses = mutableListOf<TvAddress>()
    val sentKeys = mutableListOf<RemoteKey>()
    var disconnectCount = 0
        private set

    fun setConnectionState(state: TvConnectionState) {
        mutableConnectionState.value = state
    }

    override suspend fun connect(address: TvAddress) {
        connectedAddresses += address
    }

    override suspend fun disconnect() {
        disconnectCount++
    }

    override suspend fun sendKey(key: RemoteKey) {
        sentKeys += key
    }
}
