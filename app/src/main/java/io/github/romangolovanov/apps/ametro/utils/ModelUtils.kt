package io.github.romangolovanov.apps.ametro.utils

import android.graphics.Rect
import io.github.romangolovanov.apps.ametro.model.entities.MapRect

object ModelUtils {

    @JvmStatic
    fun toRect(r: MapRect?): Rect? {
        if (r == null) return null
        return Rect(r.x, r.y, r.x + r.width, r.y + r.height)
    }
}
