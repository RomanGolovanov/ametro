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
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is MapInfoEntity) return fileName == other.fileName
        return false
    }

    override fun hashCode(): Int = fileName.hashCode()
}
