package io.github.romangolovanov.apps.ametro.render

import android.app.Activity
import android.app.ActivityManager
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Handler
import android.view.View
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme

class CanvasRenderer(
    private val canvasView: View,
    private val mapScheme: MapScheme,
    private var renderProgram: RenderProgram
) {

    private var cache: MapCache? = null
    private var oldCache: MapCache? = null

    private val matrix = Matrix()
    private val mInvertedMatrix = Matrix()
    private val renderMatrix = Matrix()

    private val screenRect = RectF()
    private val schemeRect = RectF()

    private val renderViewPort = RectF()
    private val renderViewPortVertical = RectF()
    private val renderViewPortHorizontal = RectF()
    private val renderViewPortIntersection = RectF()

    private val memoryClass: Int
    private var maximumBitmapWidth: Int = 0
    private var maximumBitmapHeight: Int = 0

    private var scale: Float = 0f
    private var currentX: Float = 0f
    private var currentY: Float = 0f
    private var currentWidth: Float = 0f
    private var currentHeight: Float = 0f

    private val matrixValues = FloatArray(9)

    private var isRenderFailed = false
    private var isUpdatesEnabled = false
    private var isEntireMapCached = false

    private val mPrivateHandler = Handler()

    init {
        val ac = canvasView.context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        memoryClass = ac.memoryClass
        setScheme(renderProgram)
    }

    fun setScheme(renderProgram: RenderProgram) {
        this.renderProgram = renderProgram

        val m = Matrix()
        m.setTranslate(1.0f, 1.0f)
        setMatrix(m)

        recycleCache()
    }

    fun setUpdatesEnabled(enabled: Boolean) {
        isUpdatesEnabled = enabled
    }

    fun draw(canvas: Canvas): Boolean {
        maximumBitmapWidth = canvas.maximumBitmapWidth
        maximumBitmapHeight = canvas.maximumBitmapHeight

        if (cache == null) {
            rebuildCache()
        }
        if (isRenderFailed) {
            return false
        }
        val m = renderMatrix
        val c = cache!!
        val img = c.image ?: return false
        if (c.scale != scale) {
            m.set(c.invertedMatrix)
            m.postConcat(matrix)
        } else {
            m.setTranslate(currentX - c.x, currentY - c.y)
        }
        canvas.clipRect(screenRect)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(img, m, null)

        if (isUpdatesEnabled) {
            if (c.scale != scale) {
                postRebuildCache()
            } else if (!isEntireMapCached && !c.hit(schemeRect)) {
                postUpdateCache()
            }
        }

        return !isRenderFailed
    }

    @Synchronized
    fun setMatrix(newMatrix: Matrix) {
        matrix.set(newMatrix)
        matrix.invert(mInvertedMatrix)
        matrix.getValues(matrixValues)
        scale = matrixValues[Matrix.MSCALE_X]
        currentX = matrixValues[Matrix.MTRANS_X]
        currentY = matrixValues[Matrix.MTRANS_Y]
        currentWidth = mapScheme.width * scale
        currentHeight = mapScheme.height * scale

        updateViewRect()

        isRenderFailed = false
    }

    fun updateViewRect() {
        schemeRect.set(0f, 0f, canvasView.width.toFloat(), canvasView.height.toFloat())
        mInvertedMatrix.mapRect(schemeRect)
        screenRect.set(schemeRect)
        matrix.mapRect(screenRect)
    }

    @Synchronized
    fun rebuildCache() {
        recycleCache()
        isEntireMapCached = false
        if (currentWidth > maximumBitmapWidth || currentHeight > maximumBitmapHeight) {
            renderPartialCache()
            return
        }

        val memoryLimit = 4 * 1024 * 1024 * memoryClass / 16
        val bitmapSize = currentWidth.toInt() * currentHeight.toInt() * 2
        if (bitmapSize > memoryLimit) {
            renderPartialCache()
            return
        }
        try {
            renderEntireCache()
            isEntireMapCached = true
        } catch (ex: OutOfMemoryError) {
            recycleCache()
            renderPartialCache()
        }
    }

    @Synchronized
    private fun renderEntireCache() {
        try {
            val viewRect = RectF(0f, 0f, currentWidth, currentHeight)
            val m = Matrix(matrix)
            m.postTranslate(-currentX, -currentY)
            val i = Matrix()
            m.invert(i)

            val newCache = MapCache.reuse(
                oldCache,
                currentWidth.toInt(),
                currentHeight.toInt(),
                m,
                i,
                0f,
                0f,
                scale,
                viewRect
            )

            val img = newCache.image ?: return
            val c = Canvas(img)
            c.drawColor(Color.WHITE)
            c.setMatrix(newCache.cacheMatrix)

            val elements = renderProgram.getAllDrawingElements()
            c.drawColor(Color.WHITE)
            elements.forEach { it.draw(c) }

            cache = newCache
        } catch (ex: Exception) {
            isRenderFailed = true
        }
    }

    @Synchronized
    private fun renderPartialCache() {
        try {
            val newCache = MapCache.reuse(
                oldCache,
                canvasView.width,
                canvasView.height,
                matrix,
                mInvertedMatrix,
                currentX,
                currentY,
                scale,
                schemeRect
            )

            val img = newCache.image ?: return
            val c = Canvas(img)
            c.setMatrix(newCache.cacheMatrix)
            c.clipRect(newCache.schemeRect)
            val elements = renderProgram.getClippedDrawingElements(newCache.schemeRect)
            c.drawColor(Color.WHITE)
            elements.forEach { it.draw(c) }
            oldCache = cache
            cache = newCache
        } catch (ex: Exception) {
            isRenderFailed = true
        }
    }

    @Synchronized
    private fun updatePartialCache() {
        try {
            val newCache = MapCache.reuse(
                oldCache,
                canvasView.width,
                canvasView.height,
                matrix,
                mInvertedMatrix,
                currentX,
                currentY,
                scale,
                schemeRect
            )

            val img = newCache.image ?: return
            val c = Canvas(img)

            val renderAll = splitRenderViewPort(newCache.schemeRect, cache!!.schemeRect)
            if (renderAll) {
                c.setMatrix(newCache.cacheMatrix)
                c.clipRect(newCache.schemeRect)
                val elements = renderProgram.getClippedDrawingElements(newCache.schemeRect)
                c.drawColor(Color.WHITE)
                elements.forEach { it.draw(c) }
            } else {
                c.save()
                c.setMatrix(newCache.cacheMatrix)
                c.clipRect(newCache.schemeRect)
                val elements = renderProgram.getClippedDrawingElements(renderViewPortHorizontal, renderViewPortVertical)
                c.drawColor(Color.WHITE)
                elements.forEach { it.draw(c) }
                c.restore()
                c.drawBitmap(cache!!.image!!, newCache.x - cache!!.x, newCache.y - cache!!.y, null)
            }

            oldCache = cache
            cache = newCache

            if (!renderAll) {
                mPrivateHandler.removeCallbacks(renderPartialCacheRunnable)
                mPrivateHandler.postDelayed(renderPartialCacheRunnable, 300)
            }
        } catch (ex: Exception) {
            isRenderFailed = true
        }
    }

    private fun splitRenderViewPort(schemeRect: RectF, cacheRect: RectF): Boolean {
        val vp = renderViewPort
        val v = renderViewPortVertical
        val h = renderViewPortHorizontal
        val i = renderViewPortIntersection
        vp.set(schemeRect)
        renderViewPortVertical.set(vp)
        renderViewPortHorizontal.set(vp)
        renderViewPortIntersection.set(vp)
        renderViewPortIntersection.intersect(cacheRect)
        var renderAll = false

        if (vp.right == i.right && vp.bottom == i.bottom) {
            h.bottom = i.top
            v.right = i.left
        } else if (vp.right == i.right && vp.top == i.top) {
            h.top = i.bottom
            v.right = i.left
        } else if (vp.left == i.left && vp.bottom == i.bottom) {
            h.bottom = i.top
            v.left = i.right
        } else if (vp.left == i.left && vp.top == i.top) {
            h.top = i.bottom
            v.left = i.right
        } else {
            renderAll = true
        }
        return renderAll
    }

    private val renderPartialCacheRunnable: Runnable = Runnable {
        renderPartialCache()
        canvasView.invalidate()
    }

    private val rebuildCacheRunnable: Runnable = Runnable {
        rebuildCache()
        canvasView.invalidate()
    }

    private val updateCacheRunnable: Runnable = Runnable {
        val oc = oldCache
        if (oc != null && oc.scale == scale) {
            updatePartialCache()
        } else {
            renderPartialCache()
            canvasView.invalidate()
        }
    }

    fun recycleCache() {
        cache?.let {
            it.image?.recycle()
            it.image = null
            cache = null
        }
        oldCache?.let {
            it.image?.recycle()
            it.image = null
            oldCache = null
        }
        System.gc()
    }

    private fun postRebuildCache() {
        mPrivateHandler.removeCallbacks(rebuildCacheRunnable)
        mPrivateHandler.removeCallbacks(renderPartialCacheRunnable)
        mPrivateHandler.removeCallbacks(updateCacheRunnable)
        mPrivateHandler.post(rebuildCacheRunnable)
    }

    private fun postUpdateCache() {
        mPrivateHandler.removeCallbacks(rebuildCacheRunnable)
        mPrivateHandler.removeCallbacks(renderPartialCacheRunnable)
        mPrivateHandler.removeCallbacks(updateCacheRunnable)
        mPrivateHandler.post(updateCacheRunnable)
    }

    fun onAttachedToWindow() {
        // do nothing
    }

    fun onDetachedFromWindow() {
        // do nothing
    }

    private class MapCache {
        val cacheMatrix = Matrix()
        val invertedMatrix = Matrix()

        var scale: Float = 0f
        var x: Float = 0f
        var y: Float = 0f

        val schemeRect = RectF()
        var image: Bitmap? = null

        fun equals(width: Int, height: Int): Boolean {
            return image?.width == width && image?.height == height
        }

        fun hit(viewRect: RectF): Boolean {
            return schemeRect.contains(viewRect)
        }

        companion object {
            fun reuse(
                oldCache: MapCache?,
                width: Int,
                height: Int,
                matrix: Matrix,
                invertedMatrix: Matrix,
                x: Float,
                y: Float,
                scale: Float,
                schemeRect: RectF
            ): MapCache {
                val newCache: MapCache
                if (oldCache != null) {
                    newCache = oldCache
                    if (!newCache.equals(width, height)) {
                        newCache.image?.recycle()
                        newCache.image = null
                        System.gc()
                    }
                } else {
                    newCache = MapCache()
                }
                if (newCache.image == null) {
                    newCache.image = Bitmap.createBitmap(width, height, Config.RGB_565)
                }

                newCache.cacheMatrix.set(matrix)
                newCache.invertedMatrix.set(invertedMatrix)
                newCache.x = x
                newCache.y = y
                newCache.scale = scale
                newCache.schemeRect.set(schemeRect)

                return newCache
            }
        }
    }
}
