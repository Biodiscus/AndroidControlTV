package net.biodiscus.androidcontroltv.testing

import java.io.IOException
import net.biodiscus.androidcontroltv.core.adb.TvShell

internal class FakeTvShell : TvShell {

    val receivedCommands = mutableListOf<String>()
    var errorOnRunCommand: IOException? = null
    var isClosed = false
        private set

    override suspend fun runCommand(command: String) {
        val error = errorOnRunCommand
        if (error != null) throw error
        receivedCommands += command
    }

    override suspend fun close() {
        isClosed = true
    }
}
