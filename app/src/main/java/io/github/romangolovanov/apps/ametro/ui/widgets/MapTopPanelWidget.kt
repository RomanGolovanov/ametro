package io.github.romangolovanov.apps.ametro.ui.widgets

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.Constants

class MapTopPanelWidget(viewGroup: ViewGroup) : Animator.AnimatorListener {

    private val view: View = viewGroup
    private val textView: TextView = viewGroup.findViewById(R.id.message)
    private var text: String? = null

    private val hideAnimation = Runnable {
        view.animate()
            .setDuration(Constants.ANIMATION_DURATION)
            .setListener(this@MapTopPanelWidget)
            .translationY(-view.height.toFloat())
    }

    private val showAnimation = Runnable {
        view.visibility = View.VISIBLE
        textView.text = text
        view.animate()
            .setDuration(Constants.ANIMATION_DURATION)
            .setListener(this@MapTopPanelWidget)
            .translationY(0f)
    }

    private var actionOnEndAnimation: Runnable? = null
    private var visible = false
    private var firstTime = true

    fun show(newText: String?) {
        if (visible && text != null && newText != null && text == newText) return

        text = newText

        if (!visible && !firstTime) {
            visible = true
            showAnimation.run()
            return
        }

        visible = true
        firstTime = false
        actionOnEndAnimation = showAnimation
        hideAnimation.run()
    }

    fun hide() {
        if (!visible) return
        visible = false
        hideAnimation.run()
    }

    override fun onAnimationStart(animation: Animator) {}

    override fun onAnimationEnd(animation: Animator) {
        if (!visible) view.visibility = View.INVISIBLE
        actionOnEndAnimation?.run()
        actionOnEndAnimation = null
    }

    override fun onAnimationCancel(animation: Animator) {}

    override fun onAnimationRepeat(animation: Animator) {}
}
