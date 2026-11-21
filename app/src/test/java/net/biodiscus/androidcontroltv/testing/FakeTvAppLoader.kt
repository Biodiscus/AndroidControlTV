package net.biodiscus.androidcontroltv.testing

import java.io.IOException
import net.biodiscus.androidcontroltv.core.adb.LoadedTvApps
import net.biodiscus.androidcontroltv.core.adb.TvAppLoader
import net.biodiscus.androidcontroltv.core.adb.TvShell
import net.biodiscus.androidcontroltv.core.model.TvApp

internal class FakeTvAppLoader : TvAppLoader {

    var apps: List<TvApp> = emptyList()
    var hasNamesAndIcons = true
    val loadedWithShells = mutableListOf<TvShell>()
    var error: IOException? = null
    var errorOnFirstLoad: IOException? = null

    override suspend fun load(shell: TvShell): LoadedTvApps {
        loadedWithShells += shell
        val firstLoadError = errorOnFirstLoad
        if (firstLoadError != null && loadedWithShells.size == 1) throw firstLoadError
        val loadError = error
        if (loadError != null) throw loadError
        return LoadedTvApps(apps, hasNamesAndIcons)
    }
}
