package net.biodiscus.androidcontroltv.core.repository

import kotlinx.coroutines.flow.StateFlow
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState

interface TvRepository {
    val connectionState: StateFlow<TvConnectionState>

    suspend fun connect(address: TvAddress)
    suspend fun disconnect()
    suspend fun sendKey(key: RemoteKey)
}
