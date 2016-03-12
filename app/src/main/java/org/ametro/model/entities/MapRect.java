package org.ametro.model.entities;

public class MapRect {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public MapRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(MapPoint point) {
        return point.x >= x && point.x <= (x + width) && point.y >= y && point.y <= (y + height);
    }
}
