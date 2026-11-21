package net.biodiscus.androidcontroltv.core.debug

import javax.inject.Inject
import net.biodiscus.androidcontroltv.core.adb.LIST_LAUNCHABLE_APPS_COMMAND
import net.biodiscus.androidcontroltv.core.adb.TvHelperApk
import net.biodiscus.androidcontroltv.core.adb.createMakeHelperReadOnlyCommand
import net.biodiscus.androidcontroltv.core.adb.createRemoveOldHelpersCommand
import net.biodiscus.androidcontroltv.core.adb.createRunHelperCommand

/** Every debug action the debug screen offers. Add a new action here, see .claude/skills/android-debug-actions. */
internal class DebugActionCatalog @Inject constructor(
    private val helperApk: TvHelperApk,
) {

    val actions: List<DebugAction> = listOf(
        createConnectionCheck(),
        createTvInfo(),
        createLauncherAppsCheck(),
        createAppHelperCheck(),
    )

    private fun createConnectionCheck() = DebugAction(
        name = "Connection check",
        description = "Checks that commands reach the TV and come back.",
        steps = listOf(
            DebugStep.RunCommand("echo connection-ok"),
            DebugStep.ExpectOutputContains("connection-ok"),
        ),
    )

    private fun createTvInfo() = DebugAction(
        name = "TV info",
        description = "Reads the model, Android version and the user the commands run as.",
        steps = listOf(
            DebugStep.RunCommand("getprop ro.product.model"),
            DebugStep.RunCommand("getprop ro.build.version.release"),
            DebugStep.RunCommand("getprop ro.build.version.sdk"),
            DebugStep.RunCommand("id"),
        ),
    )

    private fun createLauncherAppsCheck() = DebugAction(
        name = "Launcher apps",
        description = "Asks the TV for the apps that have a TV launcher entry, the list the grid falls back to.",
        steps = listOf(
            DebugStep.RunCommand(LIST_LAUNCHABLE_APPS_COMMAND),
            DebugStep.ExpectOutputContains("/"),
        ),
    )

    private fun createAppHelperCheck() = DebugAction(
        name = "App helper",
        description = "Pushes the app helper to the TV again and runs it. The output shows why names and icons are missing.",
        steps = listOf(
            DebugStep.RunCommand(createRemoveOldHelpersCommand(), title = "Remove old helper files"),
            DebugStep.PushFile(helperApk.localFile, helperApk.remotePath),
            DebugStep.RunCommand(createMakeHelperReadOnlyCommand(helperApk.remotePath), title = "Make the helper read only"),
            DebugStep.RunCommand(createRunHelperCommand(helperApk.remotePath), title = "Run the helper on the TV"),
            DebugStep.ExpectOutputContains("APP\t", title = "Expect the helper to report apps"),
        ),
    )
}
