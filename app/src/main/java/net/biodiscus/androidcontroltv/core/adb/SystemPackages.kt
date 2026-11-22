package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvSystemApp

internal const val LIST_SYSTEM_PACKAGES_COMMAND = "pm list packages -s"

private const val PACKAGE_LINE_PREFIX = "package:"

/** The plain list has no real names and icons, so each system app is labelled with its package name. */
internal fun parseSystemPackages(commandOutput: String): List<TvSystemApp> =
    commandOutput.lines()
        .map(String::trim)
        .filter { line -> line.startsWith(PACKAGE_LINE_PREFIX) }
        .map { line -> line.removePrefix(PACKAGE_LINE_PREFIX) }
        .filter(String::isNotEmpty)
        .distinct()
        .sorted()
        .map { packageName -> TvSystemApp(packageName = packageName, label = packageName) }
