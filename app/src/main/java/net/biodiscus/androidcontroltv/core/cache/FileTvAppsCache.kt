package net.biodiscus.androidcontroltv.core.cache

import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import net.biodiscus.androidcontroltv.core.model.TvAddress
import net.biodiscus.androidcontroltv.core.model.TvApp

internal class FileTvAppsCache(private val directory: File) : TvAppsCache {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun read(address: TvAddress): List<TvApp>? =
        withContext(Dispatchers.IO) {
            val file = fileFor(address)
            if (!file.exists()) return@withContext null
            readAppsOrNull(file)
        }

    override suspend fun write(address: TvAddress, apps: List<TvApp>) {
        withContext(Dispatchers.IO) {
            try {
                directory.mkdirs()
                fileFor(address).writeText(json.encodeToString(apps))
            } catch (_: IOException) {
                // A cache that cannot be written only makes the next start slower, so it is not an error.
            }
        }
    }

    private fun readAppsOrNull(file: File): List<TvApp>? =
        try {
            json.decodeFromString<List<TvApp>>(file.readText())
        } catch (_: IOException) {
            null
        } catch (_: SerializationException) {
            null
        }

    private fun fileFor(address: TvAddress): File {
        val safeName = address.displayText.replace(Regex("[^A-Za-z0-9.-]"), "_")
        return File(directory, "$safeName.json")
    }
}
