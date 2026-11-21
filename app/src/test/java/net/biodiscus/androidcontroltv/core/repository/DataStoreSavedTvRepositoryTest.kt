package net.biodiscus.androidcontroltv.core.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.model.TvAddress
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSavedTvRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun TestScope.createRepository(): DataStoreSavedTvRepository {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(temporaryFolder.root, "saved_tv.preferences_pb")
        }
        return DataStoreSavedTvRepository(dataStore)
    }

    @Test
    fun `no address is saved at first`() = runTest {
        val repository = createRepository()

        assertEquals(emptyList<TvAddress>(), repository.savedAddresses.first())
    }

    @Test
    fun `a saved address can be read back`() = runTest {
        val repository = createRepository()

        repository.saveAddress(TvAddress("192.168.1.20", 5556))

        assertEquals(listOf(TvAddress("192.168.1.20", 5556)), repository.savedAddresses.first())
    }

    @Test
    fun `a newly saved address is placed in front of the old one`() = runTest {
        val repository = createRepository()
        repository.saveAddress(TvAddress("192.168.1.20", 5555))

        repository.saveAddress(TvAddress("192.168.1.30", 5555))

        val expected = listOf(TvAddress("192.168.1.30", 5555), TvAddress("192.168.1.20", 5555))
        assertEquals(expected, repository.savedAddresses.first())
    }
}
