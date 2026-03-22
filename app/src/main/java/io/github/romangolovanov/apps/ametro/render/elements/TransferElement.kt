package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeTransfer
import io.github.romangolovanov.apps.ametro.render.RenderConstants
import kotlin.math.max
import kotlin.math.min

class TransferElement(scheme: MapScheme, transfer: MapSchemeTransfer) : DrawingElement() {

    private val from: MapPoint = transfer.fromStationPosition!!
    private val to: MapPoint = transfer.toStationPosition!!
    private val radius: Float
    private val paint: Paint

    init {
        uid = transfer.uid

        paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.strokeWidth = scheme.linesWidth.toFloat() + 1.2f
        paint.isAntiAlias = true

        radius = scheme.stationsDiameter.toFloat() / 2 + 2.2f

        setBoxAndPriority(
            Rect(
                (min(from.x, to.x) - radius).toInt(),
                (min(from.y, to.y) - radius).toInt(),
                (max(from.x, to.x) + radius).toInt(),
                (max(from.y, to.y) + radius).toInt()
            ),
            RenderConstants.TYPE_TRANSFER
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(from.x, from.y, radius, paint)
        canvas.drawCircle(to.x, to.y, radius, paint)
        canvas.drawLine(from.x, from.y, to.x, to.y, paint)
    }
}
