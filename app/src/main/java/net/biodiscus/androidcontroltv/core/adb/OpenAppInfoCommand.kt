package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvSystemApp

internal fun createOpenAppInfoCommand(app: TvSystemApp): String =
    "am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:${app.packageName}"
