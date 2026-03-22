package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.romangolovanov.apps.ametro.R

internal class NavigationCheckBoxItemHolderFactory : IHolderFactory {

    override fun createHolder(view: View): IHolder {
        val holder = NavigationCheckBoxItemHolder(view)
        view.tag = holder
        return holder
    }

    override fun createView(inflater: LayoutInflater, parent: ViewGroup): View {
        return inflater.inflate(R.layout.drawer_checkbox_item, parent, false)
    }
}
