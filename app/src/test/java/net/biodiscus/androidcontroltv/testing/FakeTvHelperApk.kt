package net.biodiscus.androidcontroltv.testing

import java.io.File
import net.biodiscus.androidcontroltv.core.adb.TvHelperApk

internal class FakeTvHelperApk : TvHelperApk {
    override val localFile = File("phone/base.apk")
    override val remotePath = "/data/local/tmp/androidcontroltv-helper-1.apk"
}
