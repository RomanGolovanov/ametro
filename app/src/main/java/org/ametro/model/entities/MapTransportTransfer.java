package org.ametro.model.entities;

public class MapTransportTransfer{

    private final int from;
    private final int to;
    private final int delay;
    private final boolean visible;

    public MapTransportTransfer(int from, int to, int delay, boolean visible) {
        this.from = from;
        this.to = to;
        this.delay = delay;
        this.visible = visible;
    }

    public int getDelay() {
        return delay;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public boolean isVisible() {
        return visible;
    }
}
