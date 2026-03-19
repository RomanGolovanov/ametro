package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Rect
import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation
import io.github.romangolovanov.apps.ametro.render.RenderConstants
import io.github.romangolovanov.apps.ametro.render.utils.getGrayedColor

class StationElement(scheme: MapScheme, line: MapSchemeLine, station: MapSchemeStation) : DrawingElement() {

    val position: MapPoint
    val radiusInternal: Float
    val radiusExternal: Float
    val paints: Array<Paint?> = arrayOfNulls(RenderConstants.LAYER_COUNT)
    val backgroundPaint: Paint

    init {
        uid = station.uid

        val radius = scheme.stationsDiameter.toInt() / 2
        position = station.position!!
        radiusInternal = radius * 0.80f
        radiusExternal = radius * 1.10f

        backgroundPaint = Paint()
        backgroundPaint.color = Color.WHITE
        backgroundPaint.style = Style.FILL_AND_STROKE
        backgroundPaint.isAntiAlias = true
        backgroundPaint.strokeWidth = 2f

        val lineColor = line.lineColor
        val isWorking = station.isWorking

        paints[RenderConstants.LAYER_VISIBLE] = createPaint(lineColor, radius.toFloat(), isWorking)
        paints[RenderConstants.LAYER_GRAYED] = createPaint(getGrayedColor(lineColor), radius.toFloat(), isWorking)

        setBoxAndPriority(
            Rect(
                (position.x - radius).toInt(),
                (position.y - radius).toInt(),
                (position.x + radius).toInt(),
                (position.y + radius).toInt()
            ),
            RenderConstants.TYPE_STATION
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(position.x, position.y, radiusExternal, backgroundPaint)
        canvas.drawCircle(position.x, position.y, radiusInternal, paints[layer]!!)
    }

    private fun createPaint(color: Int, radius: Float, isWorking: Boolean): Paint {
        val paint = Paint()
        paint.color = color
        paint.isAntiAlias = true
        paint.strokeWidth = radius * 0.15f * 2
        paint.style = if (isWorking) Style.FILL_AND_STROKE else Style.STROKE
        return paint
    }
}
