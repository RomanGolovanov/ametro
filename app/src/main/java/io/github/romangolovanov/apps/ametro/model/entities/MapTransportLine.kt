package io.github.romangolovanov.apps.ametro.model.entities

class MapTransportLine(
    val name: String,
    val map: String,
    val segments: Array<MapTransportSegment>,
    val delays: IntArray
)
