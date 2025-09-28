package io.github.romangolovanov.apps.ametro.model;


import android.util.Pair;

import io.github.romangolovanov.apps.ametro.model.entities.MapPoint;
import io.github.romangolovanov.apps.ametro.model.entities.MapRect;
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation;

public class ModelUtil {

    public static Pair<MapSchemeLine, MapSchemeStation> findTouchedStation(MapScheme scheme, MapPoint touchPoint) {
        for (MapSchemeLine line : scheme.getLines()) {
            for (MapSchemeStation station : line.getStations()) {
                MapRect rect = station.getLabelPosition();
                MapPoint point = station.getPosition();
                if ((rect != null && rect.contains(touchPoint)) ||
                        (point != null && point.distance(touchPoint) <= scheme.getStationsDiameter())) {
                    return new Pair<>(line, station);
                }
            }
        }
        return null;
    }

    public static Pair<MapSchemeLine, MapSchemeStation> findStationByUid(MapScheme scheme, long uid) {
        for (MapSchemeLine line : scheme.getLines()) {
            for (MapSchemeStation station : line.getStations()) {
                if (station.getUid() == uid) {
                    return new Pair<>(line, station);
                }
            }
        }
        return null;
    }

}
