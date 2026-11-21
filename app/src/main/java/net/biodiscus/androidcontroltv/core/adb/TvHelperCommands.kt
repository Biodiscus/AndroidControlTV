package net.biodiscus.androidcontroltv.core.adb

internal const val HELPER_DIRECTORY = "/data/local/tmp"
internal const val HELPER_FILE_PREFIX = "androidcontroltv-helper-"
internal const val HELPER_INSTALLED_ANSWER = "installed"

internal fun createIsHelperInstalledCommand(remotePath: String): String =
    "[ -f '$remotePath' ] && echo $HELPER_INSTALLED_ANSWER || echo missing"

internal fun createRemoveOldHelpersCommand(): String = "rm -f $HELPER_DIRECTORY/$HELPER_FILE_PREFIX*.apk"

internal fun createMakeHelperReadOnlyCommand(remotePath: String): String = "chmod 444 '$remotePath'"

internal fun createRunHelperCommand(remotePath: String): String =
    "CLASSPATH='$remotePath' app_process / $HELPER_MAIN_CLASS_NAME"
