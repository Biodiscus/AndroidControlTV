package net.biodiscus.androidcontroltv.core.adb

import java.util.Base64
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvSystemApp
import net.biodiscus.androidcontroltv.tvhelper.TvHelperMain
import org.junit.Assert.assertEquals
import org.junit.Test

class TvHelperProtocolTest {

    private fun encode(text: String) = Base64.getEncoder().encodeToString(text.toByteArray(Charsets.UTF_8))

    private fun appLine(
        packageName: String = "com.example.tv",
        activityName: String = "com.example.tv.Main",
        label: String = "Example",
        icon: String = "aWNvbg==",
    ) = listOf("APP", packageName, activityName, encode(label), icon).joinToString("\t")

    @Test
    fun `an app line gives an app with its label and icon`() {
        val apps = parseTvHelperOutput(appLine())

        assertEquals(listOf(TvApp("com.example.tv", "com.example.tv.Main", "Example", "aWNvbg==")), apps)
    }

    @Test
    fun `a label with special characters is decoded`() {
        val apps = parseTvHelperOutput(appLine(label = "Café – TV ▶"))

        assertEquals("Café – TV ▶", apps.single().label)
    }

    @Test
    fun `an app without an icon has no icon`() {
        val apps = parseTvHelperOutput(appLine(icon = ""))

        assertEquals(null, apps.single().iconPngBase64)
    }

    @Test
    fun `an app with a blank label is labelled with its package name`() {
        val apps = parseTvHelperOutput(appLine(label = ""))

        assertEquals("com.example.tv", apps.single().label)
    }

    @Test
    fun `lines that are not app lines are skipped`() {
        val output = "WARNING: linker something\n${appLine()}\n\nAPP\tbroken"

        assertEquals(1, parseTvHelperOutput(output).size)
    }

    @Test
    fun `a line with a label that is not base64 is skipped`() {
        val brokenLine = listOf("APP", "com.example.tv", "Main", "%%%", "").joinToString("\t")

        assertEquals(emptyList<TvApp>(), parseTvHelperOutput(brokenLine))
    }

    @Test
    fun `the apps are sorted by label ignoring upper and lower case`() {
        val output = listOf(
            appLine(packageName = "b.app", label = "banana"),
            appLine(packageName = "a.app", label = "Cherry"),
            appLine(packageName = "c.app", label = "Apple"),
        ).joinToString("\n")

        assertEquals(listOf("Apple", "banana", "Cherry"), parseTvHelperOutput(output).map(TvApp::label))
    }

    @Test
    fun `an app that is reported twice is listed once`() {
        val output = listOf(appLine(), appLine()).joinToString("\n")

        assertEquals(1, parseTvHelperOutput(output).size)
    }

    private fun systemAppLine(
        packageName: String = "com.android.settings",
        label: String = "Settings",
        icon: String = "aWNvbg==",
    ) = listOf("SYSTEM_APP", packageName, encode(label), icon).joinToString("\t")

    @Test
    fun `a system app line gives a system app with its label and icon`() {
        val systemApps = parseTvHelperSystemApps(systemAppLine())

        assertEquals(listOf(TvSystemApp("com.android.settings", "Settings", "aWNvbg==")), systemApps)
    }

    @Test
    fun `a system app without an icon has no icon`() {
        val systemApps = parseTvHelperSystemApps(systemAppLine(icon = ""))

        assertEquals(null, systemApps.single().iconPngBase64)
    }

    @Test
    fun `a system app with a blank label is labelled with its package name`() {
        val systemApps = parseTvHelperSystemApps(systemAppLine(label = ""))

        assertEquals("com.android.settings", systemApps.single().label)
    }

    @Test
    fun `system apps are sorted by label ignoring upper and lower case`() {
        val output = listOf(
            systemAppLine(packageName = "b.app", label = "banana"),
            systemAppLine(packageName = "a.app", label = "Cherry"),
            systemAppLine(packageName = "c.app", label = "Apple"),
        ).joinToString("\n")

        assertEquals(listOf("Apple", "banana", "Cherry"), parseTvHelperSystemApps(output).map(TvSystemApp::label))
    }

    @Test
    fun `a system app that is reported twice is listed once`() {
        val output = listOf(systemAppLine(), systemAppLine()).joinToString("\n")

        assertEquals(1, parseTvHelperSystemApps(output).size)
    }

    @Test
    fun `system app lines that are broken are skipped`() {
        val brokenLabelLine = listOf("SYSTEM_APP", "com.example", "%%%", "").joinToString("\t")
        val output = "$brokenLabelLine\nSYSTEM_APP\tshort\n${systemAppLine()}"

        assertEquals(listOf("com.android.settings"), parseTvHelperSystemApps(output).map(TvSystemApp::packageName))
    }

    @Test
    fun `app lines and system app lines do not mix`() {
        val output = listOf(appLine(), systemAppLine()).joinToString("\n")

        assertEquals(listOf("com.example.tv"), parseTvHelperOutput(output).map(TvApp::packageName))
        assertEquals(listOf("com.android.settings"), parseTvHelperSystemApps(output).map(TvSystemApp::packageName))
    }

    @Test
    fun `the command starts the class that is really the helper`() {
        assertEquals(TvHelperMain::class.java.name, HELPER_MAIN_CLASS_NAME)
    }
}
