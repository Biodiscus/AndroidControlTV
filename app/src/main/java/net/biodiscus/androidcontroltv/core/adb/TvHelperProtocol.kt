package net.biodiscus.androidcontroltv.core.adb

import java.nio.charset.StandardCharsets
import java.util.Base64
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvSystemApp

internal const val HELPER_MAIN_CLASS_NAME = "net.biodiscus.androidcontroltv.tvhelper.TvHelperMain"
internal const val HELPER_APP_LINE_PREFIX = "APP"
internal const val HELPER_SYSTEM_APP_LINE_PREFIX = "SYSTEM_APP"
internal const val HELPER_FIELD_SEPARATOR = "\t"

private const val HELPER_APP_LINE_FIELD_COUNT = 5
private const val HELPER_SYSTEM_APP_LINE_FIELD_COUNT = 4

/** One line per app: APP, package, activity, base64 label, base64 PNG icon (empty when the icon is missing). */
internal fun parseTvHelperOutput(output: String): List<TvApp> =
    output.lines()
        .mapNotNull(::parseTvHelperAppLine)
        .distinctBy(TvApp::packageName)
        .sortedBy { app -> app.label.lowercase() }

private fun parseTvHelperAppLine(line: String): TvApp? {
    val fields = line.removeSuffix("\r").split(HELPER_FIELD_SEPARATOR)
    if (fields.size != HELPER_APP_LINE_FIELD_COUNT || fields[0] != HELPER_APP_LINE_PREFIX) return null

    val packageName = fields[1]
    val label = decodeLabel(fields[3]) ?: return null
    return TvApp(
        packageName = packageName,
        activityName = fields[2],
        label = label.ifBlank { packageName },
        iconPngBase64 = fields[4].ifEmpty { null },
    )
}

/** One line per system app: SYSTEM_APP, package, base64 label, base64 PNG icon (empty when the icon is missing). */
internal fun parseTvHelperSystemApps(output: String): List<TvSystemApp> =
    output.lines()
        .mapNotNull(::parseTvHelperSystemAppLine)
        .distinctBy(TvSystemApp::packageName)
        .sortedBy { app -> app.label.lowercase() }

private fun parseTvHelperSystemAppLine(line: String): TvSystemApp? {
    val fields = line.removeSuffix("\r").split(HELPER_FIELD_SEPARATOR)
    if (fields.size != HELPER_SYSTEM_APP_LINE_FIELD_COUNT || fields[0] != HELPER_SYSTEM_APP_LINE_PREFIX) return null

    val packageName = fields[1]
    val label = decodeLabel(fields[2]) ?: return null
    return TvSystemApp(
        packageName = packageName,
        label = label.ifBlank { packageName },
        iconPngBase64 = fields[3].ifEmpty { null },
    )
}

private fun decodeLabel(base64Label: String): String? =
    try {
        String(Base64.getDecoder().decode(base64Label), StandardCharsets.UTF_8)
    } catch (_: IllegalArgumentException) {
        null
    }
