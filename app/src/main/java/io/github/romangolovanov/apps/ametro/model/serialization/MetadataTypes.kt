package io.github.romangolovanov.apps.ametro.model.serialization

import com.fasterxml.jackson.databind.JsonNode

import io.github.romangolovanov.apps.ametro.model.entities.MapDelay
import io.github.romangolovanov.apps.ametro.model.entities.MapDelayTimeRange
import io.github.romangolovanov.apps.ametro.model.entities.MapDelayType
import io.github.romangolovanov.apps.ametro.model.entities.MapDelayWeekdayType
import io.github.romangolovanov.apps.ametro.model.entities.MapLocale
import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata
import io.github.romangolovanov.apps.ametro.model.entities.MapStationInformation

object MetadataTypes {

    fun asStationInformation(arrayNode: JsonNode?): Array<MapStationInformation> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i ->
            val node = arrayNode.get(i)
            MapStationInformation(
                node.get("line").asText(),
                node.get("station").asText(),
                node.get("image").asText(),
                node.get("caption").asText(),
                null
            )
        }
    }

    fun asTextMap(node: JsonNode): Map<Int, String> {
        return node.fields().asSequence()
            .associate { entry -> entry.key.toInt() to node.get(entry.key).asText() }
    }

    fun asMetadata(node: JsonNode, locale: MapLocale?): MapMetadata {
        return MapMetadata(
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
        )
    }

    private fun asMapDelayArray(arrayNode: JsonNode?, locale: MapLocale?): Array<MapDelay> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i -> asMapDelay(arrayNode.get(i), locale) }
    }

    private fun asMapDelay(node: JsonNode, locale: MapLocale?): MapDelay {
        val nameNode = node.get("name_id")
        return MapDelay(
            locale,
            if (nameNode != null && !nameNode.isNull) nameNode.asInt() else null,
            asMapDelayType(node.get("type")),
            asWeekdayType(node.get("weekdays")),
            asTimeRangeArray(node.get("ranges"))
        )
    }

    private fun asTimeRangeArray(arrayNode: JsonNode?): Array<MapDelayTimeRange> {
        if (arrayNode == null || arrayNode.isNull) return emptyArray()
        return Array(arrayNode.size()) { i ->
            val rangeParts = arrayNode.get(i).asText().split(Regex("[:-]+"))
            MapDelayTimeRange(
                rangeParts[0].toInt(),
                rangeParts[1].toInt(),
                rangeParts[2].toInt(),
                rangeParts[3].toInt()
            )
        }
    }

    private fun asWeekdayType(node: JsonNode?): MapDelayWeekdayType {
        if (node == null || node.isNull) return MapDelayWeekdayType.NotDefined
        return when (node.asText()) {
            "monday" -> MapDelayWeekdayType.Monday
            "tuesday" -> MapDelayWeekdayType.Tuesday
            "wednesday" -> MapDelayWeekdayType.Wednesday
            "thursday" -> MapDelayWeekdayType.Thursday
            "friday" -> MapDelayWeekdayType.Friday
            "saturday" -> MapDelayWeekdayType.Saturday
            "sunday" -> MapDelayWeekdayType.Sunday
            "workdays" -> MapDelayWeekdayType.Workdays
            "weekend" -> MapDelayWeekdayType.Weekend
            else -> throw RuntimeException("Invalid weekday value ${node.asText()}")
        }
    }

    private fun asMapDelayType(node: JsonNode?): MapDelayType {
        if (node == null || node.isNull) return MapDelayType.NotDefined
        return when (node.asText()) {
            "custom" -> MapDelayType.Custom
            "day" -> MapDelayType.Day
            "night" -> MapDelayType.Night
            "evening" -> MapDelayType.Evening
            "mourning" -> MapDelayType.Mourning
            "rush" -> MapDelayType.Rush
            "direct" -> MapDelayType.Direct
            "west-north" -> MapDelayType.WestNorth
            "west-south" -> MapDelayType.WestSouth
            "west-east" -> MapDelayType.WestEast
            "east-north" -> MapDelayType.EastNorth
            "east-south" -> MapDelayType.EastSouth
            "east-west" -> MapDelayType.EastWest
            "north-east" -> MapDelayType.NorthEast
            "north-west" -> MapDelayType.NorthWest
            "north-south" -> MapDelayType.NorthSouth
            "south-east" -> MapDelayType.SouthEast
            "south-west" -> MapDelayType.SouthWest
            "south-north" -> MapDelayType.SouthNorth
            else -> throw RuntimeException("Invalid delay type value ${node.asText()}")
        }
    }

    private fun asSchemeDictionary(arrayNode: JsonNode?, locale: MapLocale?): Map<String, MapMetadata.Scheme> {
        if (arrayNode == null || arrayNode.isNull) return emptyMap()
        return (0 until arrayNode.size()).associate { i ->
            val node = arrayNode.get(i)
            val scheme = MapMetadata.Scheme(
                locale,
                node.get("name").asText(),
                node.get("name_text_id").asInt(),
                node.get("type_name").asText(),
                node.get("type_text_id").asInt(),
                node.get("file").asText(),
                CommonTypes.asStringArray(node.get("transports")),
                CommonTypes.asStringArray(node.get("default_transports")),
                node.get("root").asBoolean()
            )
            scheme.name to scheme
        }
    }

    private fun asTransportSchemeDictionary(arrayNode: JsonNode?): Map<String, MapMetadata.TransportScheme> {
        if (arrayNode == null || arrayNode.isNull) return emptyMap()
        return (0 until arrayNode.size()).associate { i ->
            val node = arrayNode.get(i)
            val scheme = MapMetadata.TransportScheme(
                node.get("name").asText(),
                node.get("file").asText(),
                node.get("type").asText()
            )
            scheme.name to scheme
        }
    }
}
