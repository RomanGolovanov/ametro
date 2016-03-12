package org.ametro.ui.widgets;

import android.animation.Animator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.model.entities.MapSchemeLine;
import org.ametro.model.entities.MapSchemeStation;

public class MapBottomPanelWidget implements Animator.AnimatorListener {

    private final View view;

    private final TextView stationTextView;
    private final TextView lineTextView;

    private final Button detailButton;
    private final Button beginButton;
    private final Button endButton;

    private final IMapBottomPanelEventListener listener;

    private final Runnable hideAnimation = new Runnable() {
        @Override
        public void run() {
            view.animate().setDuration(Constants.ANIMATION_DURATION).setListener(MapBottomPanelWidget.this).translationY(view.getHeight());
        }
    };

    private final Runnable showAnimation = new Runnable() {
        @Override
        public void run() {
            view.setVisibility(View.VISIBLE);
            stationTextView.setText(station.getDisplayName());
            lineTextView.setText(line.getDisplayName());
            view.animate().setDuration(Constants.ANIMATION_DURATION).setListener(MapBottomPanelWidget.this).translationY(0);
        }
    };

    private Runnable actionOnEndAnimation;
    private boolean visible;
    private boolean firstTime;

    private MapSchemeLine line;
    private MapSchemeStation station;

    public MapBottomPanelWidget(ViewGroup view, IMapBottomPanelEventListener listener) {
        this.view = view;
        this.listener = listener;
        this.visible = false;
        this.firstTime = true;

        stationTextView = (TextView) view.findViewById(R.id.station);
        lineTextView = (TextView) view.findViewById(R.id.line);
        detailButton = (Button) view.findViewById(R.id.button_details);
        beginButton = (Button) view.findViewById(R.id.button_begin);
        endButton = (Button) view.findViewById(R.id.button_end);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v == detailButton) {
                    MapBottomPanelWidget.this.listener.onShowMapDetail(line, station);
                } else if (v == beginButton) {
                    MapBottomPanelWidget.this.listener.onSelectBeginStation(line, station);
                } else if (v == endButton) {
                    MapBottomPanelWidget.this.listener.onSelectEndStation(line, station);
                }
            }
        };

        view.setOnClickListener(clickListener);
        detailButton.setOnClickListener(clickListener);
        beginButton.setOnClickListener(clickListener);
        endButton.setOnClickListener(clickListener);
    }

    public boolean isOpened() {
        return visible;
    }

    public void show(final MapSchemeLine line, final MapSchemeStation station, boolean showDetails) {

        detailButton.setVisibility(showDetails ? View.VISIBLE : View.INVISIBLE);

        if (visible && this.line == line && this.station == station) {
            return;
        }

        this.line = line;
        this.station = station;

        if (!visible && !firstTime) {
            visible = true;
            showAnimation.run();
            return;
        }

        visible = true;
        firstTime = false;
        actionOnEndAnimation = showAnimation;
        hideAnimation.run();
    }

    public void hide() {
        if (!visible) {
            return;
        }
        visible = false;
        hideAnimation.run();
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (!visible)
            view.setVisibility(View.INVISIBLE);

        if (actionOnEndAnimation != null) {
            actionOnEndAnimation.run();
            actionOnEndAnimation = null;
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public interface IMapBottomPanelEventListener {
        void onShowMapDetail(MapSchemeLine line, MapSchemeStation station);

        void onSelectBeginStation(MapSchemeLine line, MapSchemeStation station);

        void onSelectEndStation(MapSchemeLine line, MapSchemeStation station);
    }

}
