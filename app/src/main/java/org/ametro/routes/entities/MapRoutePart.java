package org.ametro.routes.entities;

public class MapRoutePart {

    private final int from;
    private final int to;
    private final long delay;

    public MapRoutePart(int from, int to, long delay) {
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

    public long getDelay() {
        return delay;
    }
}

