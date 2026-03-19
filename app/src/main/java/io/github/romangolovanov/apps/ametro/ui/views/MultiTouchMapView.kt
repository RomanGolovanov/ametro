package io.github.romangolovanov.apps.ametro.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Pair
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView
import androidx.annotation.NonNull
import io.github.romangolovanov.apps.ametro.model.MapContainer
import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme
import io.github.romangolovanov.apps.ametro.render.CanvasRenderer
import io.github.romangolovanov.apps.ametro.render.RenderProgram
import io.github.romangolovanov.apps.ametro.render.utils.Algorithms
import io.github.romangolovanov.apps.ametro.ui.controllers.MultiTouchController
import java.util.HashSet

class MultiTouchMapView : ScrollView, MultiTouchController.IMultiTouchListener {

    companion object {
        private const val SCROLLBAR_TIMEOUT = 1000L
    }

    private lateinit var multiTouchController: MultiTouchController
    private lateinit var viewportChangedListener: IViewportChangedListener
    private lateinit var renderer: CanvasRenderer
    private lateinit var renderFailedTextPaint: Paint
    private lateinit var renderFailedErrorText: String
    private lateinit var mapScheme: MapScheme
    private lateinit var rendererProgram: RenderProgram

    private val dispatcher = Handler(Looper.getMainLooper())
    private val hideScrollbarsRunnable = Runnable { fadeScrollBars() }
    private var lastClickPosition: PointF? = null
    private val performClickRunnable = Runnable {
        lastClickPosition = null
        performClick()
    }

    private var doubleClickSlop = 0f
    private var verticalScrollOffset = 0
    private var horizontalScrollOffset = 0
    private var verticalScrollRange = 0
    private var horizontalScrollRange = 0
    private var changeCenterPoint: PointF? = null
    private var changeScale: Float? = null

    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        container: MapContainer?,
        schemeName: String?,
        viewportChangedListener: IViewportChangedListener?
    ) : super(context) {
        this.viewportChangedListener = viewportChangedListener!!
        isScrollbarFadingEnabled = false

        renderFailedErrorText = "Render failed!"

        isFocusable = true
        isFocusableInTouchMode = true

        isHorizontalScrollBarEnabled = true
        isVerticalScrollBarEnabled = true

        awakeScrollBars()

        mapScheme = container!!.getScheme(schemeName!!)!!

        multiTouchController = MultiTouchController(context, this)

        doubleClickSlop = ViewConfiguration.get(context).scaledDoubleTapSlop.toFloat()

        rendererProgram = RenderProgram(container, schemeName)
        renderer = CanvasRenderer(this, mapScheme, rendererProgram)

        renderFailedTextPaint = Paint().apply {
            color = Color.RED
            textAlign = Paint.Align.CENTER
        }

        initializeViewport()
    }

    override fun computeVerticalScrollOffset(): Int = verticalScrollOffset

    override fun computeVerticalScrollRange(): Int = verticalScrollRange

    override fun computeHorizontalScrollOffset(): Int = horizontalScrollOffset

    override fun computeHorizontalScrollRange(): Int = horizontalScrollRange

    override fun onAttachedToWindow() {
        renderer.onAttachedToWindow()
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        renderer.onDetachedFromWindow()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        if (!renderer.draw(canvas)) {
            canvas.drawText(renderFailedErrorText, (width / 2).toFloat(), (height / 2).toFloat(), renderFailedTextPaint)
        }
        canvas.restore()
        super.onDraw(canvas)
    }

    override fun getPositionAndScaleMatrix(): Matrix = multiTouchController.getPositionAndScale()

    override fun setPositionAndScaleMatrix(matrix: Matrix) {
        updateScrollBars(matrix)
        renderer.setMatrix(matrix)
        viewportChangedListener.onViewportChanged(matrix)
    }

    override fun onTouchModeChanged(mode: Int) {
        renderer.setUpdatesEnabled(mode != MultiTouchController.MODE_ZOOM && mode != MultiTouchController.MODE_ANIMATION)
    }

    override fun onPerformClick(position: PointF) {
        if (lastClickPosition == null) {
            lastClickPosition = multiTouchController.getScreenTouchPoint()
            dispatcher.removeCallbacks(performClickRunnable)
            dispatcher.postDelayed(performClickRunnable, ViewConfiguration.getDoubleTapTimeout().toLong())
            return
        }

        val distance = Algorithms.calculateDistance(lastClickPosition!!, multiTouchController.getScreenTouchPoint())

        dispatcher.removeCallbacks(performClickRunnable)
        lastClickPosition = null

        if (distance <= doubleClickSlop) {
            multiTouchController.doZoomAnimation(MultiTouchController.ZOOM_IN, multiTouchController.getTouchPoint())
        } else {
            performClick()
        }
    }

    override fun onPerformLongClick(position: PointF) {
        performLongClick()
    }

    val touchPoint: MapPoint
        get() {
            val p = multiTouchController.getTouchPoint()
            return MapPoint(p.x, p.y)
        }

    fun setCenterPositionAndScale(position: PointF?, zoom: Float?, animated: Boolean) {
        if (!animated) {
            changeCenterPoint = position
            changeScale = zoom
            invalidate()
        } else {
            multiTouchController.doScrollAndZoomAnimation(position, zoom)
        }
    }

    val centerPositionAndScale: Pair<PointF, Float>
        get() {
            val position = PointF()
            val scale = multiTouchController.getPositionAndScale(position)
            val width = width / scale
            val height = height / scale
            position.offset(width / 2, height / 2)
            return Pair(position, scale)
        }

    val scale: Float get() = multiTouchController.getScale()

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            updateViewRect()
        }
    }

    override fun onTouchEvent(@NonNull event: MotionEvent): Boolean {
        return multiTouchController.onMultiTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldWidth: Int, oldHeight: Int) {
        updateViewRect()
        super.onSizeChanged(w, h, oldWidth, oldHeight)
    }

    fun highlightsElements(ids: HashSet<Int>?) {
        rendererProgram.highlightsElements(ids)
        renderer.recycleCache()
        invalidate()
    }

    private fun initializeViewport() {
        val area = RectF(0f, 0f, mapScheme.width.toFloat(), mapScheme.height.toFloat())
        val scaleX = width / area.width()
        val scaleY = height / area.height()
        val targetScale = minOf(scaleX, scaleY)
        val currentScale = scale
        val targetFinal = if (targetScale > currentScale) currentScale else targetScale
        setCenterPositionAndScale(PointF(area.centerX(), area.centerY()), targetFinal, false)
    }

    private fun updateViewRect() {
        multiTouchController.setViewRect(mapScheme.width.toFloat(), mapScheme.height.toFloat(), RectF(0f, 0f, width.toFloat(), height.toFloat()))
        val cp = changeCenterPoint
        val cs = changeScale
        if (cp != null && cs != null) {
            val w = width / cs
            val h = height / cs
            cp.offset(-w / 2, -h / 2)
            multiTouchController.setPositionAndScale(cp, cs)
            changeCenterPoint = null
            changeScale = null
        }
    }

    private fun updateScrollBars(matrix: Matrix) {
        val values = FloatArray(9)
        matrix.getValues(values)
        val scale = values[Matrix.MSCALE_X]
        horizontalScrollRange = (mapScheme.width * scale).toInt()
        verticalScrollRange = (mapScheme.height * scale).toInt()
        horizontalScrollOffset = (-values[Matrix.MTRANS_X]).toInt()
        verticalScrollOffset = (-values[Matrix.MTRANS_Y]).toInt()
        awakeScrollBars()
    }

    private fun awakeScrollBars() {
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = true
        dispatcher.removeCallbacks(hideScrollbarsRunnable)
        dispatcher.postDelayed(hideScrollbarsRunnable, SCROLLBAR_TIMEOUT)
        invalidate()
    }

    private fun fadeScrollBars() {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        invalidate()
    }

    interface IViewportChangedListener {
        fun onViewportChanged(matrix: Matrix)
    }
}
