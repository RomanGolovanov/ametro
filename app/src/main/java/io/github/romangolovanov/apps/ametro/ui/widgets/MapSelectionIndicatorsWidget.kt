package io.github.romangolovanov.apps.ametro.ui.widgets

import android.graphics.Matrix
import android.view.View
import android.view.ViewGroup
import io.github.romangolovanov.apps.ametro.model.entities.MapPoint
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation
import io.github.romangolovanov.apps.ametro.ui.views.MultiTouchMapView

class MapSelectionIndicatorsWidget(
    private val listener: IMapSelectionEventListener,
    private val beginIndicator: View,
    private val endIndicator: View
) : MultiTouchMapView.IViewportChangedListener {

    var beginStation: MapSchemeStation? = null
        private set
    var endStation: MapSchemeStation? = null
        private set
    private var viewMatrix = Matrix()

    fun setBeginStation(station: MapSchemeStation?) {
        if (endStation == station) {
            if (beginStation != null && endStation != null) {
                listener.onRouteSelectionCleared()
            }
            endStation = null
        }
        beginStation = station
        updateIndicatorsPositionAndState()
        if (beginStation != null && endStation != null) {
            listener.onRouteSelectionComplete(beginStation, endStation)
        }
    }

    fun setEndStation(station: MapSchemeStation?) {
        if (beginStation == station) {
            if (beginStation != null && endStation != null) {
                listener.onRouteSelectionCleared()
            }
            beginStation = null
        }
        endStation = station
        updateIndicatorsPositionAndState()
        if (beginStation != null && endStation != null) {
            listener.onRouteSelectionComplete(beginStation, endStation)
        }
    }

    fun clearSelection() {
        beginStation = null
        endStation = null
        updateIndicatorsPositionAndState()
        listener.onRouteSelectionCleared()
    }

    fun hasSelection(): Boolean = beginStation != null || endStation != null

    override fun onViewportChanged(matrix: Matrix) {
        viewMatrix.set(matrix)
        updateIndicatorsPositionAndState()
    }

    private fun updateIndicatorsPositionAndState() {
        if (beginStation != null) {
            beginIndicator.visibility = View.VISIBLE
            setViewPosition(beginIndicator, beginStation!!.position!!)
        } else {
            beginIndicator.visibility = View.INVISIBLE
        }

        if (endStation != null) {
            endIndicator.visibility = View.VISIBLE
            setViewPosition(endIndicator, endStation!!.position!!)
        } else {
            endIndicator.visibility = View.INVISIBLE
        }
    }

    private fun setViewPosition(view: View, point: MapPoint) {
        val pts = floatArrayOf(point.x, point.y)
        viewMatrix.mapPoints(pts)
        val p = view.layoutParams as ViewGroup.MarginLayoutParams
        p.setMargins(Math.round(pts[0] - view.width / 4), Math.round(pts[1] - view.height), 0, 0)
        view.requestLayout()
    }

    interface IMapSelectionEventListener {
        fun onRouteSelectionComplete(begin: MapSchemeStation?, end: MapSchemeStation?)
        fun onRouteSelectionCleared()
    }
}
