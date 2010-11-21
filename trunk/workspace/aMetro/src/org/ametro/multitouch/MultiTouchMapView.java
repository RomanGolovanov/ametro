package org.ametro.multitouch;

import java.util.ArrayList;

import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.multitouch.MultiTouchController.MultiTouchListener;
import org.ametro.util.MathUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.ZoomControls;

public class MultiTouchMapView extends ScrollView implements MultiTouchListener {

	protected  static final String TAG = "MultiTouchMapView";
	
	private MultiTouchController mController;
	private ZoomController mZoomController;
	private KeyEventController mKeyEventController;

	private SchemeView mScheme;
	private VectorMapRenderer mMapView;
	
	private PointF mLastClickPosition;
	private float mDblClickSlop;
	
	private float[] values = new float[9];
	
	private int mVerticalScrollOffset;
	private int mHorizontalScrollOffset;
	private int mVerticalScrollRange;
	private int mHorizontalScrollRange;
	
	private Handler mPrivateHandler = new Handler();
	
	private PointF mChangeCenterPoint;
	private Float mChangeScale;
	
	public MultiTouchMapView(Context context, SchemeView scheme) {
		super(context);
		setFadingEdgeLength(0);
		setScrollbarFadingEnabled(true);
        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(true);

        setFocusable(true);
        setFocusableInTouchMode(true);
        
		mScheme = scheme;
		mMapView = new VectorMapRenderer(this, scheme);
		mController = new MultiTouchController(getContext(),this);
		mKeyEventController = new KeyEventController(mController);
		mDblClickSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
	}
	
    protected int computeVerticalScrollOffset() {
        return mVerticalScrollOffset;
    }

    protected int computeVerticalScrollRange() {
        return mVerticalScrollRange;
    }

    protected int computeHorizontalScrollOffset() {
        return mHorizontalScrollOffset;
    }

    protected int computeHorizontalScrollRange() {
        return mHorizontalScrollRange;
    }
    
	protected void onDraw(Canvas canvas) {
		canvas.save();
		mMapView.draw(canvas);
		canvas.restore();
		super.onDraw(canvas);
	}
	
	public Matrix getPositionAndScaleMatrix() {
		return mMapView.getMatrix();
	}

	public void setPositionAndScaleMatrix(Matrix matrix) {
		if(mZoomController!=null){
			mZoomController.showZoom();
		}
		updateScrollBars(matrix);
		mMapView.setMatrix(matrix);
	}
	
	public void onTouchModeChanged(int mode) {
		mMapView.setUpdatesEnabled(mode != MultiTouchController.MODE_ZOOM && mode!= MultiTouchController.MODE_ANIMATION );
		mMapView.setAntiAlias(mode == MultiTouchController.MODE_NONE);
	}

	public void onPerformClick(PointF position) {
		if(mZoomController!=null){
			mZoomController.showZoom();
		}

		if(mLastClickPosition!=null){
			PointF p = mController.getScreenTouchPoint();
			float distance = MathUtil.distance(mLastClickPosition, p);
			mPrivateHandler.removeCallbacks(performClickRunnable);
			mLastClickPosition = null;
			if(distance <= mDblClickSlop){
				mController.doZoomAnimation(MultiTouchController.ZOOM_IN, p);
			}else{
				performClick();
			}
		}else{
			mLastClickPosition = mController.getScreenTouchPoint();
			mPrivateHandler.removeCallbacks(performClickRunnable);
			mPrivateHandler.postDelayed(performClickRunnable, ViewConfiguration.getDoubleTapTimeout());
		}
	}
	
	public void onPerformLongClick(PointF position) {
		performLongClick();
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

	public void setScheme(SchemeView scheme) {
		mScheme = scheme;
		mMapView.setScheme(scheme);
	}

	public void setSchemeSelection(ArrayList<StationView> stations,
			ArrayList<SegmentView> segments,
			ArrayList<TransferView> transfers) {
		mMapView.setSchemeSelection(stations,segments,transfers);
	}
	
	public void setZoomControls(ZoomControls zoomControls) {
		mZoomController = new ZoomController(getContext(), mController, zoomControls);
	}	
	
	public float getCenterPositionAndScale(PointF position) {
		float scale = mController.getPositionAndScale(position);
		float width = getWidth() / scale;
		float height = getHeight() / scale;
		position.offset(width/2,height/2);
		return scale;
	}
	
	public void setCenterPositionAndScale(PointF position, Float zoom) {
		mChangeCenterPoint = position;
		mChangeScale = zoom;
		invalidate();
	}
	
	public void setCenterPositionAndScale(PointF position, Float zoom, boolean animated) {
		if(!animated){
			setCenterPositionAndScale(position, zoom);
		}else{
			mController.doScrollAnimation(position);
		}
	}

	public float getScale() {
		return mController.getScale();
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			updateViewRect();
		}
	}	

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return mKeyEventController.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mKeyEventController.onKeyUp(keyCode, event);
	}
	
	public boolean onTrackballEvent(MotionEvent event) {
		return mKeyEventController.onTrackballEvent(event);
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return mController.onMultiTouchEvent(event);
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateViewRect();
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private void updateViewRect() {
		mController.setViewRect(mMapView.getContentWidth(), mMapView.getContentHeight(), new RectF(0, 0, getWidth(), getHeight()));
		if(mChangeCenterPoint!=null || mChangeScale!=null){
			float width = getWidth() / mChangeScale;
			float height = getHeight() / mChangeScale;
			mChangeCenterPoint.offset( -width/2, -height/2 );
			mController.setPositionAndScale(mChangeCenterPoint, mChangeScale);
			mChangeCenterPoint = null;
			mChangeScale = null;
		}
	}

	private void updateScrollBars(Matrix matrix) {
		matrix.getValues(values);
		float scale = values[Matrix.MSCALE_X];
		float x = -values[Matrix.MTRANS_X];
		float y = -values[Matrix.MTRANS_Y];
		float contentWidth = mScheme.width * scale;
		float contentHeight = mScheme.height * scale;
		
		mHorizontalScrollRange = (int)(contentWidth);
		mVerticalScrollRange = (int)(contentHeight);

		
		mHorizontalScrollOffset = (int)x;
		mVerticalScrollOffset = (int)y;

        awakenScrollBars();
	}

	private Runnable performClickRunnable = new Runnable() {
		public void run() {
			mLastClickPosition = null;
			performClick();
		}
	};


}