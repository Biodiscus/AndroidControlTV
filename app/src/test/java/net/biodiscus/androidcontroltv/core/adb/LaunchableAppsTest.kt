package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvApp
import org.junit.Assert.assertEquals
import org.junit.Test

class LaunchableAppsTest {

    @Test
    fun `an app is built from a component name`() {
        val apps = parseLaunchableApps("com.netflix.ninja/com.netflix.ninja.MainActivity")

        val expected = TvApp(packageName = "com.netflix.ninja", activityName = "com.netflix.ninja.MainActivity", label = "com.netflix.ninja")
        assertEquals(listOf(expected), apps)
    }

    @Test
    fun `an app with several launch activities is listed once`() {
        val output = """
            com.example.tv/com.example.tv.FirstActivity
            com.example.tv/com.example.tv.SecondActivity
        """.trimIndent()

        assertEquals(listOf("com.example.tv"), parseLaunchableApps(output).map(TvApp::packageName))
    }

    @Test
    fun `the apps are sorted by package name`() {
        val output = """
            org.zebra/org.zebra.Main
            com.apple/com.apple.Main
        """.trimIndent()

        assertEquals(listOf("com.apple", "org.zebra"), parseLaunchableApps(output).map(TvApp::packageName))
    }

    @Test
    fun `blank lines and lines that are not a component name are skipped`() {
        val output = "\n  \nNo activities found\ncom.example.tv/com.example.tv.Main\n"

        assertEquals(listOf("com.example.tv"), parseLaunchableApps(output).map(TvApp::packageName))
    }

    @Test
    fun `an empty output gives no apps`() {
        assertEquals(emptyList<TvApp>(), parseLaunchableApps(""))
    }
}
