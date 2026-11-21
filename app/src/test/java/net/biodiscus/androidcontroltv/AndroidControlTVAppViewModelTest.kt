package net.biodiscus.androidcontroltv

import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.testing.FakeSavedTvRepository
import net.biodiscus.androidcontroltv.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AndroidControlTVAppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `starts on the overview when no TV was saved`() {
        val viewModel = AndroidControlTVAppViewModel(FakeSavedTvRepository(initialAddresses = emptyList()))

        assertEquals(StartScreen.Overview, viewModel.startScreen.value)
    }

    @Test
    fun `starts on the remote when a TV was saved`() {
        val savedTvRepository = FakeSavedTvRepository(initialAddresses = listOf(TvAddress("192.168.1.20", 5555)))

        val viewModel = AndroidControlTVAppViewModel(savedTvRepository)

        assertEquals(StartScreen.Remote, viewModel.startScreen.value)
    }
}
