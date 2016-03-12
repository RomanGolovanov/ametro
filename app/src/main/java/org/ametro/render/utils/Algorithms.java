package org.ametro.render.utils;

import android.graphics.PointF;

public class Algorithms {

    public static float calculateDistance(PointF p1, PointF p2) {
        return calculateDistance(p1.x, p1.y, p2.x, p2.y);
    }

    public static float calculateDistance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float)Math.sqrt(x * x + y * y);
    }
}

