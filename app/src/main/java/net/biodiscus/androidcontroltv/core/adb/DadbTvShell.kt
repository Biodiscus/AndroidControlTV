package net.biodiscus.androidcontroltv.core.adb

import dadb.Dadb
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DadbTvShell(private val dadb: Dadb) : TvShell {

    override suspend fun runCommand(command: String) {
        withContext(Dispatchers.IO) {
            val response = dadb.shell(command)
            if (response.exitCode != 0) {
                throw IOException("TV command failed (exit code ${response.exitCode}): ${response.allOutput}")
            }
        }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) { dadb.close() }
    }
}
