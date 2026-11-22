package net.biodiscus.androidcontroltv.core.adb

import dadb.AdbAuthException
import java.io.IOException

internal class TvAuthorizationDeniedException(cause: Throwable) : IOException(
    "The TV did not allow this app. Choose Allow on the \"Allow USB debugging?\" prompt on the TV and try again.",
    cause,
)

internal fun explainConnectionError(error: IOException): IOException =
    if (error is AdbAuthException) TvAuthorizationDeniedException(error) else error
