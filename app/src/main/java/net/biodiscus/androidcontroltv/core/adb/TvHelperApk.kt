package net.biodiscus.androidcontroltv.core.adb

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/** The APK that is pushed to the TV so the helper class inside it can run there. */
internal interface TvHelperApk {
    val localFile: File
    val remotePath: String
}

internal class InstalledTvHelperApk @Inject constructor(
    @ApplicationContext context: Context,
) : TvHelperApk {

    override val localFile = File(context.applicationInfo.sourceDir)

    // The install time is in the name so a new version of the app never runs an older helper.
    override val remotePath = "$HELPER_DIRECTORY/$HELPER_FILE_PREFIX${localFile.lastModified()}.apk"
}
