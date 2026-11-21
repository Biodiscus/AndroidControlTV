package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvApp

internal const val LIST_LAUNCHABLE_APPS_COMMAND =
    "cmd package query-activities --components -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER"

internal fun parseLaunchableApps(commandOutput: String): List<TvApp> =
    commandOutput.lines()
        .map(String::trim)
        .filter { line -> line.contains('/') }
        .map(::createAppFromComponentName)
        .distinctBy(TvApp::packageName)
        .sortedBy(TvApp::label)

private fun createAppFromComponentName(componentName: String): TvApp {
    val packageName = componentName.substringBefore('/')
    return TvApp(packageName = packageName, activityName = componentName.substringAfter('/'), label = packageName)
}
