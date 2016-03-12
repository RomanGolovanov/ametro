package org.ametro.model.serialization;

import com.fasterxml.jackson.databind.JsonNode;

import org.ametro.model.entities.MapLocale;
import org.ametro.model.entities.MapScheme;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeSegment;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.model.entities.MapSchemeTransfer;

public class SchemeTypes {

    public static MapScheme asMapScheme(GlobalIdentifierProvider identifierProvider, JsonNode node, MapLocale locale) {
        return new MapScheme(
                locale,
                node.get("name").asText(),
                node.get("name_text_id").asInt(),
                node.get("type_text_id").asInt(),
                CommonTypes.asStringArray(node.get("images")),
                node.get("stations_diameter").asDouble(),
                node.get("lines_width").asDouble(),
                node.get("upper_case").asBoolean(),
                node.get("word_wrap").asBoolean(),
                CommonTypes.asStringArray(node.get("transports")),
                CommonTypes.asStringArray(node.get("default_transports")),
                asMapSchemeLineArray(identifierProvider, node.get("lines"), locale),
                asMapSchemeTransferArray(identifierProvider, node.get("transfers")),

                (int) node.get("width").asDouble(),
                (int) node.get("height").asDouble());
    }

    private static MapSchemeLine[] asMapSchemeLineArray(GlobalIdentifierProvider identifierProvider, JsonNode arrayNode, MapLocale locale) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new MapSchemeLine[0];
        }
        MapSchemeLine[] array = new MapSchemeLine[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);

            array[i] = new MapSchemeLine(
                    locale,
                    node.get("name").asText(),
                    node.get("text_id").asInt(),
                    node.get("line_width").asDouble(),
                    CommonTypes.asColor(node.get("line_color"), CommonTypes.DEFAULT_COLOR),
                    CommonTypes.asColor(node.get("labels_color"), CommonTypes.DEFAULT_COLOR),
                    CommonTypes.asColor(node.get("labels_bg_color"), CommonTypes.DEFAULT_LABEL_BG_COLOR),
                    CommonTypes.asRect(node.get("rect")),
                    asMapSchemeStationArray(node.get("stations"), locale),
                    asMapSchemeSegmentsArray(identifierProvider, node.get("segments")));
        }
        return array;
    }

    private static MapSchemeStation[] asMapSchemeStationArray(JsonNode arrayNode, MapLocale locale) {
        MapSchemeStation[] array = new MapSchemeStation[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);
            array[i] = new MapSchemeStation(
                    locale,
                    node.get("uid").asInt(),
                    node.get("name").asText(),
                    node.get("text_id").asInt(),
                    CommonTypes.asRect(node.get("rect")),
                    CommonTypes.asPoint(node.get("coord")),
                    node.get("is_working").asBoolean());
        }
        return array;
    }

    private static MapSchemeSegment[] asMapSchemeSegmentsArray(GlobalIdentifierProvider identifierProvider, JsonNode arrayNode) {
        MapSchemeSegment[] array = new MapSchemeSegment[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);

            array[i] = new MapSchemeSegment(
                    identifierProvider.getSegmentUid(),
                    node.get(0).asInt(),
                    node.get(1).asInt(),
                    CommonTypes.asPointArray(node.get(2)),
                    node.get(3).asBoolean());
        }
        return array;
    }

    private static MapSchemeTransfer[] asMapSchemeTransferArray(GlobalIdentifierProvider identifierProvider, JsonNode arrayNode) {
        if (arrayNode == null || arrayNode.isNull()) {
            return new MapSchemeTransfer[0];
        }
        MapSchemeTransfer[] array = new MapSchemeTransfer[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode node = arrayNode.get(i);

            array[i] = new MapSchemeTransfer(
                    identifierProvider.getTransferUid(),
                    node.get(0).asInt(),
                    node.get(1).asInt(),
                    CommonTypes.asPoint(node.get(2)),
                    CommonTypes.asPoint(node.get(3)));
        }
        return array;
    }
}
