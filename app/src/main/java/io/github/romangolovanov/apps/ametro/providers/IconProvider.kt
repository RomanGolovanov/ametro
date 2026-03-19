package io.github.romangolovanov.apps.ametro.providers

import android.content.Context
import android.graphics.drawable.Drawable
import java.io.IOException

class IconProvider(context: Context, private val defaultIcon: Drawable, private val assetPath: String) {

    private val assetManager = context.assets
    private val icons = mutableMapOf<String, Drawable>()
    private val assets = mutableSetOf<String>()

    init {
        try {
            for (assetName in assetManager.list(assetPath)!!) {
                assets.add(assetName.lowercase())
            }
        } catch (ex: IOException) {
            // no icons available
        }
    }

    fun getIcon(iso: String): Drawable {
        return icons.getOrPut(iso) {
            val assetName = iso.lowercase() + ".png"
            if (assets.contains(assetName)) {
                try {
                    Drawable.createFromStream(assetManager.open("$assetPath/$assetName"), null) ?: defaultIcon
                } catch (e: IOException) {
                    defaultIcon
                }
            } else {
                defaultIcon
            }
        }
    }
}
