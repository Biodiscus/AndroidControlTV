package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvApp

// The component is quoted because an activity name can contain a $ (inner class), which the shell would expand.
internal fun createLaunchAppCommand(app: TvApp): String =
    "am start -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER -n '${app.packageName}/${app.activityName}'"
