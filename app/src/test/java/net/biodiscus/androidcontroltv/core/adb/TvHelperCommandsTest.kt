package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvApp
import org.junit.Assert.assertEquals
import org.junit.Test

class TvHelperCommandsTest {

    private val remotePath = "/data/local/tmp/androidcontroltv-helper-1.apk"

    @Test
    fun `the helper is run with its apk as class path`() {
        val expected = "CLASSPATH='$remotePath' app_process / net.biodiscus.androidcontroltv.tvhelper.TvHelperMain"

        assertEquals(expected, createRunHelperCommand(remotePath))
    }

    @Test
    fun `checking for the helper always ends with an answer`() {
        val expected = "[ -f '$remotePath' ] && echo installed || echo missing"

        assertEquals(expected, createIsHelperInstalledCommand(remotePath))
    }

    @Test
    fun `old helpers are removed by their file name prefix`() {
        assertEquals("rm -f /data/local/tmp/androidcontroltv-helper-*.apk", createRemoveOldHelpersCommand())
    }

    @Test
    fun `launching an app starts its launcher activity`() {
        val app = TvApp("com.netflix.ninja", "com.netflix.ninja.MainActivity", "Netflix")

        val expected = "am start -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER " +
            "-n 'com.netflix.ninja/com.netflix.ninja.MainActivity'"
        assertEquals(expected, createLaunchAppCommand(app))
    }

    @Test
    fun `the component of an app is quoted so a dollar sign is not expanded`() {
        val app = TvApp("com.example", "com.example.Main\$Launcher", "Example")

        assertEquals(true, createLaunchAppCommand(app).endsWith("-n 'com.example/com.example.Main\$Launcher'"))
    }
}
