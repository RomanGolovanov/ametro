package io.github.romangolovanov.apps.ametro.model.entities

import io.github.romangolovanov.apps.ametro.render.utils.Algorithms

class MapPoint(@JvmField val x: Float, @JvmField val y: Float) {
    fun distance(point: MapPoint): Double {
        return Algorithms.calculateDistance(x, y, point.x, point.y).toDouble()
    }
}
