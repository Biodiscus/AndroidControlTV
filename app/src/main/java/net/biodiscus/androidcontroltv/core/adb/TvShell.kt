package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvAddress

internal interface TvShell {
    suspend fun runCommand(command: String)
    suspend fun close()
}

internal interface TvConnector {
    suspend fun connect(address: TvAddress): TvShell
}
