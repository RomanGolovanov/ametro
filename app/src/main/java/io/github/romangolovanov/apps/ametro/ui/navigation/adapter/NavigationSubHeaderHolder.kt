package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import android.view.View
import android.widget.TextView
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationItem
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationSubHeader

internal class NavigationSubHeaderHolder(view: View) : IHolder {

    private val textView: TextView = view.findViewById(R.id.text)

    override fun update(item: NavigationItem) {
        val textItem = item as NavigationSubHeader
        textView.text = textItem.text
    }
}
