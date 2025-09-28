package io.github.romangolovanov.apps.ametro.ui.loaders;

import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo;

public class ExtendedMapInfo extends MapInfo {

    private ExtendedMapStatus status;
    private boolean selected;

    public ExtendedMapInfo(MapInfo map, ExtendedMapStatus status) {
        super(map);
        this.status = status;
    }

    public ExtendedMapStatus getStatus() {
        return status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

