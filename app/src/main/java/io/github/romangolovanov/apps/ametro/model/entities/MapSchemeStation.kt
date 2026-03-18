package io.github.romangolovanov.apps.ametro.model.entities

class MapSchemeStation(
    private val locale: MapLocale,
    val uid: Int,
    val name: String,
    private val nameTextId: Int,
    val labelPosition: MapRect?,
    val position: MapPoint?,
    val isWorking: Boolean
) {
    val displayName: String? get() = locale.getText(nameTextId)
    fun getAllDisplayNames(): List<String>? = locale.getAllTexts(nameTextId)
}
