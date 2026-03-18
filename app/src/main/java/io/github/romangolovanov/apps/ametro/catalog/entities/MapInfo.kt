package io.github.romangolovanov.apps.ametro.catalog.entities

open class MapInfo(
    src: MapInfoEntity,
    val city: String,
    val country: String,
    val iso: String
) : MapInfoEntity(src) {

    constructor(src: MapInfo) : this(src as MapInfoEntity, src.city, src.country, src.iso)

    constructor(
        city_id: Int, fileName: String, latitude: Double, longitude: Double,
        size: Int, timestamp: Int, types: Array<TransportType>, uid: String,
        city: String, country: String, iso: String
    ) : this(
        MapInfoEntity(city_id, fileName, latitude, longitude, size, timestamp, types, uid),
        city, country, iso
    )

    override fun toString(): String = "$city, $country"
}
