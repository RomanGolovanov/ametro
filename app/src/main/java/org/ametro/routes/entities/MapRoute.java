package org.ametro.routes.entities;

public class MapRoute {

    private final MapRoutePart[] parts;

    public MapRoute(MapRoutePart[] parts) {
        this.parts = parts;
    }

    public MapRoutePart[] getParts() {
        return parts;
    }

    public int getDelay(){
        int delay = 0;
        for(MapRoutePart part : parts){
            delay += part.getDelay();
        }
        return delay;
    }
}

