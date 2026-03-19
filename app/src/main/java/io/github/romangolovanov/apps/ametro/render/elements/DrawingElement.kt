package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Canvas
import android.graphics.Rect

abstract class DrawingElement : Comparable<DrawingElement> {

    var uid: Int? = null

    private var boundingBox: Rect? = null

    private var priority: Int = 0

    @JvmField
    protected var layer: Int = 0

    fun getBoundingBox(): Rect? = boundingBox

    fun setLayer(layer: Int) {
        this.layer = layer
    }

    abstract fun draw(canvas: Canvas)

    protected fun setBoxAndPriority(boundingBox: Rect, priority: Int) {
        this.boundingBox = boundingBox
        this.priority = priority
    }

    override fun compareTo(other: DrawingElement): Int {
        val byLayer = this.layer - other.layer
        if (byLayer != 0) {
            return byLayer
        }
        val byPriority = this.priority - other.priority
        if (byPriority != 0) {
            return byPriority
        }
        return 0
    }
}
