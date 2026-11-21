package net.biodiscus.androidcontroltv.core.debug

import java.io.File

sealed interface DebugStep {
    val title: String

    /** Runs a shell command on the TV and records what it printed. A command that fails stops the chain. */
    data class RunCommand(val command: String, override val title: String = command) : DebugStep

    /** Checks that the output of the most recent [RunCommand] contains the text. */
    data class ExpectOutputContains(
        val expectedText: String,
        override val title: String = "Expect the output to contain \"$expectedText\"",
    ) : DebugStep

    data class PushFile(
        val localFile: File,
        val remotePath: String,
        override val title: String = "Push ${localFile.name} to $remotePath",
    ) : DebugStep

    data class PauseFor(val milliseconds: Long, override val title: String = "Wait $milliseconds ms") : DebugStep
}
