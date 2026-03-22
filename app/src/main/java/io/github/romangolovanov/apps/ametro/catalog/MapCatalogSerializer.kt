package io.github.romangolovanov.apps.ametro.catalog

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfoEntity
import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfoEntityName
import io.github.romangolovanov.apps.ametro.catalog.entities.TransportType
import io.github.romangolovanov.apps.ametro.catalog.entities.TransportTypeHelper

object MapCatalogSerializer {

    private val reader = ObjectMapper().reader()

    @Throws(SerializationException::class)
    fun deserializeMapInfoArray(jsonText: String): Array<MapInfoEntity> {
        try {
            val json = reader.readTree(jsonText) ?: return emptyArray()
            return Array(json.size()) { i ->
                val jsonMap = json.get(i)
                MapInfoEntity(
                    uid = jsonMap.get("uid").asText(),
                    cityId = jsonMap.get("city_id").asInt(),
                    types = deserializeTransports(jsonMap.get("transports")),
                    fileName = jsonMap.get("file").asText(),
                    size = jsonMap.get("size").asInt(),
                    timestamp = jsonMap.get("timestamp").asInt(),
                    latitude = jsonMap.get("latitude").asDouble(),
                    longitude = jsonMap.get("longitude").asDouble()
                )
            }
        } catch (ex: Exception) {
            throw SerializationException(ex)
        }
    }

    @Throws(SerializationException::class)
    fun deserializeLocalization(jsonText: String): Array<MapInfoEntityName> {
        try {
            val json = reader.readTree(jsonText) ?: return emptyArray()
            return Array(json.size()) { i ->
                val city = json.get(i)
                MapInfoEntityName(
                    city.get(0).asInt(),
                    city.get(1).asText(),
                    city.get(2).asText(),
                    city.get(3).asText()
                )
            }
        } catch (ex: Exception) {
            throw SerializationException(ex)
        }
    }

    private fun deserializeTransports(transports: com.fasterxml.jackson.databind.JsonNode): Array<TransportType> =
        Array(transports.size()) { i -> TransportTypeHelper.parseTransportType(transports.get(i).asText()) }
}
