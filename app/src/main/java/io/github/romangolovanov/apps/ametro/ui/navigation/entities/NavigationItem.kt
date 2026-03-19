package io.github.romangolovanov.apps.ametro.ui.navigation.entities

abstract class NavigationItem {

    companion object {
        const val INVALID_ACTION = -1
    }

    val action: Int
    val source: Any?
    var enabled: Boolean
    var selected: Boolean

    constructor() : this(INVALID_ACTION, false)

    constructor(action: Int) : this(action, false, false, null)

    constructor(action: Int, enabled: Boolean) : this(action, enabled, false, null)

    constructor(action: Int, enabled: Boolean, source: Any?) : this(action, enabled, false, source)

    constructor(action: Int, enabled: Boolean, selected: Boolean, source: Any?) {
        this.action = action
        this.enabled = enabled
        this.source = source
        this.selected = selected
    }
}
