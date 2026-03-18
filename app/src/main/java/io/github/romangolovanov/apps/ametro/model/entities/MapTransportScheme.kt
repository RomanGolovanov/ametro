package io.github.romangolovanov.apps.ametro.model.entities

class MapTransportScheme(
    val name: String,
    val type: String,
    val lines: Array<MapTransportLine>,
    val transfers: Array<MapTransportTransfer>
)
