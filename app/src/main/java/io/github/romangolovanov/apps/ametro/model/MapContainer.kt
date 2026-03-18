package io.github.romangolovanov.apps.ametro.model

import android.util.Log

import java.io.IOException
import java.util.Arrays
import java.util.HashMap

import io.github.romangolovanov.apps.ametro.app.Constants
import io.github.romangolovanov.apps.ametro.catalog.MapCatalogProvider
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo
import io.github.romangolovanov.apps.ametro.model.entities.MapLocale
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeSegment
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeTransfer
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportLine
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportSegment
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportTransfer
import io.github.romangolovanov.apps.ametro.model.serialization.MapProvider
import io.github.romangolovanov.apps.ametro.model.serialization.MapSerializationException

class MapContainer(
    private val catalogManager: MapCatalogProvider,
    val mapInfo: MapInfo,
    private val preferredLanguage: String
) {
    private var locale: MapLocale? = null
    var metadata: MapMetadata? = null
        private set
    private var stations: Array<MapStationInformation>? = null
    private var transports: HashMap<String, MapTransportScheme>? = null
    private var schemes: HashMap<String, MapScheme>? = null

    @Throws(MapSerializationException::class)
    fun loadSchemeWithTransports(schemeName: String, enabledTransports: Array<String>?) {
        try {
            catalogManager.getMapProvider(mapInfo).use { mapProvider ->
                if (metadata == null) {
                    val texts = mapProvider.getTextsMap(suggestLanguage(mapProvider.getSupportedLocales(), preferredLanguage))
                    val allTexts = mapProvider.getAllTextsMap()
                    locale = MapLocale(texts, allTexts)
                    metadata = mapProvider.getMetadata(locale)
                    stations = mapProvider.getStationInformation()
                    schemes = HashMap()
                    transports = HashMap()
                }
                val scheme = loadSchemeFile(mapProvider, schemeName, locale!!)
                loadTransportFiles(mapProvider, enabledTransports ?: scheme.defaultTransports)
            }
        } catch (e: MapSerializationException) {
            Log.e(Constants.LOG, "Map unpacking failed", e)
            throw e
        } catch (e: Exception) {
            Log.e(Constants.LOG, "Map loading failed", e)
            throw MapSerializationException(e)
        }
    }

    fun getScheme(schemeName: String): MapScheme? = schemes?.get(schemeName)

    fun getTransportSchemes(transportNames: Collection<String>): Array<MapTransportScheme> {
        val result = mutableListOf<MapTransportScheme>()
        for (name in transportNames) {
            val transport = transports?.get(name)
                ?: throw AssertionError("Transport scheme $name not loaded")
            result.add(transport)
        }
        return result.toTypedArray()
    }

    fun findStationInformation(lineName: String, stationName: String): MapStationInformation? {
        for (stationScheme in stations ?: return null) {
            if (stationScheme.line == lineName && stationScheme.station == stationName) {
                return stationScheme
            }
        }
        return null
    }

    fun findSchemeStation(schemeName: String, lineName: String, stationName: String): MapSchemeStation? {
        for (line in getScheme(schemeName)?.lines ?: return null) {
            if (line.name != lineName) continue
            for (stationScheme in line.stations) {
                if (stationScheme.name == stationName) return stationScheme
            }
        }
        return null
    }

    @Throws(IOException::class)
    fun loadStationMap(mapFilePath: String): String {
        try {
            catalogManager.getMapProvider(mapInfo).use { mapProvider ->
                return mapProvider.getFileContent(mapFilePath)
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG, "Map station scheme file loading failed", e)
            throw e
        }
    }

    fun getMaxStationUid(): Int {
        var max = 0
        for (transport in transports!!.values) {
            for (line in transport.lines) {
                for (segment in line.segments) {
                    max = maxOf(max, segment.from, segment.to)
                }
            }
            for (transfer in transport.transfers) {
                max = maxOf(max, transfer.from, transfer.to)
            }
        }
        for (scheme in schemes!!.values) {
            for (line in scheme.lines) {
                for (segment in line.segments) {
                    max = maxOf(max, segment.from, segment.to)
                }
            }
            for (transfer in scheme.transfers) {
                max = maxOf(max, transfer.from, transfer.to)
            }
        }
        return max
    }

    @Throws(IOException::class, MapSerializationException::class)
    private fun loadSchemeFile(mapProvider: MapProvider, schemeName: String, locale: MapLocale): MapScheme {
        var scheme = schemes!![schemeName]
        if (scheme == null) {
            scheme = mapProvider.getScheme(metadata!!.getScheme(schemeName).fileName, locale)
            schemes!![scheme.name] = scheme
        }
        return scheme
    }

    @Throws(IOException::class, MapSerializationException::class)
    private fun loadTransportFiles(mapProvider: MapProvider, enabledTransports: Array<String>) {
        for (transportName in enabledTransports) {
            loadTransport(mapProvider, transportName)
        }
    }

    @Throws(IOException::class)
    private fun loadTransport(mapProvider: MapProvider, transportName: String) {
        var transport = transports!![transportName]
        if (transport == null) {
            transport = mapProvider.getTransportScheme(metadata!!.getTransport(transportName).fileName)
            transports!![transport.name] = transport
        }
    }

    companion object {
        private fun suggestLanguage(supportedLanguages: Array<String>, preferredLanguage: String): String {
            val locales = Arrays.asList(*supportedLanguages)
            return if (!locales.contains(preferredLanguage)) locales[0] else preferredLanguage
        }
    }
}
