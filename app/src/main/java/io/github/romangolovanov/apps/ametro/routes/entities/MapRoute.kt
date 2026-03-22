package io.github.romangolovanov.apps.ametro.routes.entities

class MapRoute(val parts: Array<MapRoutePart>) {
    val delay: Int get() = parts.sumOf { it.delay }.toInt()
}
