package net.biodiscus.androidcontroltv.core.debug

import java.io.IOException
import kotlinx.coroutines.delay
import net.biodiscus.androidcontroltv.core.adb.TvShell

internal class DebugActionRunner(private val shell: TvShell) {

    suspend fun run(action: DebugAction): DebugReport {
        val results = mutableListOf<DebugStepResult>()
        var lastCommandOutput = ""

        for (step in action.steps) {
            val hasFailedBefore = results.any { it.outcome == StepOutcome.FAILED }
            val result = if (hasFailedBefore) skip(step) else runStep(step, lastCommandOutput)
            if (step is DebugStep.RunCommand && result.outcome == StepOutcome.PASSED) {
                lastCommandOutput = result.output
            }
            results += result
        }
        return DebugReport(action.name, results)
    }

    private suspend fun runStep(step: DebugStep, lastCommandOutput: String): DebugStepResult =
        when (step) {
            is DebugStep.RunCommand -> runCommand(step)
            is DebugStep.ExpectOutputContains -> expectOutputContains(step, lastCommandOutput)
            is DebugStep.PushFile -> pushFile(step)
            is DebugStep.PauseFor -> pauseFor(step)
        }

    private suspend fun runCommand(step: DebugStep.RunCommand): DebugStepResult =
        try {
            pass(step, shell.runCommand(step.command))
        } catch (error: IOException) {
            fail(step, describe(error))
        }

    private fun expectOutputContains(step: DebugStep.ExpectOutputContains, lastCommandOutput: String): DebugStepResult =
        if (lastCommandOutput.contains(step.expectedText)) {
            pass(step)
        } else {
            fail(step, "The output was:\n$lastCommandOutput")
        }

    private suspend fun pushFile(step: DebugStep.PushFile): DebugStepResult =
        try {
            shell.pushFile(step.localFile, step.remotePath)
            pass(step, "Pushed ${step.localFile.length()} bytes")
        } catch (error: IOException) {
            fail(step, describe(error))
        }

    private suspend fun pauseFor(step: DebugStep.PauseFor): DebugStepResult {
        delay(step.milliseconds)
        return pass(step)
    }

    private fun pass(step: DebugStep, output: String = "") = DebugStepResult(step.title, StepOutcome.PASSED, output)

    private fun fail(step: DebugStep, output: String) = DebugStepResult(step.title, StepOutcome.FAILED, output)

    private fun skip(step: DebugStep) = DebugStepResult(step.title, StepOutcome.SKIPPED)

    private fun describe(error: IOException): String = error.message ?: error.javaClass.simpleName
}
