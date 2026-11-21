package net.biodiscus.androidcontroltv.core.adb

import java.io.IOException
import javax.inject.Inject
import net.biodiscus.androidcontroltv.core.model.TvApp

internal data class LoadedTvApps(val apps: List<TvApp>, val hasNamesAndIcons: Boolean)

internal interface TvAppLoader {
    suspend fun load(shell: TvShell): LoadedTvApps
}

internal class AdbTvAppLoader @Inject constructor(
    private val helperApk: TvHelperApk,
) : TvAppLoader {

    override suspend fun load(shell: TvShell): LoadedTvApps =
        try {
            LoadedTvApps(loadWithHelper(shell), hasNamesAndIcons = true)
        } catch (_: IOException) {
            // The helper relies on hidden Android APIs and may not work on every TV. The plain list still lets the user launch apps.
            LoadedTvApps(loadFromLauncherQuery(shell), hasNamesAndIcons = false)
        }

    private suspend fun loadWithHelper(shell: TvShell): List<TvApp> {
        installHelperIfMissing(shell)
        val apps = parseTvHelperOutput(shell.runCommand(createRunHelperCommand(helperApk.remotePath)))
        if (apps.isEmpty()) throw TvCommandFailedException("The helper on the TV reported no apps")
        return apps
    }

    private suspend fun installHelperIfMissing(shell: TvShell) {
        val remotePath = helperApk.remotePath
        val answer = shell.runCommand(createIsHelperInstalledCommand(remotePath)).trim()
        if (answer == HELPER_INSTALLED_ANSWER) return

        shell.runCommand(createRemoveOldHelpersCommand())
        shell.pushFile(helperApk.localFile, remotePath)
        shell.runCommand(createMakeHelperReadOnlyCommand(remotePath))
    }

    private suspend fun loadFromLauncherQuery(shell: TvShell): List<TvApp> =
        parseLaunchableApps(shell.runCommand(LIST_LAUNCHABLE_APPS_COMMAND))
}
