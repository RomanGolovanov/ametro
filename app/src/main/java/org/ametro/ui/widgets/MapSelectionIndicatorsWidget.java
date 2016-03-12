package org.ametro.ui.widgets;

import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;

import org.ametro.model.entities.MapPoint;
import org.ametro.model.entities.MapSchemeStation;
import org.ametro.ui.views.MultiTouchMapView;

public class MapSelectionIndicatorsWidget implements MultiTouchMapView.IViewportChangedListener {

    private final IMapSelectionEventListener listener;
    private final View beginIndicator;
    private final View endIndicator;
    private MapSchemeStation beginStation;
    private MapSchemeStation endStation;
    private Matrix viewMatrix = new Matrix();

    public MapSelectionIndicatorsWidget(IMapSelectionEventListener listener, View beginIndicator, View endIndicator) {
        this.listener = listener;
        this.beginIndicator = beginIndicator;
        this.endIndicator = endIndicator;
    }

    public void setBeginStation(MapSchemeStation station) {
        if (endStation == station) {
            if(beginStation!=null && endStation!=null){
                listener.onRouteSelectionCleared();
            }
            endStation = null;
        }
        beginStation = station;
        updateIndicatorsPositionAndState();
        if (beginStation != null && endStation != null) {
            listener.onRouteSelectionComplete(beginStation, endStation);
        }
    }

    public void setEndStation(MapSchemeStation station) {
        if (beginStation == station) {
            if(beginStation!=null && endStation!=null){
                listener.onRouteSelectionCleared();
            }
            beginStation = null;
        }
        endStation = station;
        updateIndicatorsPositionAndState();
        if (beginStation != null && endStation != null) {
            listener.onRouteSelectionComplete(beginStation, endStation);
        }
    }

    public MapSchemeStation getBeginStation() {
        return beginStation;
    }

    public MapSchemeStation getEndStation() {
        return endStation;
    }

    public void clearSelection() {
        beginStation = null;
        endStation = null;
        updateIndicatorsPositionAndState();
        listener.onRouteSelectionCleared();
    }

    public boolean hasSelection() {
        return beginStation != null || endStation != null;
    }

    @Override
    public void onViewportChanged(Matrix matrix) {
        viewMatrix.set(matrix);
        updateIndicatorsPositionAndState();
    }

    private void updateIndicatorsPositionAndState() {
        if (beginStation != null) {
            beginIndicator.setVisibility(View.VISIBLE);
            setViewPosition(beginIndicator, beginStation.getPosition());
        } else {
            beginIndicator.setVisibility(View.INVISIBLE);
        }

        if (endStation != null) {
            endIndicator.setVisibility(View.VISIBLE);
            setViewPosition(endIndicator, endStation.getPosition());
        } else {
            endIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void setViewPosition(View view, MapPoint point) {
        float[] pts = new float[2];
        pts[0] = point.x;
        pts[1] = point.y;
        viewMatrix.mapPoints(pts);
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        p.setMargins(Math.round(pts[0] - view.getWidth() / 4), Math.round(pts[1] - view.getHeight()), 0, 0);
        view.requestLayout();
    }

    public interface IMapSelectionEventListener {
        void onRouteSelectionComplete(MapSchemeStation begin, MapSchemeStation end);

        void onRouteSelectionCleared();
    }
}
