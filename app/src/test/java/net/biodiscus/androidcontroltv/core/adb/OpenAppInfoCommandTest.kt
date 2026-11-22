package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.testing.createTvSystemApp
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAppInfoCommandTest {

    @Test
    fun `opening the info of an app shows the app details settings of its package`() {
        val app = createTvSystemApp(packageName = "com.android.settings")

        val expected = "am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:com.android.settings"
        assertEquals(expected, createOpenAppInfoCommand(app))
    }
}
