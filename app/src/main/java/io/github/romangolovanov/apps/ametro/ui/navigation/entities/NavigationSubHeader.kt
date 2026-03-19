package io.github.romangolovanov.apps.ametro.ui.navigation.entities

class NavigationSubHeader(
    action: Int = INVALID_ACTION,
    val text: CharSequence,
    private val items: Array<NavigationItem>
) : NavigationItem(action), INavigationItemGroup {

    constructor(text: CharSequence, items: Array<NavigationItem>) : this(INVALID_ACTION, text, items)

    override fun getItems(): Array<NavigationItem> = items
}
