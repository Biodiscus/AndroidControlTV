package net.biodiscus.androidcontroltv.core.model

data class TvAddress(val host: String, val port: Int) {

    val displayText: String = "$host:$port"

    companion object {
        const val DEFAULT_ADB_PORT = 5555

        private val VALID_PORTS = 1..65535

        fun parse(text: String): TvAddress? {
            val parts = text.trim().split(":")
            return when (parts.size) {
                1 -> createOrNull(host = parts[0], port = DEFAULT_ADB_PORT)
                2 -> createOrNull(host = parts[0], port = parts[1].toIntOrNull())
                else -> null
            }
        }

        private fun createOrNull(host: String, port: Int?): TvAddress? {
            if (port == null || port !in VALID_PORTS) return null
            if (host.isBlank() || host.any(Char::isWhitespace)) return null
            return TvAddress(host, port)
        }
    }
}
