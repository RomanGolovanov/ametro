package io.github.romangolovanov.apps.ametro.model.serialization

import java.io.IOException

import io.github.romangolovanov.apps.ametro.model.entities.MapLocale
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme

/**
 * Abstraction for different sources of map content (zip archive, assets folder, etc.)
 */
interface MapProvider : AutoCloseable {
    @Throws(IOException::class)
    fun getSupportedLocales(): Array<String>

    @Throws(IOException::class)
    fun getTextsMap(languageCode: String): Map<Int, String>

    @Throws(IOException::class)
    fun getAllTextsMap(): Map<Int, MutableList<String>>

    @Throws(IOException::class)
    fun getMetadata(locale: MapLocale?): MapMetadata

    @Throws(IOException::class)
    fun getTransportScheme(name: String): MapTransportScheme

    @Throws(IOException::class)
    fun getScheme(name: String, locale: MapLocale): MapScheme

    @Throws(IOException::class)
    fun getBackgroundObject(name: String): Any

    @Throws(IOException::class)
    fun getStationInformation(): Array<MapStationInformation>

    @Throws(IOException::class)
    fun getFileContent(name: String): String

    @Throws(IOException::class)
    override fun close()
}
