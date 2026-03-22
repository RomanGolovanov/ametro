package io.github.romangolovanov.apps.ametro.model.entities

class MapRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    fun contains(point: MapPoint): Boolean {
        return point.x >= x && point.x <= (x + width) && point.y >= y && point.y <= (y + height)
    }
}
