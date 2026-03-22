package io.github.romangolovanov.apps.ametro.model.serialization

import android.content.res.AssetManager
import android.graphics.BitmapFactory

import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.IOException
import java.io.InputStream

import io.github.romangolovanov.apps.ametro.model.entities.MapLocale
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme

/**
 * Provides access to map contents extracted under a relative folder in assets/.
 * For example: fileAssetLocation = "maps/moscow"
 */
class FileAssetsMapProvider(
    private val identifierProvider: GlobalIdentifierProvider,
    private val assetManager: AssetManager,
    fileAssetLocation: String
) : MapProvider {

    private val basePath: String = if (fileAssetLocation.endsWith("/")) fileAssetLocation else "$fileAssetLocation/"
    private val reader = ObjectMapper().reader()

    @Throws(IOException::class)
    override fun getSupportedLocales(): Array<String> = getMetadata(null).locales

    @Throws(IOException::class)
    override fun getTextsMap(languageCode: String): Map<Int, String> {
        getInputStream("texts/$languageCode.json").use { stream ->
            return MetadataTypes.asTextMap(reader.readTree(stream))
        }
    }

    @Throws(IOException::class)
    override fun getAllTextsMap(): Map<Int, MutableList<String>> {
        return getSupportedLocales()
            .flatMap { locale -> getTextsMap(locale).entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> values.toMutableList() }
    }

    @Throws(IOException::class)
    override fun getMetadata(locale: MapLocale?): MapMetadata {
        getInputStream("index.json").use { stream ->
            return MetadataTypes.asMetadata(reader.readTree(stream), locale)
        }
    }

    @Throws(IOException::class)
    override fun getTransportScheme(name: String): MapTransportScheme {
        getInputStream(name).use { stream ->
            return TransportSchemeTypes.asMapTransportScheme(reader.readTree(stream))
        }
    }

    @Throws(IOException::class)
    override fun getScheme(name: String, locale: MapLocale): MapScheme {
        val scheme: MapScheme
        getInputStream(name).use { stream ->
            scheme = SchemeTypes.asMapScheme(identifierProvider, reader.readTree(stream), locale)
        }
        for (imageName in scheme.imageNames) {
            scheme.setBackgroundObject(imageName, getBackgroundObject(imageName))
        }
        return scheme
    }

    @Throws(IOException::class)
    override fun getBackgroundObject(name: String): Any {
        try {
            getInputStream(name).use { stream ->
                return if (name.endsWith(".svg")) {
                    val svg = SVG.getFromInputStream(stream)
                    svg.renderToPicture()
                } else if (name.endsWith(".png")) {
                    BitmapFactory.decodeStream(stream)
                } else {
                    throw IOException("Unsupported type of image file $name")
                }
            }
        } catch (e: SVGParseException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    override fun getStationInformation(): Array<MapStationInformation> {
        getInputStream("images.json").use { stream ->
            return MetadataTypes.asStationInformation(reader.readTree(stream))
        }
    }

    @Throws(IOException::class)
    override fun getFileContent(name: String): String {
        getInputStream(name).use { stream ->
            return stream.bufferedReader().readText()
        }
    }

    @Throws(IOException::class)
    private fun getInputStream(relativePath: String): InputStream {
        val fullPath = basePath + relativePath
        return try {
            assetManager.open(fullPath)
        } catch (e: IOException) {
            throw IOException("Asset not found: $fullPath", e)
        }
    }

    override fun close() {
        // Nothing to close for AssetManager
    }
}
