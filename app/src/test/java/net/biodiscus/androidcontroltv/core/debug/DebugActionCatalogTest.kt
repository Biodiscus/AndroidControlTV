package net.biodiscus.androidcontroltv.core.debug

import net.biodiscus.androidcontroltv.core.adb.createRunHelperCommand
import net.biodiscus.androidcontroltv.testing.FakeTvHelperApk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugActionCatalogTest {

    private val helperApk = FakeTvHelperApk()
    private val actions = DebugActionCatalog(helperApk).actions

    @Test
    fun `every action has a name and a description`() {
        assertTrue(actions.all { it.name.isNotBlank() && it.description.isNotBlank() })
    }

    @Test
    fun `every action has steps`() {
        assertTrue(actions.all { it.steps.isNotEmpty() })
    }

    @Test
    fun `no two actions have the same name`() {
        assertEquals(actions.size, actions.map(DebugAction::name).toSet().size)
    }

    @Test
    fun `every step has a title`() {
        assertTrue(actions.flatMap(DebugAction::steps).all { it.title.isNotBlank() })
    }

    @Test
    fun `the app helper action pushes the helper before it runs it`() {
        val steps = actions.single { it.name == "App helper" }.steps

        val pushIndex = steps.indexOfFirst { it is DebugStep.PushFile }
        val runIndex = steps.indexOf(DebugStep.RunCommand(createRunHelperCommand(helperApk.remotePath), title = "Run the helper on the TV"))
        assertTrue(pushIndex in 0 until runIndex)
    }

    @Test
    fun `the app helper action pushes the apk of this app`() {
        val push = actions.single { it.name == "App helper" }.steps.filterIsInstance<DebugStep.PushFile>().single()

        assertEquals(helperApk.localFile to helperApk.remotePath, push.localFile to push.remotePath)
    }

    @Test
    fun `the system apps action runs the helper of this build`() {
        val commands = actions.single { it.name == "System apps" }.steps.filterIsInstance<DebugStep.RunCommand>()

        assertTrue(commands.any { createRunHelperCommand(helperApk.remotePath) in it.command })
    }

    @Test
    fun `the system apps action checks the helper file of this build`() {
        val commands = actions.single { it.name == "System apps" }.steps.filterIsInstance<DebugStep.RunCommand>()

        assertTrue(commands.first().command.contains(helperApk.remotePath))
    }

    @Test
    fun `the system apps action does not print the helper output in full`() {
        val commands = actions.single { it.name == "System apps" }.steps.filterIsInstance<DebugStep.RunCommand>()

        assertTrue(commands.single { createRunHelperCommand(helperApk.remotePath) in it.command }.command.contains("uniq -c"))
    }
}
