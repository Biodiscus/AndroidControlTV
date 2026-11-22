package net.biodiscus.androidcontroltv.core.model

enum class AppUninstallResult {
    UNINSTALLED,

    /** The TV answered but kept the app, for example because it is a system app. The connection is fine. */
    REFUSED,

    /** The command did not reach the TV. The connection state tells why. */
    NOT_SENT,
}
