package net.biodiscus.androidcontroltv.core.debug

import java.io.File
import java.io.IOException
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.adb.TvCommandFailedException
import net.biodiscus.androidcontroltv.testing.FakeTvShell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DebugActionRunnerTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val shell = FakeTvShell()
    private val runner = DebugActionRunner(shell)

    private fun actionWith(vararg steps: DebugStep) = DebugAction("Test action", "A test action", steps.toList())

    private fun outcomesOf(report: DebugReport) = report.stepResults.map(DebugStepResult::outcome)

    @Test
    fun `a command that works passes and records its output`() = runTest {
        shell.respondTo("getprop ro.product.model", "Chromecast")

        val report = runner.run(actionWith(DebugStep.RunCommand("getprop ro.product.model")))

        assertEquals(listOf(DebugStepResult("getprop ro.product.model", StepOutcome.PASSED, "Chromecast")), report.stepResults)
    }

    @Test
    fun `a step is titled with its command unless it has its own title`() = runTest {
        val report = runner.run(actionWith(DebugStep.RunCommand("id", title = "Who am I")))

        assertEquals("Who am I", report.stepResults.single().title)
    }

    @Test
    fun `a command that the TV refuses fails with the reason`() = runTest {
        shell.failOn("reboot", TvCommandFailedException("exit code 1: not allowed"))

        val report = runner.run(actionWith(DebugStep.RunCommand("reboot")))

        assertEquals(DebugStepResult("reboot", StepOutcome.FAILED, "exit code 1: not allowed"), report.stepResults.single())
    }

    @Test
    fun `a broken connection fails the step instead of crashing`() = runTest {
        shell.errorOnRunCommand = IOException("Broken pipe")

        val report = runner.run(actionWith(DebugStep.RunCommand("id")))

        assertEquals("Broken pipe", report.stepResults.single().output)
    }

    @Test
    fun `an error without a message is described by its name`() = runTest {
        shell.errorOnRunCommand = IOException()

        val report = runner.run(actionWith(DebugStep.RunCommand("id")))

        assertEquals("IOException", report.stepResults.single().output)
    }

    @Test
    fun `the steps after a failed step are skipped`() = runTest {
        shell.failOn("first", TvCommandFailedException("failed"))

        val report = runner.run(actionWith(DebugStep.RunCommand("first"), DebugStep.RunCommand("second")))

        assertEquals(listOf(StepOutcome.FAILED, StepOutcome.SKIPPED), outcomesOf(report))
    }

    @Test
    fun `a skipped step is not run on the TV`() = runTest {
        shell.failOn("first", TvCommandFailedException("failed"))

        runner.run(actionWith(DebugStep.RunCommand("first"), DebugStep.RunCommand("second")))

        assertTrue(shell.receivedCommands.isEmpty())
    }

    @Test
    fun `the steps run in the order they are listed`() = runTest {
        runner.run(actionWith(DebugStep.RunCommand("one"), DebugStep.RunCommand("two"), DebugStep.RunCommand("three")))

        assertEquals(listOf("one", "two", "three"), shell.receivedCommands)
    }

    @Test
    fun `an expectation passes when the last output contains the text`() = runTest {
        shell.respondTo("echo hi", "hello there")

        val report = runner.run(actionWith(DebugStep.RunCommand("echo hi"), DebugStep.ExpectOutputContains("hello")))

        assertEquals(listOf(StepOutcome.PASSED, StepOutcome.PASSED), outcomesOf(report))
    }

    @Test
    fun `an expectation fails and shows the output when the text is missing`() = runTest {
        shell.respondTo("echo hi", "hello there")

        val report = runner.run(actionWith(DebugStep.RunCommand("echo hi"), DebugStep.ExpectOutputContains("goodbye")))

        val expectation = report.stepResults.last()
        assertEquals(StepOutcome.FAILED, expectation.outcome)
        assertEquals("The output was:\nhello there", expectation.output)
    }

    @Test
    fun `an expectation looks at the most recent command only`() = runTest {
        shell.respondTo("first", "alpha")
        shell.respondTo("second", "beta")

        val report = runner.run(
            actionWith(DebugStep.RunCommand("first"), DebugStep.RunCommand("second"), DebugStep.ExpectOutputContains("alpha")),
        )

        assertEquals(StepOutcome.FAILED, report.stepResults.last().outcome)
    }

    @Test
    fun `an expectation without any command before it fails`() = runTest {
        val report = runner.run(actionWith(DebugStep.ExpectOutputContains("anything")))

        assertEquals(StepOutcome.FAILED, report.stepResults.single().outcome)
    }

    @Test
    fun `pushing a file sends it to the TV`() = runTest {
        val file = temporaryFolder.newFile("helper.apk").apply { writeText("12345") }

        val report = runner.run(actionWith(DebugStep.PushFile(file, "/data/local/tmp/helper.apk")))

        assertEquals(listOf(file to "/data/local/tmp/helper.apk"), shell.pushedFiles)
        assertEquals(DebugStepResult("Push helper.apk to /data/local/tmp/helper.apk", StepOutcome.PASSED, "Pushed 5 bytes"), report.stepResults.single())
    }

    @Test
    fun `a push that fails shows the reason`() = runTest {
        shell.errorOnPushFile = IOException("No space left on device")

        val report = runner.run(actionWith(DebugStep.PushFile(File("a.apk"), "/data/local/tmp/a.apk")))

        assertEquals(DebugStepResult("Push a.apk to /data/local/tmp/a.apk", StepOutcome.FAILED, "No space left on device"), report.stepResults.single())
    }

    @Test
    fun `a pause waits for the given time`() = runTest {
        runner.run(actionWith(DebugStep.PauseFor(milliseconds = 1500)))

        assertEquals(1500, currentTime)
    }

    @Test
    fun `a pause passes`() = runTest {
        val report = runner.run(actionWith(DebugStep.PauseFor(milliseconds = 10)))

        assertEquals(StepOutcome.PASSED, report.stepResults.single().outcome)
    }

    @Test
    fun `an action without steps passes`() = runTest {
        assertTrue(runner.run(actionWith()).hasPassed)
    }

    @Test
    fun `a failed step makes the whole report fail`() = runTest {
        shell.failOn("bad", TvCommandFailedException("failed"))

        assertFalse(runner.run(actionWith(DebugStep.RunCommand("bad"))).hasPassed)
    }

    @Test
    fun `the report carries the name of the action`() = runTest {
        assertEquals("Test action", runner.run(actionWith()).actionName)
    }
}
