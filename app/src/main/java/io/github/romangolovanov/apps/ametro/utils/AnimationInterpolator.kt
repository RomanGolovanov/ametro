package io.github.romangolovanov.apps.ametro.utils

import android.graphics.PointF

class AnimationInterpolator {

    private val mStartPoint = PointF()
    private val mEndPoint = PointF()
    private val mNowPoint = PointF()

    private var mStartScale = 0f
    private var mEndScale = 0f

    private var mPeriod = 0L
    private var mEndTime = 0L
    private var mNowTime = 0L

    private var mMode = 0

    fun begin(startPoint: PointF?, endPoint: PointF?, startScale: Float?, endScale: Float?, time: Long) {
        mNowTime = System.currentTimeMillis()
        mEndTime = mNowTime + time
        mPeriod = time
        mMode = 0
        if (startScale != null && endScale != null) {
            mStartScale = startScale
            mEndScale = endScale
            mMode = mMode or SCALE
        }
        if (startPoint != null && endPoint != null) {
            mStartPoint.set(startPoint)
            mEndPoint.set(endPoint)
            mNowPoint.set(startPoint)
            mMode = mMode or SCROLL
        }
    }

    fun more(): Boolean = mNowTime < mEndTime

    fun next() {
        mNowTime = System.currentTimeMillis()
    }

    fun getPoint(): PointF {
        return if (mNowTime < mEndTime) {
            val k = getProgress()
            val x = mEndPoint.x - k * (mEndPoint.x - mStartPoint.x)
            val y = mEndPoint.y - k * (mEndPoint.y - mStartPoint.y)
            mNowPoint.set(x, y)
            mNowPoint
        } else {
            mEndPoint
        }
    }

    fun getScale(): Float {
        return if (mNowTime < mEndTime) {
            val k = getProgress()
            mEndScale - k * (mEndScale - mStartScale)
        } else {
            mEndScale
        }
    }

    private fun getProgress(): Float = (mEndTime - mNowTime).toFloat() / mPeriod

    fun hasScale(): Boolean = (mMode and SCALE) != 0

    fun hasScroll(): Boolean = (mMode and SCROLL) != 0

    companion object {
        const val SCALE = 1
        const val SCROLL = 2
        const val SCALE_AND_SCROLL = 3
    }
}
