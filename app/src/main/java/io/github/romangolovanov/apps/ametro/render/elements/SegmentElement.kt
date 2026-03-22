package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Canvas
import android.graphics.ComposePathEffect
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.Rect
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeSegment
import io.github.romangolovanov.apps.ametro.render.RenderConstants
import io.github.romangolovanov.apps.ametro.render.utils.getGrayedColor
import kotlin.math.max
import kotlin.math.min

class SegmentElement(line: MapSchemeLine, segment: MapSchemeSegment) : DrawingElement() {

    val paints: Array<Paint?> = arrayOfNulls(RenderConstants.LAYER_COUNT)
    val path: Path

    init {
        uid = segment.uid

        val lineColor = line.lineColor
        val isWorking = segment.isWorking
        val lineWidth = line.lineWidth.toFloat()

        paints[RenderConstants.LAYER_VISIBLE] = createPaint(lineColor, lineWidth, isWorking)
        paints[RenderConstants.LAYER_GRAYED] = createPaint(getGrayedColor(lineColor), lineWidth, isWorking)

        val points = segment.points

        val minX = (min(points[0].x, points[points.size - 1].x) - lineWidth).toInt()
        val maxX = (max(points[0].x, points[points.size - 1].x) + lineWidth).toInt()
        val minY = (min(points[0].y, points[points.size - 1].y) - lineWidth).toInt()
        val maxY = (max(points[0].y, points[points.size - 1].y) + lineWidth).toInt()
        val box = Rect(minX, minY, maxX, maxY)

        path = Path()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val p = points[i]
            path.lineTo(p.x, p.y)
            box.union(p.x.toInt(), p.y.toInt())
        }
        setBoxAndPriority(box, if (segment.isWorking) RenderConstants.TYPE_LINE else RenderConstants.TYPE_LINE_DASHED)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paints[layer]!!)
    }

    private fun createPaint(color: Int, lineWidth: Float, isWorking: Boolean): Paint {
        val paint = Paint()
        paint.style = Style.STROKE
        paint.isAntiAlias = true
        paint.color = color

        if (isWorking) {
            paint.strokeWidth = lineWidth
            paint.pathEffect = CornerPathEffect(lineWidth * 0.2f)
        } else {
            paint.strokeWidth = lineWidth * 0.75f
            paint.pathEffect = ComposePathEffect(
                DashPathEffect(floatArrayOf(lineWidth * 0.8f, lineWidth * 0.2f), 0f),
                CornerPathEffect(lineWidth * 0.2f)
            )
        }
        return paint
    }
}
