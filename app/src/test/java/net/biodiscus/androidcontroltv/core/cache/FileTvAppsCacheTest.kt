package net.biodiscus.androidcontroltv.core.cache

import java.io.File
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.testing.createTvApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileTvAppsCacheTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val livingRoomTv = TvAddress("192.168.1.20", 5555)
    private val bedroomTv = TvAddress("192.168.1.30", 5555)

    private fun createCache() = FileTvAppsCache(File(temporaryFolder.root, "tv_apps"))

    @Test
    fun `nothing is cached at first`() = runTest {
        assertNull(createCache().read(livingRoomTv))
    }

    @Test
    fun `cached apps can be read back with their icon`() = runTest {
        val cache = createCache()
        val apps = listOf(TvApp("com.netflix.ninja", "com.netflix.ninja.Main", "Netflix", "aWNvbg=="), createTvApp())

        cache.write(livingRoomTv, apps)

        assertEquals(apps, cache.read(livingRoomTv))
    }

    @Test
    fun `an app without an icon is read back without an icon`() = runTest {
        val cache = createCache()

        cache.write(livingRoomTv, listOf(createTvApp()))

        assertNull(cache.read(livingRoomTv)?.single()?.iconPngBase64)
    }

    @Test
    fun `each TV has its own cached apps`() = runTest {
        val cache = createCache()
        cache.write(livingRoomTv, listOf(createTvApp(packageName = "living.room")))
        cache.write(bedroomTv, listOf(createTvApp(packageName = "bed.room")))

        assertEquals("living.room", cache.read(livingRoomTv)?.single()?.packageName)
        assertEquals("bed.room", cache.read(bedroomTv)?.single()?.packageName)
    }

    @Test
    fun `the same host on another port has its own cached apps`() = runTest {
        val cache = createCache()
        cache.write(livingRoomTv, listOf(createTvApp()))

        assertNull(cache.read(TvAddress("192.168.1.20", 5556)))
    }

    @Test
    fun `writing again replaces the cached apps`() = runTest {
        val cache = createCache()
        cache.write(livingRoomTv, listOf(createTvApp(packageName = "old.app")))

        cache.write(livingRoomTv, listOf(createTvApp(packageName = "new.app")))

        assertEquals(listOf("new.app"), cache.read(livingRoomTv)?.map(TvApp::packageName))
    }

    @Test
    fun `a corrupt cache file counts as nothing cached`() = runTest {
        val cache = createCache()
        cache.write(livingRoomTv, listOf(createTvApp()))
        File(temporaryFolder.root, "tv_apps").listFiles()!!.single().writeText("{ this is not json")

        assertNull(cache.read(livingRoomTv))
    }
}
