package io.github.romangolovanov.apps.ametro.render.utils

import android.graphics.PointF
import kotlin.math.sqrt

fun calculateDistance(p1: PointF, p2: PointF): Float {
    return calculateDistance(p1.x, p1.y, p2.x, p2.y)
}

fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val x = x1 - x2
    val y = y1 - y2
    return sqrt(x * x + y * y)
}
