package net.biodiscus.androidcontroltv.core.model

import net.biodiscus.androidcontroltv.testing.createTvApp
import net.biodiscus.androidcontroltv.testing.createTvSystemApp
import org.junit.Assert.assertEquals
import org.junit.Test

class TvAppsStateTest {

    private val keptApp = createTvApp(packageName = "com.kept")
    private val removedApp = createTvApp(packageName = "com.removed")

    @Test
    fun `an app is taken out of the loaded apps`() {
        val state = TvAppsState.Loaded(listOf(keptApp, removedApp))

        assertEquals(TvAppsState.Loaded(listOf(keptApp)), state.withoutApp(removedApp))
    }

    @Test
    fun `an app is matched by its package name and not by its label`() {
        val sameAppWithOtherLabel = removedApp.copy(label = "Other label")
        val state = TvAppsState.Loaded(listOf(keptApp, removedApp))

        assertEquals(TvAppsState.Loaded(listOf(keptApp)), state.withoutApp(sameAppWithOtherLabel))
    }

    @Test
    fun `states without apps stay as they are`() {
        assertEquals(TvAppsState.Unavailable, TvAppsState.Unavailable.withoutApp(removedApp))
    }

    @Test
    fun `taking an app out of the loaded apps keeps the system apps`() {
        val systemApp = createTvSystemApp()
        val state = TvAppsState.Loaded(listOf(removedApp), listOf(systemApp))

        assertEquals(TvAppsState.Loaded(emptyList(), listOf(systemApp)), state.withoutApp(removedApp))
    }
}
