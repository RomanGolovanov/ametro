package io.github.romangolovanov.apps.ametro.ui.controllers

import android.view.MotionEvent

class MotionEventMultiTouchWrapper(event: MotionEvent) : MotionEventWrapper(event) {

    override fun getAction(): Int = event.action and MotionEvent.ACTION_MASK

    override fun getX(pos: Int): Float = event.getX(pos)

    override fun getY(pos: Int): Float = event.getY(pos)
}
