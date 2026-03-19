package io.github.romangolovanov.apps.ametro.ui.widgets

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.github.romangolovanov.apps.ametro.R
import io.github.romangolovanov.apps.ametro.app.Constants
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation

class MapBottomPanelWidget(viewGroup: ViewGroup, private val listener: IMapBottomPanelEventListener) :
    Animator.AnimatorListener {

    private val view: View = viewGroup

    private val stationTextView: TextView = viewGroup.findViewById(R.id.station)
    private val lineTextView: TextView = viewGroup.findViewById(R.id.line)

    private val detailButton: Button = viewGroup.findViewById(R.id.button_details)
    private val beginButton: Button = viewGroup.findViewById(R.id.button_begin)
    private val endButton: Button = viewGroup.findViewById(R.id.button_end)

    private val hideAnimation = Runnable {
        view.animate()
            .setDuration(Constants.ANIMATION_DURATION)
            .setListener(this@MapBottomPanelWidget)
            .translationY(view.height.toFloat())
    }

    private val showAnimation = Runnable {
        view.visibility = View.VISIBLE
        stationTextView.text = station!!.displayName
        lineTextView.text = line!!.displayName
        view.animate()
            .setDuration(Constants.ANIMATION_DURATION)
            .setListener(this@MapBottomPanelWidget)
            .translationY(0f)
    }

    private var actionOnEndAnimation: Runnable? = null
    private var visible = false
    private var firstTime = true

    private var line: MapSchemeLine? = null
    private var station: MapSchemeStation? = null

    init {
        val clickListener = View.OnClickListener { v ->
            when (v) {
                detailButton -> listener.onShowMapDetail(line!!, station!!)
                beginButton -> listener.onSelectBeginStation(line!!, station!!)
                endButton -> listener.onSelectEndStation(line!!, station!!)
            }
        }

        viewGroup.setOnClickListener(clickListener)
        detailButton.setOnClickListener(clickListener)
        beginButton.setOnClickListener(clickListener)
        endButton.setOnClickListener(clickListener)
    }

    val isOpened: Boolean get() = visible

    fun show(line: MapSchemeLine, station: MapSchemeStation, showDetails: Boolean) {
        detailButton.visibility = if (showDetails) View.VISIBLE else View.INVISIBLE

        if (visible && this.line == line && this.station == station) return

        this.line = line
        this.station = station

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

    interface IMapBottomPanelEventListener {
        fun onShowMapDetail(line: MapSchemeLine, station: MapSchemeStation)
        fun onSelectBeginStation(line: MapSchemeLine, station: MapSchemeStation)
        fun onSelectEndStation(line: MapSchemeLine, station: MapSchemeStation)
    }
}
