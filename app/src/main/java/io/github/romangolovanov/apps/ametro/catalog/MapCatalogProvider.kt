package io.github.romangolovanov.apps.ametro.catalog

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.github.romangolovanov.apps.ametro.app.Constants
import io.github.romangolovanov.apps.ametro.catalog.entities.MapCatalog
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo
import io.github.romangolovanov.apps.ametro.model.serialization.FileAssetsMapProvider
import io.github.romangolovanov.apps.ametro.model.serialization.GlobalIdentifierProvider
import io.github.romangolovanov.apps.ametro.model.serialization.MapProvider
import io.github.romangolovanov.apps.ametro.model.serialization.ZipArchiveMapProvider
import java.io.IOException

class MapCatalogProvider(
    private val context: Context,
    private val localizationProvider: MapInfoLocalizationProvider
) {
    private var catalog: MapCatalog? = null
    private val identifierProvider = GlobalIdentifierProvider()

    val mapCatalog: MapCatalog
        get() = catalog ?: loadCatalog().also { catalog = it }

    fun findMapByName(mapFileName: String): MapInfo {
        for (map in mapCatalog.maps) {
            if (map.fileName == mapFileName) return map
        }
        throw Resources.NotFoundException("Not found map file $mapFileName")
    }

    private fun loadCatalog(): MapCatalog {
        return try {
            val json = context.assets.open("map_files/index.json").use { it.bufferedReader().readText() }
            val maps = MapCatalogSerializer.deserializeMapInfoArray(json)
            val localizations = localizationProvider.localizationMap
            val localizedMaps = Array(maps.size) { i ->
                val loc = localizations[maps[i].cityId]
                MapInfo(
                    maps[i],
                    loc?.cityName ?: "Unknown",
                    loc?.countryName ?: "Unknown",
                    loc?.countryIsoCode ?: ""
                )
            }
            MapCatalog(localizedMaps)
        } catch (ex: Exception) {
            Log.e(Constants.LOG, "Cannot read map catalog from assets", ex)
            MapCatalog(emptyArray())
        }
    }

    @Throws(IOException::class)
    fun getMapProvider(mapInfo: MapInfo): MapProvider {
        val fileAssetsPath = "map_files/${mapInfo.fileName}"
        val assets = context.assets

        try {
            assets.open(fileAssetsPath).use { is_ ->
                return ZipArchiveMapProvider(identifierProvider, is_)
            }
        } catch (ignored: IOException) {
            // not a zip, continue
        }

        val folderPath = if (fileAssetsPath.endsWith(".zip"))
            fileAssetsPath.dropLast(4)
        else
            fileAssetsPath

        try {
            val entries = assets.list(folderPath)
            if (!entries.isNullOrEmpty()) {
                return FileAssetsMapProvider(identifierProvider, assets, folderPath)
            }
        } catch (ignored: IOException) {
            // continue to error
        }

        throw IOException("Map ${mapInfo.fileName} assets not found")
    }
}
