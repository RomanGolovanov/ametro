package io.github.romangolovanov.apps.ametro.catalog.entities

open class MapInfoEntity(
    val uid: String,
    val cityId: Int,
    val types: Array<TransportType>,
    val fileName: String,
    val size: Int,
    val timestamp: Int,
    val latitude: Double,
    val longitude: Double
) {
    constructor(city_id: Int, fileName: String, latitude: Double, longitude: Double,
                size: Int, timestamp: Int, types: Array<TransportType>, uid: String)
        : this(uid, city_id, types, fileName, size, timestamp, latitude, longitude)

    constructor(src: MapInfoEntity) : this(
        src.uid, src.cityId, src.types, src.fileName, src.size, src.timestamp, src.latitude, src.longitude
    )

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is MapInfoEntity) return fileName == other.fileName
        return false
    }

    override fun hashCode(): Int = fileName.hashCode()
}
