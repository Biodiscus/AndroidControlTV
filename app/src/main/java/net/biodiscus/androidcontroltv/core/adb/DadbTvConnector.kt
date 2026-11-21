package net.biodiscus.androidcontroltv.core.adb

import android.content.Context
import dadb.AdbKeyPair
import dadb.Dadb
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.biodiscus.androidcontroltv.core.model.TvAddress

internal class DadbTvConnector @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TvConnector {

    override suspend fun connect(address: TvAddress): TvShell {
        val shell = createShell(address)
        confirmTvAnswers(shell)
        return shell
    }

    private suspend fun createShell(address: TvAddress): TvShell = withContext(Dispatchers.IO) {
        val keyPair = loadOrCreateKeyPair()
        val dadb = Dadb.create(address.host, address.port, keyPair, CONNECT_TIMEOUT_MILLIS, NO_SOCKET_TIMEOUT)
        DadbTvShell(dadb)
    }

    // Dadb opens its connection lazily, so without a first command a wrong address or
    // a declined "Allow USB debugging?" prompt would only show up on the first key press.
    private suspend fun confirmTvAnswers(shell: TvShell) {
        try {
            shell.runCommand(HANDSHAKE_COMMAND)
        } catch (error: IOException) {
            shell.close()
            throw error
        }
    }

    private fun loadOrCreateKeyPair(): AdbKeyPair {
        val privateKeyFile = File(context.filesDir, PRIVATE_KEY_FILE_NAME)
        val publicKeyFile = File(context.filesDir, PUBLIC_KEY_FILE_NAME)
        if (!privateKeyFile.exists()) {
            AdbKeyPair.generate(privateKeyFile, publicKeyFile)
        }
        return AdbKeyPair.read(privateKeyFile, publicKeyFile)
    }

    private companion object {
        const val PRIVATE_KEY_FILE_NAME = "adbkey"
        const val PUBLIC_KEY_FILE_NAME = "adbkey.pub"
        const val HANDSHAKE_COMMAND = "echo connected"
        const val CONNECT_TIMEOUT_MILLIS = 5_000
        const val NO_SOCKET_TIMEOUT = 0
    }
}
