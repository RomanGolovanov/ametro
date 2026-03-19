package io.github.romangolovanov.apps.ametro.ui.navigation.entities

import android.graphics.drawable.Drawable

class NavigationHeader : NavigationItem {

    val icon: Drawable?
    val city: String?
    val country: String?
    val comment: String?
    private val transportTypes: Array<Drawable?>

    constructor(icon: Drawable?, city: String?, country: String?, comment: String?, transportTypes: Array<Drawable?>) {
        this.icon = icon
        this.city = city
        this.country = country
        this.comment = comment
        this.transportTypes = transportTypes
    }

    constructor(emptyText: String) : this(null, emptyText, null, null, emptyArray<Drawable?>())

    fun getTransportTypeIcons(): Array<Drawable?> = transportTypes
}
