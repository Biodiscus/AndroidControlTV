package net.biodiscus.androidcontroltv.testing

import java.io.IOException
import net.biodiscus.androidcontroltv.core.adb.TvConnector
import net.biodiscus.androidcontroltv.core.adb.TvShell
import net.biodiscus.androidcontroltv.core.model.TvAddress

internal class FakeTvConnector(private val shell: TvShell) : TvConnector {

    val connectedAddresses = mutableListOf<TvAddress>()
    var errorOnConnect: IOException? = null

    override suspend fun connect(address: TvAddress): TvShell {
        val error = errorOnConnect
        if (error != null) throw error
        connectedAddresses += address
        return shell
    }
}
