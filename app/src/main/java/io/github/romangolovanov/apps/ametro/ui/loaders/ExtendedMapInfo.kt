package io.github.romangolovanov.apps.ametro.ui.loaders

import io.github.romangolovanov.apps.ametro.catalog.entities.MapInfo

class ExtendedMapInfo(map: MapInfo, val status: ExtendedMapStatus) : MapInfo(map) {
    var selected: Boolean = false
}
