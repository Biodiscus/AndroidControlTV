package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvApp

internal fun createUninstallAppCommand(app: TvApp): String = "pm uninstall ${app.packageName}"
