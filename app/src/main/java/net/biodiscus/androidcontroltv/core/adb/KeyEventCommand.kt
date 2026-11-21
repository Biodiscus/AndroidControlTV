package net.biodiscus.androidcontroltv.core.adb

import net.biodiscus.androidcontroltv.core.model.RemoteKey

internal fun createKeyEventCommand(key: RemoteKey): String = "input keyevent ${key.androidKeyName}"
