package org.ametro.model.entities;

import org.ametro.render.utils.Algorithms;

public class MapPoint{
    public final float x;
    public final float y;

    public MapPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public double distance(MapPoint point) {

        return Algorithms.calculateDistance(x,y,point.x, point.y);
    }
}
