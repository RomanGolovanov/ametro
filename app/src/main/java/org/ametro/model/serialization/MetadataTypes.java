package org.ametro.model.serialization;

import com.fasterxml.jackson.databind.JsonNode;

import org.ametro.model.entities.MapDelay;
import org.ametro.model.entities.MapDelayType;
import org.ametro.model.entities.MapLocale;
import org.ametro.model.entities.MapMetadata;
import org.ametro.model.entities.MapStationInformation;
import org.ametro.model.entities.MapDelayTimeRange;
import org.ametro.model.entities.MapDelayWeekdayType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MetadataTypes {

    public static MapStationInformation[] asStationInformation(JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new MapStationInformation[0];
        }

        List<MapStationInformation> array = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);

            MapStationInformation scheme = new MapStationInformation(
                    node.get("line").asText(),
                    node.get("station").asText(),
                    node.get("image").asText(),
                    node.get("caption").asText(),
                    null);
            array.add(scheme);
        }
        return array.toArray(new MapStationInformation[array.size()]);
    }

    public static HashMap<Integer, String> asTextMap(JsonNode node){
        HashMap<Integer, String> textMap = new HashMap<>(node.size());

        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        while(iterator.hasNext()){
            Map.Entry<String, JsonNode> entry = iterator.next();
            textMap.put(Integer.parseInt(entry.getKey()), node.get(entry.getKey()).asText());
        }
        return textMap;
    }

    public static MapMetadata asMetadata(JsonNode node, MapLocale locale) {
        return new MapMetadata(
                locale,
                node.get("map_id").asText(),
                node.get("city_id").asInt(),
                node.get("timestamp").asInt(),
                node.get("latitude").asDouble(),
                node.get("longitude").asDouble(),
                asSchemeDictionary(node.get("schemes"), locale),
                asTransportSchemeDictionary(node.get("transports")),
                CommonTypes.asStringArray(node.get("transport_types")),
                asMapDelayArray(node.get("delays"), locale),
                CommonTypes.asStringArray(node.get("locales")),
                node.get("comments_text_id").asInt(),
                node.get("description_text_id").asInt(),
                node.get("file").asText()
        );
    }

    private static MapDelay[] asMapDelayArray(JsonNode arrayNode, MapLocale locale){
        if(arrayNode == null || arrayNode.isNull()){
            return new MapDelay[0];
        }
        MapDelay[] array = new MapDelay[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            array[i] = asMapDelay(arrayNode.get(i), locale);
        }
        return array;
    }

    private static MapDelay asMapDelay(JsonNode node, MapLocale locale){
        JsonNode nameNode = node.get("name_id");
        return new MapDelay(
                locale,
                nameNode!=null && !nameNode.isNull() ? nameNode.asInt() : null,
                asMapDelayType(node.get("type")),
                asWeekdayType(node.get("weekdays")),
                asTimeRangeArray(node.get("ranges"))
        );
    }

    private static MapDelayTimeRange[] asTimeRangeArray(JsonNode arrayNode) {
        if(arrayNode == null || arrayNode.isNull()){
            return new MapDelayTimeRange[0];
        }
        MapDelayTimeRange[] array = new MapDelayTimeRange[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            String[] rangeParts = arrayNode.get(i).asText().split("[:-]+");
            array[i] = new MapDelayTimeRange(
                    Integer.parseInt(rangeParts[0]),
                    Integer.parseInt(rangeParts[1]),
                    Integer.parseInt(rangeParts[2]),
                    Integer.parseInt(rangeParts[3])
            );
        }
        return array;
    }

    private static MapDelayWeekdayType asWeekdayType(final JsonNode node) {
        if(node == null || node.isNull()){
            return MapDelayWeekdayType.NotDefined;
        }
        final String value = node.asText();

        switch (value){
            case "monday": return MapDelayWeekdayType.Monday;
            case "tuesday": return MapDelayWeekdayType.Tuesday;
            case "wednesday": return MapDelayWeekdayType.Wednesday;
            case "thursday": return MapDelayWeekdayType.Thursday;
            case "friday": return MapDelayWeekdayType.Friday;
            case "saturday": return MapDelayWeekdayType.Saturday;
            case "sunday": return MapDelayWeekdayType.Sunday;

            case "workdays": return MapDelayWeekdayType.Workdays;
            case "weekend": return MapDelayWeekdayType.Weekend;
        }
        throw new RuntimeException("Invalid weekday value " + value);
    }

    private static MapDelayType asMapDelayType(final JsonNode node) {
        if(node == null || node.isNull()){
            return MapDelayType.NotDefined;
        }
        final String value = node.asText();
        switch (value){
            case "custom": return MapDelayType.Custom;

            case "day": return MapDelayType.Day;
            case "night": return MapDelayType.Night;
            case "evening": return MapDelayType.Evening;
            case "mourning": return MapDelayType.Mourning;
            case "rush": return MapDelayType.Rush;

            case "direct": return MapDelayType.Direct;

            case "west-north": return MapDelayType.WestNorth;
            case "west-south": return MapDelayType.WestSouth;
            case "west-east": return MapDelayType.WestEast;

            case "east-north": return MapDelayType.EastNorth;
            case "east-south": return MapDelayType.EastSouth;
            case "east-west": return MapDelayType.EastWest;

            case "north-east": return MapDelayType.NorthEast;
            case "north-west": return MapDelayType.NorthWest;
            case "north-south": return MapDelayType.NorthSouth;

            case "south-east": return MapDelayType.SouthEast;
            case "south-west": return MapDelayType.SouthWest;
            case "south-north": return MapDelayType.SouthNorth;
        }
        throw new RuntimeException("Invalid delay type value " + value);
    }


    private static Map<String, MapMetadata.Scheme> asSchemeDictionary(JsonNode arrayNode, MapLocale locale) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new HashMap<>();
        }
        HashMap<String, MapMetadata.Scheme> dict = new HashMap<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);

            MapMetadata.Scheme scheme = new MapMetadata.Scheme(
                    locale,
                    node.get("name").asText(),
                    node.get("name_text_id").asInt(),
                    node.get("type_name").asText(),
                    node.get("type_text_id").asInt(),
                    node.get("file").asText(),
                    CommonTypes.asStringArray(node.get("transports")),
                    CommonTypes.asStringArray(node.get("default_transports")),
                    node.get("root").asBoolean()
            );
            dict.put(scheme.getName(), scheme);
        }
        return dict;
    }

    private static Map<String, MapMetadata.TransportScheme> asTransportSchemeDictionary(JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new HashMap<>();
        }
        HashMap<String, MapMetadata.TransportScheme> dict = new HashMap<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);

            MapMetadata.TransportScheme scheme = new MapMetadata.TransportScheme(
                    node.get("name").asText(),
                    node.get("file").asText(),
                    node.get("type").asText()
            );
            dict.put(scheme.getName(), scheme);
        }
        return dict;
    }

}
