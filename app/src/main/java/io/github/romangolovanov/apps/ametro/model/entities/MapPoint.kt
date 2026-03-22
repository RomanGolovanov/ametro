package io.github.romangolovanov.apps.ametro.model.entities

import io.github.romangolovanov.apps.ametro.render.utils.calculateDistance

class MapPoint(val x: Float, val y: Float) {
    fun distance(point: MapPoint): Double {
        return calculateDistance(x, y, point.x, point.y).toDouble()
    }
}
