package io.github.romangolovanov.apps.ametro.render.elements

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.Typeface
import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation
import io.github.romangolovanov.apps.ametro.render.RenderConstants
import io.github.romangolovanov.apps.ametro.render.utils.getGrayedColor
import io.github.romangolovanov.apps.ametro.utils.toRect

class StationNameElement(scheme: MapScheme, line: MapSchemeLine, station: MapSchemeStation) : DrawingElement() {

    private val vertical: Boolean

    private lateinit var textPaints: Array<Paint>
    private lateinit var borderPaints: Array<Paint>

    private val firstLine: String
    private val firstLinePosition: MapPoint

    private var secondLine: String? = null
    private var secondLinePosition: MapPoint? = null

    init {
        uid = station.uid

        val name = if (scheme.isUpperCase) {
            station.displayName?.uppercase() ?: ""
        } else {
            station.displayName ?: ""
        }

        val textLength = name.length
        val textRect = toRect(station.labelPosition)!!
        val point = station.position!!

        vertical = textRect.width() < textRect.height()

        val align = if (vertical) {
            if (point.y > textRect.centerY()) Align.LEFT else Align.RIGHT
        } else {
            if (point.x > textRect.centerX()) Align.RIGHT else Align.LEFT
        }

        val textColor = line.labelColor
        val visiblePaint = createTextPaint(textColor).also { it.textAlign = align }
        val grayedPaint = createTextPaint(getGrayedColor(textColor)).also { it.textAlign = align }
        textPaints = arrayOf(grayedPaint, visiblePaint)

        var borderColor = line.labelBackgroundColor
        var borderGrayedColor = getGrayedColor(borderColor)
        if (borderColor == -1) {
            borderColor = Color.WHITE
            borderGrayedColor = Color.WHITE
        }

        val visibleBorderPaint = createBorderPaint(visiblePaint, borderColor).also { it.textAlign = align }
        val grayedBorderPaint = createBorderPaint(visiblePaint, borderGrayedColor).also { it.textAlign = align }
        borderPaints = arrayOf(grayedBorderPaint, visibleBorderPaint)

        val result = splitTextToLines(scheme, name, textLength, textRect, align, visiblePaint)
        firstLine = result.first
        firstLinePosition = result.second
        secondLine = result.third
        secondLinePosition = result.fourth

        setBoxAndPriority(textRect, RenderConstants.TYPE_STATION_NAME)
    }

    private fun splitTextToLines(
        scheme: MapScheme,
        name: String,
        textLength: Int,
        textRect: Rect,
        align: Align,
        paint: Paint
    ): Quadruple<String, MapPoint, String?, MapPoint?> {
        val rect: Rect = if (vertical) {
            if (align == Align.LEFT) {
                Rect(textRect.left, textRect.bottom, textRect.left + textRect.height(), textRect.bottom + textRect.width())
            } else {
                Rect(textRect.left - textRect.height(), textRect.top, textRect.left, textRect.top + textRect.width())
            }
        } else {
            Rect(textRect)
        }

        val bounds = Rect()
        paint.getTextBounds(name, 0, textLength, bounds)
        var isNeedSecondLine = bounds.width() > rect.width() && scheme.isWordWrap
        var spacePosition = -1
        if (isNeedSecondLine) {
            spacePosition = name.indexOf(' ')
            isNeedSecondLine = spacePosition != -1
        }
        return if (isNeedSecondLine) {
            val firstText = name.substring(0, spacePosition)
            val secondText = name.substring(spacePosition + 1)
            val secondRect = Rect(rect.left, rect.top + bounds.height() + 2, rect.right, rect.bottom + bounds.height() + 2)

            val firstPos = initializeLine(firstText, vertical, rect, paint, align)
            var secondPos = initializeLine(secondText, vertical, secondRect, paint, align)
            secondPos = MapPoint(secondPos.x - firstPos.x, secondPos.y - firstPos.y)

            Quadruple(firstText, firstPos, secondText, secondPos)
        } else {
            val firstPos = initializeLine(name, vertical, rect, paint, align)
            Quadruple(name, firstPos, null, null)
        }
    }

    private fun createTextPaint(color: Int): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.typeface = Typeface.DEFAULT
        paint.isFakeBoldText = true
        paint.textSize = 10f
        paint.textAlign = Align.LEFT
        paint.color = color
        paint.style = Style.FILL
        return paint
    }

    private fun createBorderPaint(paint: Paint, color: Int): Paint {
        val borderPaint = Paint(paint)
        borderPaint.color = color
        borderPaint.style = Style.STROKE
        borderPaint.strokeWidth = 2f
        return borderPaint
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(firstLinePosition.x, firstLinePosition.y)
        if (vertical) {
            canvas.rotate(-90f)
        }
        canvas.drawText(firstLine, 0f, 0f, borderPaints[layer])
        canvas.drawText(firstLine, 0f, 0f, textPaints[layer])
        val sl = secondLine
        val slp = secondLinePosition
        if (sl != null && slp != null) {
            canvas.translate(slp.x, slp.y)
            canvas.drawText(sl, 0f, 0f, borderPaints[layer])
            canvas.drawText(sl, 0f, 0f, textPaints[layer])
        }
        canvas.restore()
    }

    companion object {
        private fun initializeLine(text: String, vertical: Boolean, rect: Rect, paint: Paint, align: Align): MapPoint {
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            return if (align == Align.RIGHT) {
                MapPoint(
                    (rect.right + if (vertical) bounds.height() else 0).toFloat(),
                    (rect.top + if (vertical) 0 else bounds.height()).toFloat()
                )
            } else {
                MapPoint(
                    (rect.left + if (vertical) bounds.height() else 0).toFloat(),
                    (rect.top + if (vertical) 0 else bounds.height()).toFloat()
                )
            }
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
