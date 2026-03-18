package io.github.romangolovanov.apps.ametro.routes.entities

import io.github.romangolovanov.apps.ametro.model.MapContainer
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme

class MapRouteQueryParameters(
    private val container: MapContainer,
    private val enabledTransportsSet: Set<String>,
    val delayIndex: Int?,
    val beginStationUid: Int,
    val endStationUid: Int
) {
    val stationCount: Int get() = container.getMaxStationUid() + 1
    fun getEnabledTransportsSchemes(): Array<MapTransportScheme> =
        container.getTransportSchemes(enabledTransportsSet)
}
