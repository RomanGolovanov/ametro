package io.github.romangolovanov.apps.ametro.model.serialization

import com.fasterxml.jackson.databind.JsonNode

import io.github.romangolovanov.apps.ametro.model.entities.MapLocale
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeSegment
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeTransfer

object SchemeTypes {

    @JvmStatic
    fun asMapScheme(identifierProvider: GlobalIdentifierProvider, node: JsonNode, locale: MapLocale): MapScheme {
        return MapScheme(
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
            node.get("width").asDouble().toInt(),
            node.get("height").asDouble().toInt()
        )
    }

    private fun asMapSchemeLineArray(
        identifierProvider: GlobalIdentifierProvider,
        arrayNode: JsonNode?,
        locale: MapLocale
    ): Array<MapSchemeLine> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapSchemeLine(
                locale,
                node.get("name").asText(),
                node.get("text_id").asInt(),
                node.get("line_width").asDouble(),
                CommonTypes.asColor(node.get("line_color"), CommonTypes.DEFAULT_COLOR),
                CommonTypes.asColor(node.get("labels_color"), CommonTypes.DEFAULT_COLOR),
                CommonTypes.asColor(node.get("labels_bg_color"), CommonTypes.DEFAULT_LABEL_BG_COLOR),
                CommonTypes.asRect(node.get("rect")),
                asMapSchemeStationArray(node.get("stations"), locale),
                asMapSchemeSegmentsArray(identifierProvider, node.get("segments"))
            )
        }
    }

    private fun asMapSchemeStationArray(arrayNode: JsonNode, locale: MapLocale): Array<MapSchemeStation> {
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapSchemeStation(
                locale,
                node.get("uid").asInt(),
                node.get("name").asText(),
                node.get("text_id").asInt(),
                CommonTypes.asRect(node.get("rect")),
                CommonTypes.asPoint(node.get("coord")),
                node.get("is_working").asBoolean()
            )
        }
    }

    private fun asMapSchemeSegmentsArray(
        identifierProvider: GlobalIdentifierProvider,
        arrayNode: JsonNode
    ): Array<MapSchemeSegment> {
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapSchemeSegment(
                identifierProvider.getSegmentUid(),
                node.get(0).asInt(),
                node.get(1).asInt(),
                CommonTypes.asPointArray(node.get(2)),
                node.get(3).asBoolean()
            )
        }
    }

    private fun asMapSchemeTransferArray(
        identifierProvider: GlobalIdentifierProvider,
        arrayNode: JsonNode?
    ): Array<MapSchemeTransfer> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapSchemeTransfer(
                identifierProvider.getTransferUid(),
                node.get(0).asInt(),
                node.get(1).asInt(),
                CommonTypes.asPoint(node.get(2)),
                CommonTypes.asPoint(node.get(3))
            )
        }
    }
}
