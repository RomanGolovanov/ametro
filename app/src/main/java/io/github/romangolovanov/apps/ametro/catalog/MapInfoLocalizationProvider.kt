package io.github.romangolovanov.apps.ametro.catalog

import android.content.Context
import io.github.romangolovanov.apps.ametro.app.ApplicationSettingsProvider
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfoEntityName

class MapInfoLocalizationProvider(
    private val context: Context,
    private val applicationSettingsProvider: ApplicationSettingsProvider
) {
    val localizationMap: Map<Int, MapInfoEntityName> by lazy {
        try {
            val lang = applicationSettingsProvider.preferredMapLanguage
            val json = try {
                context.assets.open("map_files/locales/cities.$lang.json").use { it.bufferedReader().readText() }
            } catch (e: Exception) {
                context.assets.open("map_files/locales/cities.default.json").use { it.bufferedReader().readText() }
            }
            MapCatalogSerializer.deserializeLocalization(json).associateBy { it.cityId }
        } catch (e: Exception) {
            throw RuntimeException("Localization cannot be read", e)
        }
    }

    fun getCityName(cityId: Int): String =
        localizationMap[cityId]?.cityName ?: error("City $cityId not found")

    fun getCountryName(cityId: Int): String =
        localizationMap[cityId]?.countryName ?: error("City $cityId not found")

    fun getCountryIsoCode(cityId: Int): String =
        localizationMap[cityId]?.countryIsoCode ?: error("City $cityId not found")
}
