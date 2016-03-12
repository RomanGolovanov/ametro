package org.ametro.model.entities;

public class MapSchemeTransfer {

    private final int uid;
    private final int from;
    private final int to;
    private final MapPoint fromStationPosition;
    private final MapPoint toStationPosition;

    public MapSchemeTransfer(int uid, int from, int to, MapPoint fromStationPosition, MapPoint toStationPosition) {
        this.uid = uid;
        this.from = from;
        this.to = to;
        this.fromStationPosition = fromStationPosition;
        this.toStationPosition = toStationPosition;
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

    public MapPoint getFromStationPosition() {
        return fromStationPosition;
    }

    public MapPoint getToStationPosition() {
        return toStationPosition;
    }
}
