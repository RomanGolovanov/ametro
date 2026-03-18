package io.github.romangolovanov.apps.ametro.catalog.entities

class MapCatalog(val maps: Array<MapInfo>) {

    fun findMap(fileName: String): MapInfo? = maps.find { it.fileName == fileName }
}
