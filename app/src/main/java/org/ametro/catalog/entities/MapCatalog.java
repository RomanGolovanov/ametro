package org.ametro.catalog.entities;

public class MapCatalog {

    private final MapInfo[] maps;

    public MapCatalog(MapInfo[] maps) {
        this.maps = maps;
    }

    public MapInfo[] getMaps() {
        return maps;
    }

    public MapInfo findMap(String fileName) {
        for (MapInfo m : maps) {
            if (m.getFileName().equals(fileName))
                return m;
        }
        return null;
    }
}
