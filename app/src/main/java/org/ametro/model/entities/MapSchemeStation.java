package org.ametro.model.entities;

public class MapSchemeStation {
    private final MapLocale locale;
    private final int uid;
    private final String name;
    private final int nameTextId;
    private final MapRect labelPosition;
    private final MapPoint position;
    private final boolean isWorking;

    public MapSchemeStation(MapLocale locale, int uid, String name, int nameTextId, MapRect labelPosition, MapPoint position, boolean isWorking) {
        this.locale = locale;
        this.uid = uid;
        this.name = name;
        this.nameTextId = nameTextId;
        this.labelPosition = labelPosition;
        this.position = position;
        this.isWorking = isWorking;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return locale.getText(nameTextId);
    }

    public MapRect getLabelPosition() {
        return labelPosition;
    }

    public MapPoint getPosition() {
        return position;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public int getUid() {
        return uid;
    }
}
