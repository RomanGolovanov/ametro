package io.github.romangolovanov.apps.ametro.render.utils

import android.graphics.Color

object RenderUtils {

    @JvmStatic
    fun getGrayedColor(color: Int): Int {
        if (color == Color.BLACK) {
            return 0xFFd0d0d0.toInt()
        }
        var r = Color.red(color).toFloat() / 255
        var g = Color.green(color).toFloat() / 255
        var b = Color.blue(color).toFloat() / 255
        val t = 0.8f
        r = r * (1 - t) + 1.0f * t
        g = g * (1 - t) + 1.0f * t
        b = b * (1 - t) + 1.0f * t
        return Color.argb(0xFF, minOf((r * 255).toInt(), 255), minOf((g * 255).toInt(), 255), minOf((b * 255).toInt(), 255))
    }
}
