package net.biodiscus.androidcontroltv.core.model

data class TvSystemApp(
    val packageName: String,
    val label: String,
    val iconPngBase64: String? = null,
)
