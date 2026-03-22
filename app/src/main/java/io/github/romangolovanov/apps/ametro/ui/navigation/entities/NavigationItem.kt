package io.github.romangolovanov.apps.ametro.ui.navigation.entities

abstract class NavigationItem(
    val action: Int = INVALID_ACTION,
    var enabled: Boolean = false,
    var selected: Boolean = false,
    val source: Any? = null
) {
    companion object {
        const val INVALID_ACTION = -1
    }
}
