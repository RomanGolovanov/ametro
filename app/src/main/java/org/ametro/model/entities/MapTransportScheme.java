package org.ametro.model.entities;

public class MapTransportScheme {
    private final String name;
    private final String type;

    private final MapTransportLine[] lines;
    private final MapTransportTransfer[] transfers;

    public MapTransportScheme(String name, String type, MapTransportLine[] lines, MapTransportTransfer[] transfers) {
        this.name = name;
        this.type = type;
        this.lines = lines;
        this.transfers = transfers;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public MapTransportLine[] getLines() {
        return lines;
    }

    public MapTransportTransfer[] getTransfers() {
        return transfers;
    }
}
