package net.biodiscus.androidcontroltv.core.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.model.PowerAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStorePowerSettingsRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun TestScope.createRepository(): DataStorePowerSettingsRepository {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(temporaryFolder.root, "settings.preferences_pb")
        }
        return DataStorePowerSettingsRepository(dataStore)
    }

    @Test
    fun `there is no default power action at first`() = runTest {
        val repository = createRepository()

        assertNull(repository.defaultPowerAction.first())
    }

    @Test
    fun `a default power action can be read back`() = runTest {
        val repository = createRepository()

        repository.setDefaultPowerAction(PowerAction.SLEEP)

        assertEquals(PowerAction.SLEEP, repository.defaultPowerAction.first())
    }

    @Test
    fun `a new default power action replaces the old one`() = runTest {
        val repository = createRepository()
        repository.setDefaultPowerAction(PowerAction.SLEEP)

        repository.setDefaultPowerAction(PowerAction.REBOOT)

        assertEquals(PowerAction.REBOOT, repository.defaultPowerAction.first())
    }

    @Test
    fun `clearing the default power action removes it`() = runTest {
        val repository = createRepository()
        repository.setDefaultPowerAction(PowerAction.SLEEP)

        repository.setDefaultPowerAction(null)

        assertNull(repository.defaultPowerAction.first())
    }
}
