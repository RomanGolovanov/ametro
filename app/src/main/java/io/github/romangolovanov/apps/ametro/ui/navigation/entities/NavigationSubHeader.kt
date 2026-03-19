package io.github.romangolovanov.apps.ametro.ui.navigation.entities

class NavigationSubHeader : NavigationItem, INavigationItemGroup {

    val text: CharSequence
    private val items: Array<NavigationItem>

    constructor(text: CharSequence, items: Array<NavigationItem>) : this(INVALID_ACTION, text, items)

    constructor(action: Int, text: CharSequence, items: Array<NavigationItem>) : super(action) {
        this.text = text
        this.items = items
    }

    override fun getItems(): Array<NavigationItem> = items
}
