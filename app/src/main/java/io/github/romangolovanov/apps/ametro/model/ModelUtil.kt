package io.github.romangolovanov.apps.ametro.model

import android.util.Pair

import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation

object ModelUtil {

    @JvmStatic
    fun findTouchedStation(scheme: MapScheme, touchPoint: MapPoint): Pair<MapSchemeLine, MapSchemeStation>? {
        for (line in scheme.lines) {
            for (station in line.stations) {
                val rect = station.labelPosition
                val point = station.position
                if ((rect != null && rect.contains(touchPoint)) ||
                    (point != null && point.distance(touchPoint) <= scheme.stationsDiameter)) {
                    return Pair(line, station)
                }
            }
        }
        return null
    }

    @JvmStatic
    fun findStationByUid(scheme: MapScheme, uid: Long): Pair<MapSchemeLine, MapSchemeStation>? {
        for (line in scheme.lines) {
            for (station in line.stations) {
                if (station.uid.toLong() == uid) {
                    return Pair(line, station)
                }
            }
        }
        return null
    }
}
