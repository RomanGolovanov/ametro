package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationItem
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationTextItem

internal class NavigationTextItemHolder(view: View) : IHolder {

    private val imageView: ImageView = view.findViewById(R.id.icon)
    private val textView: TextView = view.findViewById(R.id.text)
    private val container: View = view

    override fun update(item: NavigationItem) {
        val textItem = item as NavigationTextItem
        imageView.setImageDrawable(textItem.getDrawable())
        textView.text = textItem.text
        container.setBackgroundResource(if (textItem.selected) R.color.activated_color else android.R.color.transparent)
    }
}
