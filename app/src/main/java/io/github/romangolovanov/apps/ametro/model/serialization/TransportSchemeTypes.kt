package io.github.romangolovanov.apps.ametro.model.serialization

import com.fasterxml.jackson.databind.JsonNode

import io.github.romangolovanov.apps.ametro.model.entities.MapTransportLine
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportSegment
import io.github.romangolovanov.apps.ametro.model.entities.MapTransportTransfer

object TransportSchemeTypes {

    @JvmStatic
    fun asMapTransportScheme(node: JsonNode): MapTransportScheme {
        return MapTransportScheme(
            node.get("name").asText(),
            node.get("type_name").asText(),
            asMapTransportLineArray(node.get("lines")),
            asMapTransportTransferDictionary(node.get("transfers"))
        )
    }

    private fun asMapTransportLineArray(arrayNode: JsonNode?): Array<MapTransportLine> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapTransportLine(
                node.get("name").asText(),
                node.get("scheme").asText(),
                asMapTransportSegmentArray(node.get("segments")),
                CommonTypes.asIntArray(node.get("delays"))
            )
        }
    }

    private fun asMapTransportTransferDictionary(arrayNode: JsonNode?): Array<MapTransportTransfer> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapTransportTransfer(
                node.get(0).asInt(),
                node.get(1).asInt(),
                node.get(2).asInt(),
                node.get(3).asBoolean()
            )
        }
    }

    private fun asMapTransportSegmentArray(arrayNode: JsonNode?): Array<MapTransportSegment> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapTransportSegment(
                node.get(0).asInt(),
                node.get(1).asInt(),
                node.get(2).asInt()
            )
        }
    }
}
