package org.ametro.multitouch;

import org.ametro.model.MapView;
import org.ametro.multitouch.MultiTouchController.MultiTouchListener;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MultiTouchMapView extends FrameLayout implements MultiTouchListener<VectorMapView> {

	protected  static final String TAG = "MultiTouchMapView";
	
	private MultiTouchController<VectorMapView> mController;
	private VectorMapView mMapView;
	private int mTouchMode;
	
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

	
	public void onTouchModeChanged(int mode) {
		if(this.mTouchMode == MultiTouchController.MODE_ZOOM || this.mTouchMode == MultiTouchController.MODE_ZOOM_ANIMATION){
			mMapView.rebuildCache();
		}
		mMapView.enableUpdates(mode != MultiTouchController.MODE_ZOOM && mode!= MultiTouchController.MODE_ZOOM_ANIMATION );
		this.mTouchMode = mode;
	}

	public void onPerformClick(PointF position) {
		Toast.makeText(getContext(), "click in " + position.x + "," + position.y, Toast.LENGTH_SHORT).show();
		mController.zoomIn();
	}

	public void onPerformLongClick(PointF position) {
		Toast.makeText(getContext(), "long click in " + position.x + "," + position.y, Toast.LENGTH_SHORT).show();
		mController.zoomOut();
	}

}