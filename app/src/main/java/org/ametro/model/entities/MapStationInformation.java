package org.ametro.model.entities;

public class MapStationInformation {

    private final String line;
    private final String station;
    private final String mapFilePath;
    private final String caption;
    private final String about;

    public MapStationInformation(String line, String station, String mapFilePath, String caption, String about) {
        this.line = line;
        this.station = station;
        this.mapFilePath = mapFilePath;
        this.caption = caption;
        this.about = about;
    }

    public String getLine() {
        return line;
    }

    public String getStation() {
        return station;
    }

    public String getMapFilePath() {
        return mapFilePath;
    }

    public String getCaption() {
        return caption;
    }

    public String getAbout() {
        return about;
    }
}
