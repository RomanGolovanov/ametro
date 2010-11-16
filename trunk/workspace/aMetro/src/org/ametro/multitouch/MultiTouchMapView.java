package org.ametro.multitouch;

import org.ametro.model.MapView;
import org.ametro.multitouch.MultiTouchController.MultiTouchListener;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MultiTouchMapView extends FrameLayout implements MultiTouchListener<VectorMapView> {

	@SuppressWarnings("unused")
	private static final String TAG = "MultiTouchMapView";
	
	private MultiTouchController<VectorMapView> mController;
	private VectorMapView mMapView;
	
	public MultiTouchMapView(Context context, MapView scheme) {
		super(context);
		// create map image
		mMapView = new VectorMapView(getContext(), scheme);
		addView(mMapView);
		// create controller
		mController = new MultiTouchController<VectorMapView>(getContext(),this);
	}

	private void updateViewRect() {
		float height = mMapView.getContentHeight();
		float width = mMapView.getContentWidth();
		mController.setViewRect(width, height, new RectF(0, 0, getWidth(), getHeight()));
	}
	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		updateViewRect();
		super.onLayout(changed, left, top, right, bottom);
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateViewRect();
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			updateViewRect();
		}
	}	

	public boolean onTouchEvent(MotionEvent event) {
		return mController.onMultiTouchEvent(event);
	}
	
	public Matrix getPositionAndScaleMatrix() {
		return mMapView.getMatrix();
	}

	public void setPositionAndScaleMatrix(Matrix matrix) {
		matrix.set(matrix);
		mMapView.setMatrix(matrix,true);
	}

	private int mode;
	
	public void onTouchModeChanged(int mode) {
		boolean isUserInteractionFinished = (mode == MultiTouchController.MODE_NONE && this.mode!= MultiTouchController.MODE_NONE);
		if(isUserInteractionFinished && this.mode == MultiTouchController.MODE_ZOOM){
			mMapView.rebuildCache();
		}
		mMapView.enableUpdates(mode != MultiTouchController.MODE_ZOOM);
		this.mode = mode;
	}
}