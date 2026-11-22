package net.biodiscus.androidcontroltv.core.adb

import java.io.IOException
import javax.inject.Inject
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvSystemApp

internal data class LoadedTvApps(
    val apps: List<TvApp>,
    val hasNamesAndIcons: Boolean,
    val systemApps: List<TvSystemApp> = emptyList(),
)

internal interface TvAppLoader {
    suspend fun load(shell: TvShell): LoadedTvApps
}

internal class AdbTvAppLoader @Inject constructor(
    private val helperApk: TvHelperApk,
) : TvAppLoader {

    override suspend fun load(shell: TvShell): LoadedTvApps =
        try {
            loadWithHelper(shell)
        } catch (_: IOException) {
            // The helper relies on hidden Android APIs and may not work on every TV. The plain list still lets the user launch apps.
            loadFromPlainLists(shell)
        }

    private suspend fun loadWithHelper(shell: TvShell): LoadedTvApps {
        installHelperIfMissing(shell)
        val helperOutput = shell.runCommand(createRunHelperCommand(helperApk.remotePath))
        val apps = parseTvHelperOutput(helperOutput)
        if (apps.isEmpty()) throw TvCommandFailedException("The helper on the TV reported no apps")
        return LoadedTvApps(apps, hasNamesAndIcons = true, systemApps = parseTvHelperSystemApps(helperOutput))
    }

    private suspend fun installHelperIfMissing(shell: TvShell) {
        val remotePath = helperApk.remotePath
        val answer = shell.runCommand(createIsHelperInstalledCommand(remotePath)).trim()
        if (answer == HELPER_INSTALLED_ANSWER) return

        shell.runCommand(createRemoveOldHelpersCommand())
        shell.pushFile(helperApk.localFile, remotePath)
        shell.runCommand(createMakeHelperReadOnlyCommand(remotePath))
    }

    private suspend fun loadFromPlainLists(shell: TvShell): LoadedTvApps {
        val apps = parseLaunchableApps(shell.runCommand(LIST_LAUNCHABLE_APPS_COMMAND))
        val systemApps = parseSystemPackages(shell.runCommand(LIST_SYSTEM_PACKAGES_COMMAND))
        return LoadedTvApps(apps, hasNamesAndIcons = false, systemApps = systemApps)
    }
}
