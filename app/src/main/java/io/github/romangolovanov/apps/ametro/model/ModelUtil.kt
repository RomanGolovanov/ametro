package io.github.romangolovanov.apps.ametro.model

import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation

object ModelUtil {

    fun findTouchedStation(scheme: MapScheme, touchPoint: MapPoint): Pair<MapSchemeLine, MapSchemeStation>? {
        return scheme.lines.firstNotNullOfOrNull { line ->
            line.stations.firstOrNull { station ->
                val rect = station.labelPosition
                val point = station.position
                (rect != null && rect.contains(touchPoint)) ||
                    (point != null && point.distance(touchPoint) <= scheme.stationsDiameter)
            }?.let { line to it }
        }
    }

    fun findStationByUid(scheme: MapScheme, uid: Long): Pair<MapSchemeLine, MapSchemeStation>? {
        return scheme.lines.firstNotNullOfOrNull { line ->
            line.stations.firstOrNull { it.uid.toLong() == uid }?.let { line to it }
        }
    }
}
