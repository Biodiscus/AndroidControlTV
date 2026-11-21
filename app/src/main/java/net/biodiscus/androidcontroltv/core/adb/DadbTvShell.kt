package net.biodiscus.androidcontroltv.core.adb

import dadb.Dadb
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DadbTvShell(private val dadb: Dadb) : TvShell {

    override suspend fun runCommand(command: String): String =
        withContext(Dispatchers.IO) {
            val response = dadb.shell(command)
            if (response.exitCode != 0) {
                throw TvCommandFailedException("TV command failed (exit code ${response.exitCode}): ${response.allOutput}")
            }
            response.allOutput
        }

    override suspend fun pushFile(localFile: File, remotePath: String) {
        withContext(Dispatchers.IO) { dadb.push(localFile, remotePath) }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) { dadb.close() }
    }
}
