package net.biodiscus.androidcontroltv.feature.debug

import net.biodiscus.androidcontroltv.core.debug.DebugActionCatalog
import net.biodiscus.androidcontroltv.core.debug.DebugReport
import net.biodiscus.androidcontroltv.core.debug.DebugStepResult
import net.biodiscus.androidcontroltv.core.debug.StepOutcome
import net.biodiscus.androidcontroltv.testing.FakeDebugReportLogger
import net.biodiscus.androidcontroltv.testing.FakeTvHelperApk
import net.biodiscus.androidcontroltv.testing.FakeTvRepository
import net.biodiscus.androidcontroltv.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class DebugViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tvRepository = FakeTvRepository()
    private val reportLogger = FakeDebugReportLogger()
    private val catalog = DebugActionCatalog(FakeTvHelperApk())
    private val viewModel = DebugViewModel(tvRepository, reportLogger, catalog)

    private val report = DebugReport("Connection check", listOf(DebugStepResult("echo", StepOutcome.PASSED, "ok")))

    @Test
    fun `the ui state lists the actions of the catalog`() {
        assertEquals(catalog.actions, viewModel.uiState.value.actions)
    }

    @Test
    fun `no report is shown before an action has run`() {
        assertNull(viewModel.uiState.value.report)
    }

    @Test
    fun `running an action runs it on the TV`() {
        val action = catalog.actions.first()

        viewModel.onRunClick(action)

        assertEquals(listOf(action), tvRepository.ranDebugActions)
    }

    @Test
    fun `the report of an action is shown`() {
        tvRepository.debugReport = report

        viewModel.onRunClick(catalog.actions.first())

        assertEquals(report, viewModel.uiState.value.report)
    }

    @Test
    fun `the report of an action is written to the log`() {
        tvRepository.debugReport = report

        viewModel.onRunClick(catalog.actions.first())

        assertEquals(listOf(report.toText()), reportLogger.loggedReports)
    }

    @Test
    fun `no action is marked as running once it has finished`() {
        viewModel.onRunClick(catalog.actions.first())

        assertNull(viewModel.uiState.value.runningActionName)
        assertFalse(viewModel.uiState.value.isRunningAction)
    }

    @Test
    fun `a newer report replaces the older one`() {
        tvRepository.debugReport = report
        viewModel.onRunClick(catalog.actions.first())
        val newerReport = DebugReport("TV info", emptyList())
        tvRepository.debugReport = newerReport

        viewModel.onRunClick(catalog.actions.last())

        assertEquals(newerReport, viewModel.uiState.value.report)
    }
}
