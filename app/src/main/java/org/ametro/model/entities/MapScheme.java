package org.ametro.model.entities;

import java.util.HashMap;
import java.util.Map;

public class MapScheme {

    private final MapLocale locale;
    private final String name;
    private final int nameTextId;
    private final int typeTextId;

    private final String[] imageNames;
    private final double stationsDiameter;
    private final double linesWidth;
    private final boolean upperCase;
    private final boolean wordWrap;

    private final String[] transports;
    private final String[] defaultTransports;

    private final MapSchemeLine[] lines;
    private final MapSchemeTransfer[] transfers;

    private int width;
    private int height;

    private Map<String, Object> images;

    public MapScheme(MapLocale locale, String name, int nameTextId, int typeTextId, String[] imageNames, double stationsDiameter, double linesWidth, boolean upperCase, boolean wordWrap, String[] transports, String[] defaultTransports, MapSchemeLine[] lines, MapSchemeTransfer[] transfers, int width, int height) {
        this.locale = locale;
        this.name = name;
        this.nameTextId = nameTextId;
        this.typeTextId = typeTextId;
        this.imageNames = imageNames;
        this.stationsDiameter = stationsDiameter;
        this.linesWidth = linesWidth;
        this.upperCase = upperCase;
        this.wordWrap = wordWrap;
        this.transports = transports;
        this.defaultTransports = defaultTransports;
        this.lines = lines;
        this.transfers = transfers;
        this.width = width;
        this.height = height;
        this.images = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return locale.getText(nameTextId);
    }

    public String getTypeName() {
        return locale.getText(typeTextId);
    }

    public String[] getImageNames() {
        return imageNames;
    }

    public double getStationsDiameter() {
        return stationsDiameter;
    }

    public double getLinesWidth() {
        return linesWidth;
    }

    public boolean isUpperCase() {
        return upperCase;
    }

    public boolean isWordWrap() {
        return wordWrap;
    }

    public String[] getTransports() {
        return transports;
    }

    public String[] getDefaultTransports() {
        return defaultTransports;
    }

    public MapSchemeLine[] getLines() {
        return lines;
    }

    public MapSchemeTransfer[] getTransfers() {
        return transfers;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Object getBackgroundObject(String name){
        return images.get(name);
    }

    public void setBackgroundObject(String name, Object image){
        images.put(name, image);
    }

}
