package net.biodiscus.androidcontroltv.core.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugReportTest {

    @Test
    fun `a report with only passed steps has passed`() {
        val report = DebugReport("Check", listOf(DebugStepResult("a", StepOutcome.PASSED)))

        assertTrue(report.hasPassed)
    }

    @Test
    fun `the text starts with the action name and the result`() {
        val report = DebugReport("Check", listOf(DebugStepResult("a", StepOutcome.PASSED)))

        assertEquals(listOf("Debug action: Check", "Result: PASSED"), report.toText().lines().take(2))
    }

    @Test
    fun `the text says FAILED when a step failed`() {
        val report = DebugReport("Check", listOf(DebugStepResult("a", StepOutcome.FAILED, "boom")))

        assertEquals("Result: FAILED", report.toText().lines()[1])
    }

    @Test
    fun `the steps are numbered with their outcome`() {
        val report = DebugReport(
            "Check",
            listOf(DebugStepResult("first", StepOutcome.PASSED), DebugStepResult("second", StepOutcome.SKIPPED)),
        )

        assertEquals(listOf("1. [PASSED] first", "2. [SKIPPED] second"), report.toText().lines().drop(2))
    }

    @Test
    fun `the output of a step is shown indented under it`() {
        val report = DebugReport("Check", listOf(DebugStepResult("model", StepOutcome.PASSED, "Chromecast\nAndroid 14")))

        assertEquals(listOf("1. [PASSED] model", "   Chromecast", "   Android 14"), report.toText().lines().drop(2))
    }

    @Test
    fun `blank output is left out`() {
        val report = DebugReport("Check", listOf(DebugStepResult("quiet", StepOutcome.PASSED, "  \n ")))

        assertEquals(listOf("1. [PASSED] quiet"), report.toText().lines().drop(2))
    }

    @Test
    fun `very long output is cut short and says how much is hidden`() {
        val report = DebugReport("Check", listOf(DebugStepResult("long", StepOutcome.PASSED, "x".repeat(2500))))

        val lines = report.toText().lines()
        assertEquals("   " + "x".repeat(2000), lines[3])
        assertEquals("   … (500 more characters)", lines[4])
    }

    @Test
    fun `output that fits is not cut`() {
        val report = DebugReport("Check", listOf(DebugStepResult("fits", StepOutcome.PASSED, "x".repeat(2000))))

        assertEquals(4, report.toText().lines().size)
    }

    @Test
    fun `the report for a missing connection has one failed step`() {
        val action = DebugAction("Check", "A check", emptyList())

        val report = createNotConnectedReport(action)

        assertEquals(listOf(StepOutcome.FAILED), report.stepResults.map(DebugStepResult::outcome))
        assertEquals("Check", report.actionName)
    }
}
