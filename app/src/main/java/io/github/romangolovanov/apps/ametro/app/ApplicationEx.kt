package io.github.romangolovanov.apps.ametro.app

import android.app.Activity
import android.app.Application
import android.graphics.PointF
import androidx.core.content.ContextCompat
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.catalog.MapCatalogProvider
import io.github.romangolovanov.apps.ametro.catalog.MapInfoLocalizationProvider
import io.github.romangolovanov.apps.ametro.model.MapContainer
import io.github.romangolovanov.apps.ametro.providers.IconProvider

class ApplicationEx : Application() {

    // Providers — lazy-initialized, thread-safe
    val applicationSettingsProvider: ApplicationSettingsProvider by lazy {
        ApplicationSettingsProvider(this)
    }

    val countryFlagProvider: IconProvider by lazy {
        IconProvider(
            this,
            ContextCompat.getDrawable(applicationContext, R.drawable.no_country)!!,
            "country_icons"
        )
    }

    val mapCatalogProvider: MapCatalogProvider by lazy {
        MapCatalogProvider(applicationContext, localizedMapInfoProvider)
    }

    val localizedMapInfoProvider: MapInfoLocalizationProvider by lazy {
        MapInfoLocalizationProvider(applicationContext, applicationSettingsProvider)
    }

    // UI state — will be moved to MapViewModel in Phase 3.
    // Kotlin var properties auto-generate getX()/setX() for Java callers.
    var container: MapContainer? = null
    var schemeName: String? = null
    var enabledTransports: Array<String>? = null
    var centerPositionAndScale: kotlin.Pair<PointF, Float>? = null
    // Named to match the Java caller API: getSelectedBeginUid() / getSelectedEndUid()
    var selectedBeginUid: Int? = null
    var selectedEndUid: Int? = null

    fun setCurrentMapViewState(container: MapContainer, schemeName: String, enabledTransports: Array<String>?) {
        this.container = container
        this.schemeName = schemeName
        this.enabledTransports = enabledTransports
    }

    fun clearCurrentMapViewState() {
        container = null
        schemeName = null
        enabledTransports = null
    }

    fun setSelectedStations(beginUid: Int?, endUid: Int?) {
        selectedBeginUid = beginUid
        selectedEndUid = endUid
    }

    companion object {
        fun getInstance(activity: Activity): ApplicationEx =
            activity.application as ApplicationEx
    }
}
