package org.ametro.model.entities;

public class MapTransportSegment {
    private final int from;
    private final int to;
    private final int delay;

    public MapTransportSegment(int from, int to, int delay) {
        this.from = from;
        this.to = to;
        this.delay = delay;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getDelay() {
        return delay;
    }
}
