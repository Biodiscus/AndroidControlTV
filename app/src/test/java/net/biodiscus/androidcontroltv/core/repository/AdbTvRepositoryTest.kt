package net.biodiscus.androidcontroltv.core.repository

import java.io.IOException
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.model.RemoteKey
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvConnectionState
import net.biodiscus.androidcontroltv.testing.FakeTvConnector
import net.biodiscus.androidcontroltv.testing.FakeTvShell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbTvRepositoryTest {

    private val tvAddress = TvAddress("192.168.1.20", 5555)
    private val shell = FakeTvShell()
    private val connector = FakeTvConnector(shell)
    private val repository = AdbTvRepository(connector)

    @Test
    fun `starts disconnected`() {
        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `connecting to a reachable TV ends up connected`() = runTest {
        repository.connect(tvAddress)

        assertEquals(TvConnectionState.Connected(tvAddress), repository.connectionState.value)
    }

    @Test
    fun `connecting asks the connector for the given address`() = runTest {
        repository.connect(tvAddress)

        assertEquals(listOf(tvAddress), connector.connectedAddresses)
    }

    @Test
    fun `connecting to an unreachable TV fails with the error message`() = runTest {
        connector.errorOnConnect = IOException("Connection refused")

        repository.connect(tvAddress)

        assertEquals(TvConnectionState.Failed("Connection refused"), repository.connectionState.value)
    }

    @Test
    fun `a failure without a message is described by the error name`() = runTest {
        connector.errorOnConnect = IOException()

        repository.connect(tvAddress)

        assertEquals(TvConnectionState.Failed("IOException"), repository.connectionState.value)
    }

    @Test
    fun `sending a key runs the key event command on the TV`() = runTest {
        repository.connect(tvAddress)

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(listOf("input keyevent KEYCODE_VOLUME_UP"), shell.receivedCommands)
    }

    @Test
    fun `sending a key while disconnected does nothing`() = runTest {
        repository.sendKey(RemoteKey.VOLUME_UP)

        assertTrue(shell.receivedCommands.isEmpty())
        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `a failing key press marks the connection as failed`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertEquals(TvConnectionState.Failed("Broken pipe"), repository.connectionState.value)
    }

    @Test
    fun `a failing key press closes the connection`() = runTest {
        repository.connect(tvAddress)
        shell.errorOnRunCommand = IOException("Broken pipe")

        repository.sendKey(RemoteKey.VOLUME_UP)

        assertTrue(shell.isClosed)
    }

    @Test
    fun `disconnecting closes the connection`() = runTest {
        repository.connect(tvAddress)

        repository.disconnect()

        assertTrue(shell.isClosed)
    }

    @Test
    fun `disconnecting ends up disconnected`() = runTest {
        repository.connect(tvAddress)

        repository.disconnect()

        assertEquals(TvConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `disconnecting while already disconnected closes nothing`() = runTest {
        repository.disconnect()

        assertFalse(shell.isClosed)
    }
}
