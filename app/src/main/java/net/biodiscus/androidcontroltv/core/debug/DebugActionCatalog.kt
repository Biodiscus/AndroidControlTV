package net.biodiscus.androidcontroltv.core.debug

import javax.inject.Inject
import net.biodiscus.androidcontroltv.core.adb.HELPER_FILE_PREFIX
import net.biodiscus.androidcontroltv.core.adb.HELPER_SYSTEM_APP_LINE_PREFIX
import net.biodiscus.androidcontroltv.core.adb.LIST_LAUNCHABLE_APPS_COMMAND
import net.biodiscus.androidcontroltv.core.adb.LIST_SYSTEM_PACKAGES_COMMAND
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
        createSystemAppsCheck(),
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

    private fun createSystemAppsCheck() = DebugAction(
        name = "System apps",
        description = "Runs the helper that is on the TV now and counts the lines it prints per type, to see why system apps are missing.",
        steps = listOf(
            DebugStep.RunCommand("ls -l '${helperApk.remotePath}'", title = "Is the helper of this build on the TV"),
            DebugStep.ExpectOutputContains(HELPER_FILE_PREFIX),
            DebugStep.RunCommand("$LIST_SYSTEM_PACKAGES_COMMAND | wc -l", title = "How many system packages the TV has"),
            DebugStep.RunCommand(createCountHelperLinesCommand(), title = "Run the helper: lines per type and seconds it took"),
            DebugStep.ExpectOutputContains(HELPER_SYSTEM_APP_LINE_PREFIX, title = "Expect the helper to report system apps"),
        ),
    )

    // Only the first word of each line is kept, so the base64 icons are not printed. Error lines of the helper stay visible.
    private fun createCountHelperLinesCommand(): String {
        val runHelper = createRunHelperCommand(helperApk.remotePath)
        val countLinesPerType = "cut -f1 | cut -c1-80 | sort | uniq -c | head -n 20"
        return "start=\$(date +%s); $runHelper 2>&1 | $countLinesPerType; echo \"seconds: \$(( \$(date +%s) - start ))\""
    }
}
