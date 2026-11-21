package net.biodiscus.androidcontroltv.core.adb

import java.io.File
import net.biodiscus.androidcontroltv.core.model.TvAddress

internal interface TvShell {
    suspend fun runCommand(command: String): String
    suspend fun pushFile(localFile: File, remotePath: String)
    suspend fun close()
}

internal interface TvConnector {
    suspend fun connect(address: TvAddress): TvShell
}
