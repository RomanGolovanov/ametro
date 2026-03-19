package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

internal interface IHolderFactory {
    fun createView(inflater: LayoutInflater, parent: ViewGroup): View
    fun createHolder(view: View): IHolder
}
