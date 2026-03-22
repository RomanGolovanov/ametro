package io.github.romangolovanov.apps.ametro.ui.controllers

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.Scroller
import io.github.romangolovanov.apps.ametro.utils.AnimationInterpolator

class MultiTouchController(context: Context, iMultiTouchListener: IMultiTouchListener) {

    interface IMultiTouchListener {
        fun getPositionAndScaleMatrix(): Matrix
        fun setPositionAndScaleMatrix(matrix: Matrix)
        fun onTouchModeChanged(mode: Int)
        fun onPerformClick(position: PointF)
        fun onPerformLongClick(position: PointF)
    }

    companion object {
        const val MODE_NONE = 1
        const val MODE_INIT = 2
        const val MODE_DRAG_START = 3
        const val MODE_DRAG = 4
        const val MODE_ZOOM = 5
        const val MODE_SHORTPRESS_START = 6
        const val MODE_SHORTPRESS_MODE = 7
        const val MODE_LONGPRESS_START = 8
        const val MODE_ANIMATION = 100

        const val ZOOM_IN = 1
        const val ZOOM_OUT = 2

        private const val MIN_FLING_TIME = 250
        private const val ANIMATION_TIME = 250

        private const val MSG_SWITCH_TO_SHORTPRESS = 1
        private const val MSG_SWITCH_TO_LONGPRESS = 2
        private const val MSG_PROCESS_FLING = 3
        private const val MSG_PROCESS_ANIMATION = 4

        private const val ZOOM_LEVEL_DISTANCE = 1.5f
    }

    private val listener: IMultiTouchListener = iMultiTouchListener
    private var initialized = false
    private var touchSlopSquare: Int

    private var matrix = Matrix()
    private var invertedMatrix = Matrix()
    private var savedMatrix = Matrix()

    private val animationInterpolator = AnimationInterpolator()

    private var mode = MODE_NONE

    private val touchStartPoint = PointF()
    private var touchStartTime: Long = 0
    private val zoomCenter = PointF()
    private var zoomBase = 1f

    private val matrixValues = FloatArray(9)
    private var maxScale = 0f
    private var minScale = 0f

    private var contentHeight = 0f
    private var contentWidth = 0f
    private var displayRect: RectF? = null

    private val scroller: Scroller
    private var velocityTracker: VelocityTracker?

    val privateHandler = PrivateHandler()
    private val density: Float

    private val animationEndPoint = PointF()
    private val animationStartPoint = PointF()

    init {
        listener
        scroller = Scroller(context)
        val vc = ViewConfiguration.get(context)
        val slop = vc.scaledTouchSlop
        touchSlopSquare = slop * slop
        density = context.resources.displayMetrics.density
        velocityTracker = null
    }

    fun mapPoint(point: PointF) {
        val pts = floatArrayOf(point.x, point.y)
        matrix.mapPoints(pts)
        point.x = pts[0]
        point.y = pts[1]
    }

    fun unmapPoint(point: PointF) {
        matrix.invert(invertedMatrix)
        val pts = floatArrayOf(point.x, point.y)
        invertedMatrix.mapPoints(pts)
        point.x = pts[0]
        point.y = pts[1]
    }

    fun setViewRect(newContentWidth: Float, newContentHeight: Float, newDisplayRect: RectF) {
        contentWidth = newContentWidth
        contentHeight = newContentHeight
        if (displayRect != null) {
            matrix.postTranslate(
                (newDisplayRect.width() - displayRect!!.width()) / 2,
                (newDisplayRect.height() - displayRect!!.height()) / 2
            )
        }
        displayRect = newDisplayRect
        maxScale = 2.0f * density
        minScale = minOf(displayRect!!.width() / contentWidth, displayRect!!.height() / contentHeight)
        adjustScale()
        adjustPan()
        listener.setPositionAndScaleMatrix(matrix)
    }

    fun onMultiTouchEvent(rawEvent: MotionEvent): Boolean {
        if (mode == MODE_ANIMATION) return false
        val event = MotionEventWrapper.create(rawEvent)
        if (!initialized) {
            matrix.set(listener.getPositionAndScaleMatrix())
            initialized = true
        }
        val action = event.getAction()
        val handled: Boolean
        handled = when {
            action == MotionEvent.ACTION_DOWN -> doActionDown(event)
            action == MotionEventWrapper.ACTION_POINTER_DOWN -> doActionPointerDown(event)
            action == MotionEvent.ACTION_UP || action == MotionEventWrapper.ACTION_POINTER_UP -> doActionUp(event)
            action == MotionEvent.ACTION_CANCEL -> doActionCancel(event)
            action == MotionEvent.ACTION_MOVE -> doActionMove(event)
            else -> true
        }
        listener.setPositionAndScaleMatrix(matrix)
        return handled
    }

    private fun doActionDown(event: MotionEventWrapper): Boolean {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
            setControllerMode(MODE_DRAG_START)
        } else {
            setControllerMode(MODE_INIT)
        }
        if (mode == MODE_INIT) {
            privateHandler.sendEmptyMessageDelayed(MSG_SWITCH_TO_SHORTPRESS, ViewConfiguration.getTapTimeout().toLong())
        }
        velocityTracker = VelocityTracker.obtain()
        savedMatrix.set(matrix)
        touchStartPoint.set(event.getX(), event.getY())
        touchStartTime = event.getEventTime()
        return true
    }

    private fun doActionPointerDown(event: MotionEventWrapper): Boolean {
        zoomBase = distance(event)
        if (zoomBase > 10f) {
            if (!scroller.isFinished) scroller.abortAnimation()
            savedMatrix.set(matrix)
            val x = event.getX(0) + event.getX(1)
            val y = event.getY(0) + event.getY(1)
            zoomCenter.set(x / 2, y / 2)
            setControllerMode(MODE_ZOOM)
        }
        return true
    }

    private fun doActionMove(event: MotionEventWrapper): Boolean {
        if (mode == MODE_NONE || mode == MODE_LONGPRESS_START) return false
        if (mode == MODE_ZOOM) {
            val newDist = distance(event)
            if (newDist > 10f) {
                matrix.set(savedMatrix)
                var scale = newDist / zoomBase
                matrix.getValues(matrixValues)
                val currentScale = matrixValues[Matrix.MSCALE_X]
                scale = when {
                    scale * currentScale > maxScale -> maxScale / currentScale
                    scale * currentScale < minScale -> minScale / currentScale
                    else -> scale
                }
                matrix.postScale(scale, scale, zoomCenter.x, zoomCenter.y)
                adjustPan()
            }
            return true
        }
        velocityTracker!!.addMovement(event.event)
        if (mode != MODE_DRAG) {
            val deltaX = (touchStartPoint.x - event.getX()).toInt()
            val deltaY = (touchStartPoint.y - event.getY()).toInt()
            if ((deltaX * deltaX + deltaY * deltaY) < touchSlopSquare) return false
            if (mode == MODE_SHORTPRESS_MODE || mode == MODE_SHORTPRESS_START) {
                privateHandler.removeMessages(MSG_SWITCH_TO_LONGPRESS)
            } else if (mode == MODE_INIT) {
                privateHandler.removeMessages(MSG_SWITCH_TO_SHORTPRESS)
            }
            setControllerMode(MODE_DRAG)
        }
        matrix.set(savedMatrix)
        val dx = event.getX() - touchStartPoint.x
        val dy = event.getY() - touchStartPoint.y
        matrix.postTranslate(dx, dy)
        adjustPan()
        return true
    }

    private fun doActionUp(event: MotionEventWrapper): Boolean {
        when (mode) {
            MODE_INIT, MODE_SHORTPRESS_START, MODE_SHORTPRESS_MODE -> {
                privateHandler.removeMessages(MSG_SWITCH_TO_SHORTPRESS)
                privateHandler.removeMessages(MSG_SWITCH_TO_LONGPRESS)
                velocityTracker?.recycle()
                velocityTracker = null
                setControllerMode(MODE_NONE)
                performClick()
                return true
            }
            MODE_LONGPRESS_START -> { /* do nothing */ }
            MODE_DRAG, MODE_DRAG_START -> {
                if ((event.getEventTime() - touchStartTime) <= MIN_FLING_TIME) {
                    velocityTracker!!.addMovement(event.event)
                    velocityTracker!!.computeCurrentVelocity(1000)
                    matrix.getValues(matrixValues)
                    val currentY = matrixValues[Matrix.MTRANS_Y]
                    val currentX = matrixValues[Matrix.MTRANS_X]
                    val currentScale = matrixValues[Matrix.MSCALE_X]
                    val currentHeight = contentHeight * currentScale
                    val currentWidth = contentWidth * currentScale
                    val vx = (-velocityTracker!!.xVelocity / 2).toInt()
                    val vy = (-velocityTracker!!.yVelocity / 2).toInt()
                    val maxX = maxOf(currentWidth - displayRect!!.width(), 0f).toInt()
                    val maxY = maxOf(currentHeight - displayRect!!.height(), 0f).toInt()
                    scroller.fling((-currentX).toInt(), (-currentY).toInt(), vx, vy, 0, maxX, 0, maxY)
                    privateHandler.sendEmptyMessage(MSG_PROCESS_FLING)
                }
            }
        }
        velocityTracker?.recycle()
        velocityTracker = null
        setControllerMode(MODE_NONE)
        return true
    }

    private fun doActionCancel(event: MotionEventWrapper): Boolean {
        privateHandler.removeMessages(MSG_SWITCH_TO_SHORTPRESS)
        privateHandler.removeMessages(MSG_SWITCH_TO_LONGPRESS)
        setControllerMode(MODE_NONE)
        return true
    }

    internal fun setControllerMode(newMode: Int) {
        val fireUpdate = mode != newMode
        mode = newMode
        if (fireUpdate) {
            listener.onTouchModeChanged(newMode)
        }
    }

    fun getControllerMode(): Int = mode

    private fun adjustScale() {
        matrix.getValues(matrixValues)
        val currentScale = matrixValues[Matrix.MSCALE_X]
        if (currentScale < minScale) {
            matrix.setScale(minScale, minScale)
        }
    }

    private fun adjustPan() {
        matrix.getValues(matrixValues)
        val currentY = matrixValues[Matrix.MTRANS_Y]
        val currentX = matrixValues[Matrix.MTRANS_X]
        val currentScale = matrixValues[Matrix.MSCALE_X]
        val currentHeight = contentHeight * currentScale
        val currentWidth = contentWidth * currentScale

        val drawingRect = RectF(currentX, currentY, currentX + currentWidth, currentY + currentHeight)
        val diffUp = minOf(displayRect!!.bottom - drawingRect.bottom, displayRect!!.top - drawingRect.top)
        val diffDown = maxOf(displayRect!!.bottom - drawingRect.bottom, displayRect!!.top - drawingRect.top)
        val diffLeft = minOf(displayRect!!.left - drawingRect.left, displayRect!!.right - drawingRect.right)
        val diffRight = maxOf(displayRect!!.left - drawingRect.left, displayRect!!.right - drawingRect.right)
        var dx = 0f
        var dy = 0f
        if (diffUp > 0) dy += diffUp
        if (diffDown < 0) dy += diffDown
        if (diffLeft > 0) dx += diffLeft
        if (diffRight < 0) dx += diffRight
        if (currentWidth < displayRect!!.width()) dx = -currentX + (displayRect!!.width() - currentWidth) / 2
        if (currentHeight < displayRect!!.height()) dy = -currentY + (displayRect!!.height() - currentHeight) / 2
        matrix.postTranslate(dx, dy)
    }

    private fun distance(event: MotionEventWrapper): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun computeScroll(): Boolean {
        val more = scroller.computeScrollOffset()
        if (more) {
            val x = scroller.currX.toFloat()
            val y = scroller.currY.toFloat()
            matrix.getValues(matrixValues)
            val currentY = -matrixValues[Matrix.MTRANS_Y]
            val currentX = -matrixValues[Matrix.MTRANS_X]
            val dx = currentX - x
            val dy = currentY - y
            matrix.postTranslate(dx, dy)
            adjustPan()
            listener.setPositionAndScaleMatrix(matrix)
        }
        return more
    }

    private fun computeAnimation(): Boolean {
        if (mode == MODE_ANIMATION) {
            animationInterpolator.next()
            if (animationInterpolator.hasScale()) {
                val scale = animationInterpolator.getScale() / getScale()
                matrix.postScale(scale, scale)
            }
            if (animationInterpolator.hasScroll()) {
                val newCenter = animationInterpolator.getPoint()
                mapPoint(newCenter)
                val dx = newCenter.x - displayRect!!.width() / 2
                val dy = newCenter.y - displayRect!!.height() / 2
                matrix.postTranslate(-dx, -dy)
            }
            adjustScale()
            adjustPan()
            listener.setPositionAndScaleMatrix(matrix)
            return animationInterpolator.more()
        }
        return false
    }

    fun getScreenTouchPoint(): PointF = PointF(touchStartPoint.x, touchStartPoint.y)

    fun getTouchPoint(): PointF {
        val p = PointF()
        p.set(touchStartPoint)
        unmapPoint(p)
        return p
    }

    fun getScale(): Float {
        matrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    fun getTouchRadius(): Float = touchSlopSquare.toFloat()

    fun getPositionAndScale(position: PointF?): Float {
        matrix.getValues(matrixValues)
        val scale = matrixValues[Matrix.MSCALE_X]
        position?.set(-matrixValues[Matrix.MTRANS_X] / scale, -matrixValues[Matrix.MTRANS_Y] / scale)
        return scale
    }

    fun setPositionAndScale(position: PointF, scale: Float) {
        matrix.setScale(scale, scale)
        matrix.postTranslate(-position.x * scale, -position.y * scale)
        adjustScale()
        adjustPan()
        listener.setPositionAndScaleMatrix(matrix)
    }

    fun performLongClick() {
        listener.onPerformLongClick(getTouchPoint())
    }

    fun performClick() {
        listener.onPerformClick(getTouchPoint())
    }

    fun doZoomAnimation(scaleMode: Int, scaleCenter: PointF) {
        val scaleFactor = if (scaleMode == ZOOM_IN) ZOOM_LEVEL_DISTANCE else 1 / ZOOM_LEVEL_DISTANCE
        val currentScale = getScale()
        var targetScale = minOf(maxOf(minScale, scaleFactor * currentScale), maxScale)
        if (targetScale != currentScale) {
            val nextScale = minOf(maxOf(minScale, scaleFactor * targetScale), maxScale)
            if (nextScale == maxScale && (nextScale / targetScale) < scaleFactor * 0.8f) {
                targetScale = maxScale
            } else if (nextScale == minScale && (targetScale / nextScale) < scaleFactor * 0.8f) {
                targetScale = minScale
            }
            doScrollAndZoomAnimation(scaleCenter, targetScale)
        }
    }

    fun doScrollAndZoomAnimation(center: PointF?, scale: Float?) {
        if (mode == MODE_NONE || mode == MODE_LONGPRESS_START) {
            animationStartPoint.set(displayRect!!.width() / 2, displayRect!!.height() / 2)
            unmapPoint(animationStartPoint)
            if (center != null) {
                animationEndPoint.set(center)
            } else {
                animationEndPoint.set(animationStartPoint)
            }
            val currentScale = getScale()
            animationInterpolator.begin(animationStartPoint, animationEndPoint, currentScale, scale ?: currentScale, ANIMATION_TIME.toLong())
            privateHandler.sendEmptyMessage(MSG_PROCESS_ANIMATION)
            setControllerMode(MODE_ANIMATION)
        }
    }

    fun getPositionAndScale(): Matrix = Matrix(matrix)

    inner class PrivateHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_PROCESS_ANIMATION -> {
                    if (mode == MODE_ANIMATION) {
                        val more = computeAnimation()
                        if (more) {
                            privateHandler.sendEmptyMessage(MSG_PROCESS_ANIMATION)
                        } else {
                            setControllerMode(MODE_NONE)
                            listener.setPositionAndScaleMatrix(matrix)
                        }
                    }
                }
                MSG_PROCESS_FLING -> {
                    val more = computeScroll()
                    if (more) {
                        privateHandler.sendEmptyMessage(MSG_PROCESS_FLING)
                    }
                }
                MSG_SWITCH_TO_SHORTPRESS -> {
                    if (mode == MODE_INIT) {
                        setControllerMode(MODE_SHORTPRESS_START)
                        privateHandler.sendEmptyMessageDelayed(MSG_SWITCH_TO_LONGPRESS, ViewConfiguration.getLongPressTimeout().toLong())
                    }
                }
                MSG_SWITCH_TO_LONGPRESS -> {
                    setControllerMode(MODE_LONGPRESS_START)
                    performLongClick()
                }
                else -> super.handleMessage(msg)
            }
        }
    }
}
