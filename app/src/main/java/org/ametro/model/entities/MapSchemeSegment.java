package org.ametro.model.entities;

public class MapSchemeSegment{
    private final int uid;
    private final int from;
    private final int to;
    private final MapPoint[] points;
    private final boolean isWorking;

    public MapSchemeSegment(int uid, int from, int to, MapPoint[] points, boolean isWorking) {
        this.uid = uid;
        this.from = from;
        this.to = to;
        this.points = points;
        this.isWorking = isWorking;
    }

    public int getUid() {
        return uid;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public MapPoint[] getPoints() {
        return points;
    }

    public boolean isWorking() {
        return isWorking;
    }
}
