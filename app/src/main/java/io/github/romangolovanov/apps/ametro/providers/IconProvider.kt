package io.github.romangolovanov.apps.ametro.providers

import android.content.Context
import android.graphics.drawable.Drawable
import java.io.IOException
import java.util.HashMap
import java.util.HashSet

class IconProvider(context: Context, private val defaultIcon: Drawable, private val assetPath: String) {

    private val assetManager = context.assets
    private val icons = HashMap<String, Drawable>()
    private val assets = HashSet<String>()

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
        var d = icons[iso]
        if (d == null) {
            val assetName = iso.lowercase() + ".png"
            d = if (assets.contains(assetName)) {
                try {
                    Drawable.createFromStream(assetManager.open("$assetPath/$assetName"), null)
                } catch (e: IOException) {
                    defaultIcon
                }
            } else {
                defaultIcon
            }
            icons[iso] = d!!
        }
        return d!!
    }
}
