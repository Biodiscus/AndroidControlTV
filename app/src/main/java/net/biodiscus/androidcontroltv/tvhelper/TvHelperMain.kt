package net.biodiscus.androidcontroltv.tvhelper

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Looper
import android.util.Base64
import androidx.annotation.Keep
import java.io.ByteArrayOutputStream
import kotlin.system.exitProcess
import net.biodiscus.androidcontroltv.core.adb.HELPER_APP_LINE_PREFIX
import net.biodiscus.androidcontroltv.core.adb.HELPER_FIELD_SEPARATOR
import net.biodiscus.androidcontroltv.core.adb.HELPER_SYSTEM_APP_LINE_PREFIX

/**
 * Runs on the TV, not on the phone: the app pushes its own APK to the TV and starts this class with app_process.
 * It prints the label and icon of every app that has a TV launcher entry and of every system app, see
 * parseTvHelperOutput and parseTvHelperSystemApps for the formats.
 */
@Keep
object TvHelperMain {

    private const val ICON_SIZE_PIXELS = 128

    @JvmStatic
    fun main(args: Array<String>) {
        val packageManager = createSystemContext().packageManager
        val launcherActivities = packageManager.queryIntentActivities(createLauncherIntent(), 0)
        launcherActivities.forEach { launcherActivity -> printApp(packageManager, launcherActivity) }
        packageManager.getInstalledApplications(0)
            .filter(::isSystemApp)
            .forEach { systemApp -> printSystemApp(packageManager, systemApp) }
        System.out.flush()
        exitProcess(0)
    }

    private fun createLauncherIntent(): Intent =
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)

    // A process started with app_process has no Context, and the system context is the only way to get a PackageManager
    // that can load the labels and icons of other apps. It is not public API, so this is the part that can break per TV.
    @Suppress("DEPRECATION")
    private fun createSystemContext(): Context {
        Looper.prepareMainLooper()
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getMethod("systemMain").invoke(null)
        return activityThreadClass.getMethod("getSystemContext").invoke(activityThread) as Context
    }

    private fun printApp(packageManager: PackageManager, launcherActivity: ResolveInfo) {
        val activityInfo = launcherActivity.activityInfo
        val label = launcherActivity.loadLabel(packageManager).toString()
        val fields = listOf(
            HELPER_APP_LINE_PREFIX,
            activityInfo.packageName,
            activityInfo.name,
            encodeText(label),
            encodeIconOrEmpty { launcherActivity.loadIcon(packageManager) },
        )
        println(fields.joinToString(HELPER_FIELD_SEPARATOR))
    }

    private fun isSystemApp(applicationInfo: ApplicationInfo): Boolean =
        (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

    private fun printSystemApp(packageManager: PackageManager, systemApp: ApplicationInfo) {
        val fields = listOf(
            HELPER_SYSTEM_APP_LINE_PREFIX,
            systemApp.packageName,
            encodeText(systemApp.loadLabel(packageManager).toString()),
            encodeIconOrEmpty { systemApp.loadIcon(packageManager) },
        )
        println(fields.joinToString(HELPER_FIELD_SEPARATOR))
    }

    private fun encodeIconOrEmpty(loadIcon: () -> Drawable): String =
        try {
            encodeAsPng(loadIcon())
        } catch (_: Exception) {
            ""
        }

    private fun encodeAsPng(icon: Drawable): String {
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PIXELS, ICON_SIZE_PIXELS, Bitmap.Config.ARGB_8888)
        icon.setBounds(0, 0, ICON_SIZE_PIXELS, ICON_SIZE_PIXELS)
        icon.draw(Canvas(bitmap))

        val pngBytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngBytes)
        return Base64.encodeToString(pngBytes.toByteArray(), Base64.NO_WRAP)
    }

    private fun encodeText(text: String): String = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
}
