package org.ametro.model;


import android.support.v4.util.Pair;

import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapRect;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeStation;

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
