package io.github.romangolovanov.apps.ametro.model.entities

class MapSchemeLine(
    private val locale: MapLocale,
    val name: String,
    private val nameTextId: Int,
    val lineWidth: Double,
    val lineColor: Int,
    val labelColor: Int,
    val labelBackgroundColor: Int,
    val labelPosition: MapRect?,
    val stations: Array<MapSchemeStation>,
    val segments: Array<MapSchemeSegment>
) {
    val displayName: String? get() = locale.getText(nameTextId)
}
