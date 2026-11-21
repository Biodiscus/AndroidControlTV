package net.biodiscus.androidcontroltv.core.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreFavoriteAppsRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun TestScope.createRepository(): DataStoreFavoriteAppsRepository {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(temporaryFolder.root, "settings.preferences_pb")
        }
        return DataStoreFavoriteAppsRepository(dataStore)
    }

    @Test
    fun `there are no favorites at first`() = runTest {
        assertEquals(emptySet<String>(), createRepository().favoritePackageNames.first())
    }

    @Test
    fun `a favorite can be read back`() = runTest {
        val repository = createRepository()

        repository.setFavorite("com.netflix", isFavorite = true)

        assertEquals(setOf("com.netflix"), repository.favoritePackageNames.first())
    }

    @Test
    fun `favorites are added to the existing ones`() = runTest {
        val repository = createRepository()
        repository.setFavorite("com.netflix", isFavorite = true)

        repository.setFavorite("com.plex", isFavorite = true)

        assertEquals(setOf("com.netflix", "com.plex"), repository.favoritePackageNames.first())
    }

    @Test
    fun `a favorite can be removed`() = runTest {
        val repository = createRepository()
        repository.setFavorite("com.netflix", isFavorite = true)
        repository.setFavorite("com.plex", isFavorite = true)

        repository.setFavorite("com.netflix", isFavorite = false)

        assertEquals(setOf("com.plex"), repository.favoritePackageNames.first())
    }

    @Test
    fun `removing an app that is not a favorite changes nothing`() = runTest {
        val repository = createRepository()
        repository.setFavorite("com.netflix", isFavorite = true)

        repository.setFavorite("com.plex", isFavorite = false)

        assertEquals(setOf("com.netflix"), repository.favoritePackageNames.first())
    }

    @Test
    fun `adding the same favorite twice keeps it once`() = runTest {
        val repository = createRepository()
        repository.setFavorite("com.netflix", isFavorite = true)

        repository.setFavorite("com.netflix", isFavorite = true)

        assertEquals(setOf("com.netflix"), repository.favoritePackageNames.first())
    }
}
