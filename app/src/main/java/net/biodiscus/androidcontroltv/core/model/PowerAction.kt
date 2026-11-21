package net.biodiscus.androidcontroltv.core.model

enum class PowerAction(val endsConnection: Boolean) {
    REBOOT(endsConnection = true),
    SLEEP(endsConnection = false),
    POWER_OFF(endsConnection = true),
}
