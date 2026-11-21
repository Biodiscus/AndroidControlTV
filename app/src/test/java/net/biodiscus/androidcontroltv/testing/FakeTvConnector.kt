package net.biodiscus.androidcontroltv.testing

import java.io.IOException
import net.biodiscus.androidcontroltv.core.adb.TvConnector
import net.biodiscus.androidcontroltv.core.adb.TvShell
import net.biodiscus.androidcontroltv.core.model.TvAddress

/** Hands out the given shells one by one, one per connection, and keeps handing out the last one. */
internal class FakeTvConnector(firstShell: TvShell, vararg laterShells: TvShell) : TvConnector {

    private val shells = listOf(firstShell) + laterShells

    val connectedAddresses = mutableListOf<TvAddress>()
    var errorOnConnect: IOException? = null

    override suspend fun connect(address: TvAddress): TvShell {
        val error = errorOnConnect
        if (error != null) throw error
        val shell = shells[minOf(connectedAddresses.size, shells.lastIndex)]
        connectedAddresses += address
        return shell
    }
}
