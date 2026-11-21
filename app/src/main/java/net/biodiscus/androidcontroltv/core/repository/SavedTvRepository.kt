package net.biodiscus.androidcontroltv.core.repository

import kotlinx.coroutines.flow.Flow
import net.biodiscus.androidcontroltv.core.model.TvAddress

interface SavedTvRepository {
    /** The TVs that were connected before, the most recently connected one first. */
    val savedAddresses: Flow<List<TvAddress>>

    suspend fun saveAddress(address: TvAddress)
}
