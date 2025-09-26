package org.ametro.ng.model;


import android.util.Pair;

import org.ametro.ng.model.entities.MapPoint;
import org.ametro.ng.model.entities.MapRect;
import org.ametro.ng.model.entities.MapScheme;
import org.ametro.ng.model.entities.MapSchemeLine;
import org.ametro.ng.model.entities.MapSchemeStation;

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
