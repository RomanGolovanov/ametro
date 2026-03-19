package io.github.romangolovanov.apps.ametro.ui.navigation.entities

import android.graphics.drawable.Drawable

class NavigationTextItem : NavigationItem {

    private val icon: Drawable?
    val text: CharSequence

    constructor(action: Int, icon: Drawable?, text: CharSequence) : this(action, icon, text, true, null)

    constructor(action: Int, icon: Drawable?, text: CharSequence, enabled: Boolean, source: Any?) : super(action, enabled, source) {
        this.icon = icon
        this.text = text
    }

    fun getDrawable(): Drawable? = icon
}
