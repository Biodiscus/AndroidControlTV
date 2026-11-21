package net.biodiscus.androidcontroltv.core.debug

private const val MAX_OUTPUT_LENGTH_IN_REPORT = 2000
private const val OUTPUT_INDENT = "   "

enum class StepOutcome { PASSED, FAILED, SKIPPED }

data class DebugStepResult(val title: String, val outcome: StepOutcome, val output: String = "")

data class DebugReport(val actionName: String, val stepResults: List<DebugStepResult>) {

    val hasPassed: Boolean = stepResults.all { it.outcome == StepOutcome.PASSED }

    /** The text the user copies into a chat, so it stays short enough to read. */
    fun toText(): String {
        val resultName = if (hasPassed) "PASSED" else "FAILED"
        val header = listOf("Debug action: $actionName", "Result: $resultName")
        val steps = stepResults.mapIndexed { index, result -> formatStep(index + 1, result) }
        return (header + steps).joinToString("\n")
    }

    private fun formatStep(number: Int, result: DebugStepResult): String {
        val titleLine = "$number. [${result.outcome}] ${result.title}"
        val output = result.output.trim()
        if (output.isEmpty()) return titleLine

        val indentedOutput = shorten(output).lines().joinToString("\n") { line -> OUTPUT_INDENT + line }
        return "$titleLine\n$indentedOutput"
    }

    private fun shorten(output: String): String {
        if (output.length <= MAX_OUTPUT_LENGTH_IN_REPORT) return output
        val hiddenCount = output.length - MAX_OUTPUT_LENGTH_IN_REPORT
        return output.take(MAX_OUTPUT_LENGTH_IN_REPORT) + "\n… ($hiddenCount more characters)"
    }
}

fun createNotConnectedReport(action: DebugAction): DebugReport {
    val notConnected = DebugStepResult(
        title = "Connect to a TV",
        outcome = StepOutcome.FAILED,
        output = "There is no connection to a TV, so nothing was run.",
    )
    return DebugReport(action.name, listOf(notConnected))
}
