package io.github.romangolovanov.apps.ametro.routes.entities;

import io.github.romangolovanov.apps.ametro.model.MapContainer;
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme;

import java.util.Set;

public class MapRouteQueryParameters {

    private MapContainer container;
    private Set<String> enabledTransportsSet;
    private final Integer delayIndex;
    private int beginStationUid;
    private int endStationUid;


    public MapRouteQueryParameters(MapContainer container, Set<String> enabledTransportsSet, Integer delayIndex, int beginStationUid, int endStationUid) {
        this.container = container;
        this.enabledTransportsSet = enabledTransportsSet;
        this.delayIndex = delayIndex;
        this.beginStationUid = beginStationUid;
        this.endStationUid = endStationUid;
    }

    public int getStationCount() {
        return container.getMaxStationUid()+1;
    }

    public MapTransportScheme[] getEnabledTransportsSchemes() {
        return container.getTransportSchemes(enabledTransportsSet);
    }

    public Integer getDelayIndex() {
        return delayIndex;
    }



    public int getBeginStationUid() {
        return beginStationUid;
    }

    public int getEndStationUid() {
        return endStationUid;
    }
}
