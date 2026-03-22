package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.render.RenderConstants

class BitmapBackgroundElement(scheme: MapScheme, private val bitmap: Bitmap) : DrawingElement() {

    private val paint = Paint()

    init {
        setBoxAndPriority(Rect(0, 0, scheme.width, scheme.height), RenderConstants.TYPE_BACKGROUND)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }
}
