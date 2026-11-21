package net.biodiscus.androidcontroltv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.biodiscus.androidcontroltv.core.repository.SavedTvRepository

@HiltViewModel
internal class AndroidControlTVAppViewModel @Inject constructor(
    savedTvRepository: SavedTvRepository,
) : ViewModel() {

    private val mutableStartScreen = MutableStateFlow<StartScreen>(StartScreen.Loading)

    // Read once: when the saved TV changes later on, the screen that is open must not be replaced.
    val startScreen: StateFlow<StartScreen> = mutableStartScreen.asStateFlow()

    init {
        viewModelScope.launch {
            mutableStartScreen.value = createStartScreen(savedTvRepository.savedAddresses.first())
        }
    }
}
