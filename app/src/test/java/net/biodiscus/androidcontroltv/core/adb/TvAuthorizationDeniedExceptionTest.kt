package net.biodiscus.androidcontroltv.core.adb

import dadb.AdbAuthException
import java.io.IOException
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TvAuthorizationDeniedExceptionTest {

    @Test
    fun `a TV that rejects the key is explained as authorization denied`() {
        val error = AdbAuthException("Device rejected authentication (unauthorized)")

        val explained = explainConnectionError(error)

        assertTrue(explained is TvAuthorizationDeniedException)
    }

    @Test
    fun `the explanation keeps the original error as its cause`() {
        val error = AdbAuthException("Device rejected authentication (unauthorized)")

        val explained = explainConnectionError(error)

        assertSame(error, explained.cause)
    }

    @Test
    fun `an error that is not about authorization is left as it is`() {
        val error = IOException("Connection refused")

        val explained = explainConnectionError(error)

        assertSame(error, explained)
    }
}
