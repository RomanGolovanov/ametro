package io.github.romangolovanov.apps.ametro.model.entities

class MapSchemeSegment(
    val uid: Int,
    val from: Int,
    val to: Int,
    val points: Array<MapPoint>,
    val isWorking: Boolean
)
