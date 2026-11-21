package net.biodiscus.androidcontroltv.core.adb

import java.io.IOException
import kotlinx.coroutines.test.runTest
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.testing.FakeTvHelperApk
import net.biodiscus.androidcontroltv.testing.FakeTvShell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AdbTvAppLoaderTest {

    private val helperApk = FakeTvHelperApk()
    private val shell = FakeTvShell()
    private val loader = AdbTvAppLoader(helperApk)

    // "Example" and a one byte icon, in the format the helper on the TV prints.
    private val helperOutput = "APP\tcom.example.tv\tcom.example.tv.Main\tRXhhbXBsZQ==\taQ=="
    private val helperApp = TvApp("com.example.tv", "com.example.tv.Main", "Example", "aQ==")

    private fun givenHelperIsInstalled() {
        shell.respondTo(createIsHelperInstalledCommand(helperApk.remotePath), "installed")
    }

    private fun givenHelperReports(output: String) {
        shell.respondTo(createRunHelperCommand(helperApk.remotePath), output)
    }

    @Test
    fun `apps are loaded from the helper on the TV`() = runTest {
        givenHelperIsInstalled()
        givenHelperReports(helperOutput)

        val loaded = loader.load(shell)

        assertEquals(listOf(helperApp), loaded.apps)
    }

    @Test
    fun `apps from the helper are marked as having names and icons`() = runTest {
        givenHelperIsInstalled()
        givenHelperReports(helperOutput)

        assertTrue(loader.load(shell).hasNamesAndIcons)
    }

    @Test
    fun `apps from the plain list are marked as not having names and icons`() = runTest {
        givenHelperIsInstalled()
        givenHelperReports("")
        shell.respondTo(LIST_LAUNCHABLE_APPS_COMMAND, "org.plain/org.plain.Main")

        assertFalse(loader.load(shell).hasNamesAndIcons)
    }

    @Test
    fun `an installed helper is not pushed again`() = runTest {
        givenHelperIsInstalled()
        givenHelperReports(helperOutput)

        loader.load(shell)

        assertTrue(shell.pushedFiles.isEmpty())
    }

    @Test
    fun `a missing helper is pushed to the TV`() = runTest {
        shell.respondTo(createIsHelperInstalledCommand(helperApk.remotePath), "missing")
        givenHelperReports(helperOutput)

        loader.load(shell)

        assertEquals(listOf(helperApk.localFile to helperApk.remotePath), shell.pushedFiles)
    }

    @Test
    fun `old helpers are removed before the new one is pushed`() = runTest {
        shell.respondTo(createIsHelperInstalledCommand(helperApk.remotePath), "missing")
        givenHelperReports(helperOutput)

        loader.load(shell)

        assertTrue(createRemoveOldHelpersCommand() in shell.receivedCommands)
    }

    @Test
    fun `a pushed helper is made read only before it runs`() = runTest {
        shell.respondTo(createIsHelperInstalledCommand(helperApk.remotePath), "missing")
        givenHelperReports(helperOutput)

        loader.load(shell)

        val commands = shell.receivedCommands
        val readOnlyIndex = commands.indexOf(createMakeHelperReadOnlyCommand(helperApk.remotePath))
        assertTrue(readOnlyIndex in 0 until commands.indexOf(createRunHelperCommand(helperApk.remotePath)))
    }

    @Test
    fun `the plain list is used when the helper reports no apps`() = runTest {
        givenHelperIsInstalled()
        givenHelperReports("")
        shell.respondTo(LIST_LAUNCHABLE_APPS_COMMAND, "org.plain/org.plain.Main")

        val loaded = loader.load(shell)

        assertEquals(listOf("org.plain"), loaded.apps.map(TvApp::packageName))
    }

    @Test
    fun `the plain list is used when the helper command fails`() = runTest {
        givenHelperIsInstalled()
        shell.failOn(createRunHelperCommand(helperApk.remotePath), TvCommandFailedException("exit code 1"))
        shell.respondTo(LIST_LAUNCHABLE_APPS_COMMAND, "org.plain/org.plain.Main")

        val loaded = loader.load(shell)

        assertEquals(listOf("org.plain"), loaded.apps.map(TvApp::packageName))
    }

    @Test
    fun `the plain list is used when the helper cannot be pushed`() = runTest {
        shell.respondTo(createIsHelperInstalledCommand(helperApk.remotePath), "missing")
        shell.errorOnPushFile = IOException("No space left on device")
        shell.respondTo(LIST_LAUNCHABLE_APPS_COMMAND, "org.plain/org.plain.Main")

        val loaded = loader.load(shell)

        assertEquals(listOf("org.plain"), loaded.apps.map(TvApp::packageName))
    }

    @Test
    fun `a broken connection is reported to the caller`() = runTest {
        shell.errorOnRunCommand = IOException("Broken pipe")

        try {
            loader.load(shell)
            fail("Expected the broken connection to be reported")
        } catch (error: IOException) {
            assertEquals("Broken pipe", error.message)
        }
    }
}
