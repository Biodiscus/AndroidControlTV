package net.biodiscus.androidcontroltv.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.addAsMostRecent
import net.biodiscus.androidcontroltv.core.repository.SavedTvRepository

class FakeSavedTvRepository(initialAddresses: List<TvAddress> = emptyList()) : SavedTvRepository {

    private val mutableSavedAddresses = MutableStateFlow(initialAddresses)
    override val savedAddresses: Flow<List<TvAddress>> = mutableSavedAddresses

    override suspend fun saveAddress(address: TvAddress) {
        mutableSavedAddresses.value = addAsMostRecent(mutableSavedAddresses.value, address)
    }
}
