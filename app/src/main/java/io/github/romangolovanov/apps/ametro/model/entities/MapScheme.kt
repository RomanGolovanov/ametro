package io.github.romangolovanov.apps.ametro.model.entities

class MapScheme(
    private val locale: MapLocale,
    val name: String,
    private val nameTextId: Int,
    private val typeTextId: Int,
    val imageNames: Array<String>,
    val stationsDiameter: Double,
    val linesWidth: Double,
    val isUpperCase: Boolean,
    val isWordWrap: Boolean,
    val transports: Array<String>,
    val defaultTransports: Array<String>,
    val lines: Array<MapSchemeLine>,
    val transfers: Array<MapSchemeTransfer>,
    var width: Int,
    var height: Int
) {
    var images: MutableMap<String, Any> = mutableMapOf()

    val displayName: String? get() = locale.getText(nameTextId)
    val typeName: String? get() = locale.getText(typeTextId)

    fun getBackgroundObject(name: String): Any? = images[name]
    fun setBackgroundObject(name: String, image: Any) { images[name] = image }
}
