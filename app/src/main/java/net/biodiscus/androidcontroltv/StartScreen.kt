package net.biodiscus.androidcontroltv

import net.biodiscus.androidcontroltv.core.model.TvAddress

internal sealed interface StartScreen {
    data object Loading : StartScreen
    data object Overview : StartScreen
    data object Remote : StartScreen
}

internal fun createStartScreen(savedAddresses: List<TvAddress>): StartScreen =
    if (savedAddresses.isEmpty()) StartScreen.Overview else StartScreen.Remote
