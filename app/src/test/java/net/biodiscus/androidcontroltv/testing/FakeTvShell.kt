package net.biodiscus.androidcontroltv.testing

import java.io.File
import java.io.IOException
import net.biodiscus.androidcontroltv.core.adb.TvShell

internal class FakeTvShell : TvShell {

    private val outputsByCommand = mutableMapOf<String, String>()
    private val errorsByCommand = mutableMapOf<String, IOException>()

    val receivedCommands = mutableListOf<String>()
    val pushedFiles = mutableListOf<Pair<File, String>>()
    var errorOnRunCommand: IOException? = null
    var errorOnPushFile: IOException? = null
    var outputOfRunCommand = ""
    var isClosed = false
        private set

    fun respondTo(command: String, output: String) {
        outputsByCommand[command] = output
    }

    fun failOn(command: String, error: IOException) {
        errorsByCommand[command] = error
    }

    override suspend fun runCommand(command: String): String {
        val error = errorOnRunCommand ?: errorsByCommand[command]
        if (error != null) throw error
        receivedCommands += command
        return outputsByCommand[command] ?: outputOfRunCommand
    }

    override suspend fun pushFile(localFile: File, remotePath: String) {
        val error = errorOnPushFile
        if (error != null) throw error
        pushedFiles += localFile to remotePath
    }

    override suspend fun close() {
        isClosed = true
    }
}
