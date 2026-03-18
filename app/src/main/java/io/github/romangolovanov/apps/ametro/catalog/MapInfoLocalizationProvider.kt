package io.github.romangolovanov.apps.ametro.catalog

import android.content.Context
import io.github.romangolovanov.apps.ametro.app.ApplicationSettingsProvider
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfoEntityName
import io.github.romangolovanov.apps.ametro.utils.FileUtils

class MapInfoLocalizationProvider(
    private val context: Context,
    private val applicationSettingsProvider: ApplicationSettingsProvider
) {
    private var localizationMap: HashMap<Int, MapInfoEntityName>? = null

    fun getCityName(cityId: Int): String =
        getLocalizationMap()[cityId]?.cityName ?: error("City $cityId not found")

    fun getCountryName(cityId: Int): String =
        getLocalizationMap()[cityId]?.countryName ?: error("City $cityId not found")

    fun getCountryIsoCode(cityId: Int): String =
        getLocalizationMap()[cityId]?.countryIsoCode ?: error("City $cityId not found")

    fun getLocalizationMap(): HashMap<Int, MapInfoEntityName> {
        localizationMap?.let { return it }

        try {
            val lang = applicationSettingsProvider.preferredMapLanguage
            val json = try {
                context.assets.open("map_files/locales/cities.$lang.json").use { FileUtils.readAllText(it) }
            } catch (e: Exception) {
                context.assets.open("map_files/locales/cities.default.json").use { FileUtils.readAllText(it) }
            }

            val entities = MapCatalogSerializer.deserializeLocalization(json)
            val map = HashMap<Int, MapInfoEntityName>(entities.size)
            for (entity in entities) {
                map[entity.cityId] = entity
            }
            localizationMap = map
            return map
        } catch (e: Exception) {
            throw RuntimeException("Localization cannot be read", e)
        }
    }
}
