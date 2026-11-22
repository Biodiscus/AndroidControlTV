package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.testing.createTvApp
import org.junit.Assert.assertEquals
import org.junit.Test

class UninstallAppCommandTest {

    @Test
    fun `uninstalling an app runs pm uninstall with its package name`() {
        val app = createTvApp(packageName = "com.netflix.ninja")

        assertEquals("pm uninstall com.netflix.ninja", createUninstallAppCommand(app))
    }
}
