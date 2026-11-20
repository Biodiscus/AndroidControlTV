package net.biodiscus.androidcontroltv

import android.app.Application
import android.content.pm.ApplicationInfo
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AndroidControlTVApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (isDebuggableBuild()) Timber.plant(Timber.DebugTree())
    }

    private fun isDebuggableBuild(): Boolean =
        applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
