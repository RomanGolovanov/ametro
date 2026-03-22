package io.github.romangolovanov.apps.ametro.ui.navigation

import io.github.romangolovanov.apps.ametro.model.entities.MapDelay

interface INavigationControllerListener {
    fun onOpenMaps(): Boolean
    fun onOpenSettings(): Boolean
    fun onChangeScheme(schemeName: String): Boolean
    fun onToggleTransport(source: String, checked: Boolean): Boolean
    fun onDelayChanged(delay: MapDelay): Boolean
    fun onOpenAbout(): Boolean
}
