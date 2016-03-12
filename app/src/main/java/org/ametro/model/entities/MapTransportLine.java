package org.ametro.model.entities;

public class MapTransportLine {
    private final String name;
    private final String map;
    private final MapTransportSegment[] segments;
    private final int[] delays;

    public MapTransportLine(String name, String map, MapTransportSegment[] segments, int[] delays) {
        this.name = name;
        this.map = map;
        this.segments = segments;
        this.delays = delays;
    }

    public String getName() {
        return name;
    }

    public String getMap() {
        return map;
    }

    public MapTransportSegment[] getSegments() {
        return segments;
    }

    public int[] getDelays() {
        return delays;
    }
}
