package io.github.romangolovanov.apps.ametro.ui.navigation.entities

import android.graphics.drawable.Drawable

class NavigationTextItem(
    action: Int,
    private val icon: Drawable?,
    val text: CharSequence,
    enabled: Boolean = true,
    source: Any? = null
) : NavigationItem(action, enabled, source = source) {

    fun getDrawable(): Drawable? = icon
}
