package org.ametro.model.serialization;

import com.fasterxml.jackson.databind.JsonNode;

import org.ametro.model.entities.MapTransportLine;
import org.ametro.model.entities.MapTransportScheme;
import org.ametro.model.entities.MapTransportSegment;
import org.ametro.model.entities.MapTransportTransfer;

public class TransportSchemeTypes {

    public static MapTransportScheme asMapTransportScheme(JsonNode node) {
        return new MapTransportScheme(
                node.get("name").asText(),
                node.get("type_name").asText(),
                asMapTransportLineArray(node.get("lines")),
                asMapTransportTransferDictionary(node.get("transfers"))
        );
    }

    private static MapTransportLine[] asMapTransportLineArray(JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new MapTransportLine[0];
        }
        MapTransportLine[] array = new MapTransportLine[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);
            array[i] = new MapTransportLine(
                    node.get("name").asText(),
                    node.get("scheme").asText(),
                    asMapTransportSegmentArray(node.get("segments")),
                    CommonTypes.asIntArray(node.get("delays"))
                    );

        }
        return array;
    }

    private static MapTransportTransfer[] asMapTransportTransferDictionary(JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new MapTransportTransfer[0];
        }
        MapTransportTransfer[] array = new MapTransportTransfer[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);
            array[i] = new MapTransportTransfer(
                    node.get(0).asInt(),
                    node.get(1).asInt(),
                    node.get(2).asInt(),
                    node.get(3).asBoolean()
            );
        }
        return array;
    }

    private static MapTransportSegment[] asMapTransportSegmentArray(JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new MapTransportSegment[0];
        }
        MapTransportSegment[] array = new MapTransportSegment[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);
            array[i] = new MapTransportSegment(
                    node.get(0).asInt(),
                    node.get(1).asInt(),
                    node.get(2).asInt()
            );
        }
        return array;
    }
}
