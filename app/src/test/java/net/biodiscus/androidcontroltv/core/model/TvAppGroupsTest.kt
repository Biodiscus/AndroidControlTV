package net.biodiscus.androidcontroltv.core.model

import net.biodiscus.androidcontroltv.testing.createTvApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvAppGroupsTest {

    private val netflix = createTvApp(packageName = "com.netflix", label = "Netflix")
    private val plex = createTvApp(packageName = "com.plex", label = "Plex")
    private val youtube = createTvApp(packageName = "com.youtube", label = "YouTube")

    @Test
    fun `favorite apps are split from the other apps`() {
        val groups = groupTvApps(listOf(netflix, plex, youtube), favoritePackageNames = setOf("com.plex"))

        assertEquals(listOf(plex), groups.favorites)
        assertEquals(listOf(netflix, youtube), groups.others)
    }

    @Test
    fun `an app is never in both groups`() {
        val groups = groupTvApps(listOf(netflix, plex), favoritePackageNames = setOf("com.netflix", "com.plex"))

        assertTrue(groups.others.isEmpty())
    }

    @Test
    fun `both groups keep the order of the apps`() {
        val groups = groupTvApps(listOf(youtube, netflix, plex), favoritePackageNames = setOf("com.plex", "com.youtube"))

        assertEquals(listOf(youtube, plex), groups.favorites)
    }

    @Test
    fun `without favorites all apps are in the other group`() {
        val groups = groupTvApps(listOf(netflix, plex), favoritePackageNames = emptySet())

        assertEquals(listOf(netflix, plex), groups.others)
        assertTrue(groups.favorites.isEmpty())
    }

    @Test
    fun `a favorite that is not on the TV is ignored`() {
        val groups = groupTvApps(listOf(netflix), favoritePackageNames = setOf("com.not.installed"))

        assertTrue(groups.favorites.isEmpty())
        assertEquals(listOf(netflix), groups.others)
    }

    @Test
    fun `groups without apps are empty`() {
        assertTrue(groupTvApps(emptyList(), setOf("com.plex")).isEmpty)
    }

    @Test
    fun `groups with apps are not empty`() {
        assertFalse(groupTvApps(listOf(netflix), emptySet()).isEmpty)
    }
}
