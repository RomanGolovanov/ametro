package org.ametro.catalog.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.ametro.catalog.entities.MapInfoEntity;
import org.ametro.catalog.entities.MapInfoEntityName;
import org.ametro.catalog.entities.TransportType;
import org.ametro.catalog.entities.TransportTypeHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapCatalogSerializer {

    private static final ObjectReader reader;

    static {
        reader = new ObjectMapper().reader();
    }

    public static MapInfoEntity[] deserializeMapInfoArray(String jsonText) throws SerializationException {
        try {
            JsonNode json = reader.readTree(jsonText);
            if(json == null){
                return new MapInfoEntity[0];
            }
            MapInfoEntity[] maps = new MapInfoEntity[json.size()];
            for (int i = 0; i < maps.length; i++) {
                JsonNode jsonMap = json.get(i);
                maps[i] = new MapInfoEntity(
                        jsonMap.get("city_id").asInt(),
                        jsonMap.get("file").asText(),
                        jsonMap.get("latitude").asDouble(),
                        jsonMap.get("longitude").asDouble(),
                        jsonMap.get("size").asInt(),
                        jsonMap.get("timestamp").asInt(),
                        deserializeTransports(jsonMap.get("transports")),
                        jsonMap.get("uid").asText()
                );
            }
            return maps;
        }catch (Exception ex){
            throw new SerializationException(ex);
        }
    }

    public static MapInfoEntityName[] deserializeLocalization(String jsonText) throws SerializationException {
        try {
            JsonNode json = reader.readTree(jsonText);
            if(json == null){
                return new MapInfoEntityName[0];
            }
            MapInfoEntityName[] entities = new MapInfoEntityName[json.size()];
            for (int i = 0; i < entities.length; i++) {
                JsonNode city = json.get(i);
                entities[i] = new MapInfoEntityName(
                        city.get(0).asInt(),
                        city.get(1).asText(),
                        city.get(2).asText(),
                        city.get(3).asText());
            }
            return entities;
        }catch (Exception ex){
            throw new SerializationException(ex);
        }

    }
    public static String serializeMapInfoArray(MapInfoEntity[] maps) throws SerializationException {
        try {

            JSONArray jsonMaps = new JSONArray();
            for (MapInfoEntity map : maps) {
                JSONObject jsonMap = new JSONObject();
                jsonMap.put("city_id", map.getCityId());
                jsonMap.put("file", map.getFileName());
                jsonMap.put("latitude", map.getLatitude());
                jsonMap.put("longitude", map.getLongitude());
                jsonMap.put("size", map.getSize());
                jsonMap.put("timestamp", map.getTimestamp());
                jsonMap.put("transports", serializeTransports(map.getTypes()));
                jsonMap.put("uid", map.getUid());
                jsonMaps.put(jsonMap);
            }
            return jsonMaps.toString();
        }catch (Exception ex){
            throw new SerializationException(ex);
        }
    }

    private static TransportType[] deserializeTransports(JsonNode transports) {
        TransportType[] types = new TransportType[transports.size()];
        for(int i=0; i<types.length;i++){
            types[i] = TransportTypeHelper.parseTransportType(transports.get(i).asText());
        }
        return types;
    }

    private static JSONArray serializeTransports(TransportType[] transportTypes) {
        JSONArray types = new JSONArray();
        for (TransportType transportType : transportTypes) {
            types.put(TransportTypeHelper.formatTransportTypeName(transportType));
        }
        return types;
    }}
