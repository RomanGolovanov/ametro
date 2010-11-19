package org.ametro.multitouch;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.widget.ZoomControls;

public class ZoomController<T> {

	Context mContext;
	MultiTouchController<T> mController;
	ZoomControls mZoomControls;
	Handler mPrivateHandler = new Handler();
	
	public ZoomController(Context context, MultiTouchController<T> controller, ZoomControls controls){
		mContext = context;
		mController = controller;
		mZoomControls = controls;

		mZoomControls.setVisibility(View.INVISIBLE);
		mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mController.doZoomAnimation(1.5f);
			}
		});
		mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mController.doZoomAnimation(1/1.5f);
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
		mPrivateHandler.postDelayed(mZoomControlRunnable, ViewConfiguration.getZoomControlsTimeout());
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

	void invalidate(){
		float max = mController.getMaxScale();
		float min = mController.getMinScale();
		float scale = mController.getScale();
		mZoomControls.setIsZoomOutEnabled( scale > min );
		mZoomControls.setIsZoomInEnabled( scale < max );
	}
	
	public void showZoom(){
		invalidate();
		if (mZoomControls.getVisibility() != View.VISIBLE) {
			show();
		}
		delay();
	}
			
}
