package net.biodiscus.androidcontroltv.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TvApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val iconPngBase64: String? = null,
)
