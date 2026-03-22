package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.render.RenderConstants

class PictureBackgroundElement(scheme: MapScheme, private val picture: Picture) : DrawingElement() {

    init {
        setBoxAndPriority(Rect(0, 0, scheme.width, scheme.height), RenderConstants.TYPE_BACKGROUND)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPicture(picture)
    }
}
