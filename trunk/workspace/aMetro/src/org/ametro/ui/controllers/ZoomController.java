package org.ametro.ui.controllers;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.widget.ZoomControls;

public class ZoomController {

	private MultiTouchController mController;
	private ZoomControls mZoomControls;
	private Handler mPrivateHandler = new Handler();
	private boolean mEnabled;

	public ZoomController(Context context, MultiTouchController controller, ZoomControls controls) {
		mEnabled = true;
		mController = controller;
		mZoomControls = controls;

		mZoomControls.setVisibility(View.INVISIBLE);
		mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mController.doZoomAnimation(MultiTouchController.ZOOM_IN);
			}
		});
		mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mController.doZoomAnimation(MultiTouchController.ZOOM_OUT);
			}
		});
	}

	private Runnable mZoomControlRunnable = new Runnable() {
		public void run() {
			if (!mZoomControls.hasFocus()) {
				hide();
			} else {
				delay();
			}
		}
	};

	void delay() {
		mPrivateHandler.removeCallbacks(mZoomControlRunnable);
		mPrivateHandler.postDelayed(mZoomControlRunnable,
				ViewConfiguration.getZoomControlsTimeout());
	}

	void show() {
		fade(View.VISIBLE, 0.0f, 1.0f);
	}

	void hide() {
		fade(View.INVISIBLE, 1.0f, 0.0f);
	}

	void fade(int visibility, float startAlpha, float endAlpha) {
		AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
		anim.setDuration(500);
		mZoomControls.startAnimation(anim);
		mZoomControls.setVisibility(visibility);
	}

	void invalidate() {
		float max = mController.getMaxScale();
		float min = mController.getMinScale();
		float scale = mController.getScale();
		mZoomControls.setIsZoomOutEnabled(Math.abs(scale - min) > 0.01f);
		mZoomControls.setIsZoomInEnabled(Math.abs(scale - max) > 0.01f);
	}

	public void showZoom() {
		if(!mEnabled){
			return;
		}
		invalidate();
		if (mZoomControls.getVisibility() != View.VISIBLE) {
			show();
		}
		delay();
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
		if(!enabled){
			hide();
		}
	}

}
