package io.github.romangolovanov.apps.ametro.catalog.entities

open class MapInfo(
    src: MapInfoEntity,
    val city: String,
    val country: String,
    val iso: String
) : MapInfoEntity(src.uid, src.cityId, src.types, src.fileName, src.size, src.timestamp, src.latitude, src.longitude) {

    constructor(src: MapInfo) : this(src as MapInfoEntity, src.city, src.country, src.iso)

    override fun toString(): String = "$city, $country"
}
