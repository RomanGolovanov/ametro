package io.github.romangolovanov.apps.ametro.model.entities

class MapSchemeTransfer(
    val uid: Int,
    val from: Int,
    val to: Int,
    val fromStationPosition: MapPoint?,
    val toStationPosition: MapPoint?
)
