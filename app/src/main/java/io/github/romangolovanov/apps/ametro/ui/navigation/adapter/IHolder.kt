package io.github.romangolovanov.apps.ametro.ui.navigation.adapter

import io.github.romangolovanov.apps.ametro.ui.navigation.entities.NavigationItem

internal interface IHolder {
    fun update(item: NavigationItem)
}
