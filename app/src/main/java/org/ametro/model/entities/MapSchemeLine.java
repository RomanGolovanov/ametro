package org.ametro.model.entities;

public class MapSchemeLine {

    private final MapLocale locale;
    private final String name;
    private final int nameTextId;
    private final double lineWidth;

    private final int lineColor;
    private final int labelColor;
    private final int labelBackgroundColor;
    private final MapRect labelPosition;

    private final MapSchemeStation[] stations;
    private final MapSchemeSegment[] segments;

    public MapSchemeLine(
            MapLocale locale,
            String name,
            int nameTextId,
            double lineWidth,
            int lineColor,
            int labelColor,
            int labelBackgroundColor,
            MapRect labelPosition,
            MapSchemeStation[] stations,
            MapSchemeSegment[] segments) {
        this.locale = locale;
        this.name = name;
        this.nameTextId = nameTextId;
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.labelColor = labelColor;
        this.labelBackgroundColor = labelBackgroundColor;
        this.labelPosition = labelPosition;
        this.stations = stations;
        this.segments = segments;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return locale.getText(nameTextId);
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public int getLineColor() {
        return lineColor;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public int getLabelBackgroundColor() {
        return labelBackgroundColor;
    }

    public MapRect getLabelPosition() {
        return labelPosition;
    }

    public MapSchemeStation[] getStations() {
        return stations;
    }

    public MapSchemeSegment[] getSegments() {
        return segments;
    }
}

