package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationCheckBoxItem
import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationItem

internal class NavigationCheckBoxItemHolder(view: View) : IHolder {

    private val checkBox: CheckBox = view.findViewById(R.id.checkbox)
    private val textView: TextView = view.findViewById(R.id.text)

    override fun update(item: NavigationItem) {
        val checkboxItem = item as NavigationCheckBoxItem
        checkBox.isChecked = checkboxItem.checked
        textView.text = checkboxItem.text
    }
}
