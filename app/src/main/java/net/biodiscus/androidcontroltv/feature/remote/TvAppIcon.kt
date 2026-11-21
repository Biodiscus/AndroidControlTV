package net.biodiscus.androidcontroltv.feature.remote

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal fun decodeTvAppIcon(iconPngBase64: String?): ImageBitmap? {
    if (iconPngBase64 == null) return null
    val pngBytes = Base64.decode(iconPngBase64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.size)?.asImageBitmap()
}
