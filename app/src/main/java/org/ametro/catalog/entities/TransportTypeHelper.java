package org.ametro.catalog.entities;


import java.util.HashSet;
import java.util.Set;

public class TransportTypeHelper {

    public static String formatTransportTypeName(TransportType transportType)
    {
        switch (transportType){
            case Subway : return "Метро";
            case Tram : return "Трамвай";
            case Bus : return "Автобус";
            case Train : return "Электричка";
            case WaterBus : return "Речной трамвай";
            case TrolleyBus : return "Троллейбус";
            case CableWay : return "Фуникулер";
            default: return "Unknown";
        }
    }
    
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
        return types.toArray(new TransportType[types.size()]);
    }
}
