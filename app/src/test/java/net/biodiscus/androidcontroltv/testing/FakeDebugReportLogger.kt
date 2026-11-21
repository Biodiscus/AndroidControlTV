package net.biodiscus.androidcontroltv.testing

import net.biodiscus.androidcontroltv.core.debug.DebugReportLogger

internal class FakeDebugReportLogger : DebugReportLogger {

    val loggedReports = mutableListOf<String>()

    override fun log(reportText: String) {
        loggedReports += reportText
    }
}
