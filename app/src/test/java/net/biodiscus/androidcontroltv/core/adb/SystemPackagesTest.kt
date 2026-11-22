package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.TvSystemApp
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemPackagesTest {

    @Test
    fun `a system app is built from a package line`() {
        val systemApps = parseSystemPackages("package:com.android.settings")

        assertEquals(listOf(TvSystemApp("com.android.settings", "com.android.settings")), systemApps)
    }

    @Test
    fun `the system apps are sorted by package name`() {
        val output = "package:org.zebra\npackage:com.apple"

        assertEquals(listOf("com.apple", "org.zebra"), parseSystemPackages(output).map(TvSystemApp::packageName))
    }

    @Test
    fun `a package that is listed twice is listed once`() {
        val output = "package:com.example\npackage:com.example"

        assertEquals(1, parseSystemPackages(output).size)
    }

    @Test
    fun `lines that are not package lines are skipped`() {
        val output = "\n  \nWarning: something\npackage:\npackage:com.example\r\n"

        assertEquals(listOf("com.example"), parseSystemPackages(output).map(TvSystemApp::packageName))
    }

    @Test
    fun `an empty output gives no system apps`() {
        assertEquals(emptyList<TvSystemApp>(), parseSystemPackages(""))
    }
}
