package org.ametro.ui.widgets;


import android.animation.Animator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.app.Constants;

public class MapTopPanelWidget implements Animator.AnimatorListener {

    private final View view;
    private final TextView textView;
    private String text;

    private final Runnable hideAnimation = new Runnable() {
        @Override
        public void run() {
            view.animate().setDuration(Constants.ANIMATION_DURATION).setListener(MapTopPanelWidget.this).translationY(-view.getHeight());
        }
    };

    private final Runnable showAnimation = new Runnable() {
        @Override
        public void run() {
            view.setVisibility(View.VISIBLE);
            textView.setText(text);
            view.animate().setDuration(Constants.ANIMATION_DURATION).setListener(MapTopPanelWidget.this).translationY(0);
        }
    };

    private Runnable actionOnEndAnimation;
    private boolean visible;
    private boolean firstTime;

    public MapTopPanelWidget(ViewGroup view) {
        this.view = view;
        this.textView = (TextView)view.findViewById(R.id.message);
        this.visible = false;
        this.firstTime = true;
    }

    public void show(final String newText) {
        if (visible && (text != null && newText!=null && text.equals(newText))) {
            return;
        }

        text = newText;

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
}
