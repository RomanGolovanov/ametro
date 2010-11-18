package org.ametro.multitouch;

import java.util.ArrayList;

import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.multitouch.MultiTouchController.MultiTouchListener;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ZoomControls;

public class MultiTouchMapView extends FrameLayout implements MultiTouchListener<VectorMapView> {

	protected  static final String TAG = "MultiTouchMapView";
	
	private MultiTouchController<VectorMapView> mController;
	private VectorMapView mMapView;
	private int mTouchMode;
	private ZoomController<VectorMapView> mZoomController;
	
	public MultiTouchMapView(Context context, SchemeView scheme) {
		super(context);
		// create map image
		mMapView = new VectorMapView(getContext(), scheme);
		//mZoomControls
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
		if(mZoomController!=null){
			mZoomController.showZoom();
		}
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
		if(mZoomController!=null){
			mZoomController.showZoom();
		}
	}

	public void onPerformLongClick(PointF position) {
	}

	
	public PointF getTouchPoint() {
		return mController.getTouchPoint();
	}

	
	
	public void setAntiAliasingDisableOnScroll(boolean antiAliasingDisableOnScroll) {
		// TODO Auto-generated method stub
	}

	public void setAntiAliasingEnabled(boolean antiAliasingEnabled) {
		// TODO Auto-generated method stub
	}


	public void setCenterPositionAndScale(PointF position, Float zoom) {
		setCenterPositionAndScale(position, zoom, false);
	}
	
	public float getPositionAndScale(PointF position) {
		position.set(mController.getCenterPosition());
		return mController.getScale();
	}

	public void setScheme(SchemeView scheme) {
		mMapView.setScheme(scheme);
	}

	public void setSchemeSelection(ArrayList<StationView> stations,
			ArrayList<SegmentView> segments,
			ArrayList<TransferView> transfers) {
		mMapView.setSchemeSelection(stations,segments,transfers);
	}
	
	public void setZoomControls(ZoomControls zoomControls) {
		mZoomController = new ZoomController<VectorMapView>(getContext(), mController, zoomControls);
	}	
	
	public void setCenterPositionAndScale(PointF position, Float zoom, boolean animated) {
		// TODO Auto-generated method stub
	}

	public float getScale() {
		return mController.getScale();
	}


}