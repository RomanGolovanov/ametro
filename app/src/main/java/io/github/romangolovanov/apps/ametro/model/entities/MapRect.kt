package io.github.romangolovanov.apps.ametro.model.entities

class MapRect(
    @JvmField val x: Int,
    @JvmField val y: Int,
    @JvmField val width: Int,
    @JvmField val height: Int
) {
    fun contains(point: MapPoint): Boolean {
        return point.x >= x && point.x <= (x + width) && point.y >= y && point.y <= (y + height)
    }
}
