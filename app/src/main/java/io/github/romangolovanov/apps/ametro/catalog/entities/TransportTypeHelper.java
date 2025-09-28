package io.github.romangolovanov.apps.ametro.catalog.entities;


import java.util.HashSet;
import java.util.Set;

public class TransportTypeHelper {

    public static TransportType parseTransportType(String transportTypeName)
    {
        switch (transportTypeName){
            case "Метро": return TransportType.Subway;
            case "Трамвай": return TransportType.Tram;
            case "Автобус": return TransportType.Bus;
            case "Электричка": return TransportType.Train;
            case "Речной трамвай": return TransportType.WaterBus;
            case "Троллейбус": return TransportType.TrolleyBus;
            case "Фуникулер": return TransportType.CableWay;
            default: return TransportType.Unknown;
        }
    }

    public static TransportType[] parseTransportTypes(String[] names) {
        Set<TransportType> types = new HashSet<>();
        for(String name : names){
            types.add(parseTransportType(name));
        }
        return types.toArray(new TransportType[0]);
    }
}
