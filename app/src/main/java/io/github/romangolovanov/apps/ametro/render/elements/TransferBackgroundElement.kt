package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeTransfer
import io.github.romangolovanov.apps.ametro.render.RenderConstants
import io.github.romangolovanov.apps.ametro.render.utils.RenderUtils
import kotlin.math.max
import kotlin.math.min

class TransferBackgroundElement(scheme: MapScheme, transfer: MapSchemeTransfer) : DrawingElement() {

    private val from: MapPoint = transfer.fromStationPosition!!
    private val to: MapPoint = transfer.toStationPosition!!
    private val radius: Float
    private val paints: Array<Paint?> = arrayOfNulls(RenderConstants.LAYER_COUNT)

    init {
        uid = transfer.uid

        radius = scheme.stationsDiameter.toFloat() / 2 + 3.5f

        val linesWidth = scheme.linesWidth.toFloat()

        paints[RenderConstants.LAYER_VISIBLE] = createPaint(Color.BLACK, linesWidth)
        paints[RenderConstants.LAYER_GRAYED] = createPaint(RenderUtils.getGrayedColor(Color.BLACK), linesWidth)

        setBoxAndPriority(
            Rect(
                (min(from.x, to.x) - radius).toInt(),
                (min(from.y, to.y) - radius).toInt(),
                (max(from.x, to.x) + radius).toInt(),
                (max(from.y, to.y) + radius).toInt()
            ),
            RenderConstants.TYPE_TRANSFER_BACKGROUND
        )
    }

    override fun draw(canvas: Canvas) {
        val p = paints[layer]!!
        canvas.drawCircle(from.x, from.y, radius, p)
        canvas.drawCircle(to.x, to.y, radius, p)
        canvas.drawLine(from.x, from.y, to.x, to.y, p)
    }

    private fun createPaint(color: Int, linesWidth: Float): Paint {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.strokeWidth = linesWidth + 4.5f
        paint.isAntiAlias = true
        return paint
    }
}
