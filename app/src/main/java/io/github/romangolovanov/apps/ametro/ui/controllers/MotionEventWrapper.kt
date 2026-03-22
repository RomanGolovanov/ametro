package io.github.romangolovanov.apps.ametro.ui.controllers

import android.util.Log
import android.view.MotionEvent

open class MotionEventWrapper(internal var event: MotionEvent) {

    companion object {
        const val TAG = "MotionEventWrapper"

        val ACTION_POINTER_UP: Int
        val ACTION_POINTER_DOWN: Int
        val HasMultiTouchSupport: Boolean

        init {
            var succeeded = false
            try {
                MotionEvent::class.java.getMethod("getPointerCount")
                succeeded = true
            } catch (e: Exception) {
                Log.e(TAG, "Methods static initializer failed", e)
            }
            HasMultiTouchSupport = succeeded
            var pointerDown = 5
            var pointerUp = 6
            if (HasMultiTouchSupport) {
                try {
                    pointerDown = MotionEvent::class.java.getField("ACTION_POINTER_DOWN").getInt(null)
                    pointerUp = MotionEvent::class.java.getField("ACTION_POINTER_UP").getInt(null)
                } catch (e: Exception) {
                    Log.e(TAG, "Constants static initializer failed", e)
                }
            }
            ACTION_POINTER_DOWN = pointerDown
            ACTION_POINTER_UP = pointerUp
        }

        fun create(event: MotionEvent): MotionEventWrapper {
            return if (HasMultiTouchSupport) {
                MotionEventMultiTouchWrapper(event)
            } else {
                MotionEventWrapper(event)
            }
        }
    }

    open fun getAction(): Int = event.action

    open fun getX(): Float = event.x

    open fun getY(): Float = event.y

    open fun getX(pos: Int): Float = 0f

    open fun getY(pos: Int): Float = 0f

    fun getEventTime(): Long = event.eventTime
}
