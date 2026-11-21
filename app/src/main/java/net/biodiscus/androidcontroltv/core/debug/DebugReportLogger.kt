package net.biodiscus.androidcontroltv.core.debug

import android.util.Log
import javax.inject.Inject

private const val LOG_TAG = "DebugAction"

internal fun interface DebugReportLogger {
    fun log(reportText: String)
}

// Logcat has no limit per line that matters here, but one call per line keeps long reports readable in Android Studio.
internal class LogcatDebugReportLogger @Inject constructor() : DebugReportLogger {

    override fun log(reportText: String) {
        reportText.lines().forEach { line -> Log.i(LOG_TAG, line) }
    }
}
