package org.ametro.ui.view;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.ametro.R;
import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.render.AsyncVectorMapRenderer;
import org.ametro.render.IVectorMapRenderer;
import org.ametro.render.RenderProgram;
import org.ametro.render.VectorMapRenderer;
import org.ametro.ui.controllers.KeyEventController;
import org.ametro.ui.controllers.MultiTouchController;
import org.ametro.ui.controllers.ZoomController;
import org.ametro.ui.controllers.MultiTouchController.MultiTouchListener;
import org.ametro.util.MathUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.ZoomControls;

public class MultiTouchMapView extends ScrollView implements MultiTouchListener {

	protected  static final String TAG = "MultiTouchMapView";

	public static final int RENDERER_TYPE_SYNC = 0;
	public static final int RENDERER_TYPE_ASYNC = 1;
	
	private static final long SCROLLBAR_TIMEOUT = 1000;

	private MultiTouchController mController;
	private ZoomController mZoomController;
	private KeyEventController mKeyEventController;

	private SchemeView mScheme;
	private RenderProgram mRenderProgram;
	private IVectorMapRenderer mMapView;
	
	private PointF mLastClickPosition;
	private float mDblClickSlop;
	
	private float[] values = new float[9];
	private boolean mAttached;
	
	private int mVerticalScrollOffset;
	private int mHorizontalScrollOffset;
	private int mVerticalScrollRange;
	private int mHorizontalScrollRange;
	
	private Handler mPrivateHandler = new Handler();
	
	private PointF mChangeCenterPoint;
	private Float mChangeScale;

	private boolean mAntiAliasingEnabled;

	private ArrayList<StationView> mStations;
	private ArrayList<SegmentView> mSegment;
	private ArrayList<TransferView> mTransfers;
	
    private String mRenderFailedErrorText;
	
	public MultiTouchMapView(Context context, SchemeView scheme, int rendererType) {
		super(context);
		
		try{
			Method method = View.class.getMethod("setScrollbarFadingEnabled",new Class[]{ boolean.class });
			method.invoke(this, new Object[]{ false });
		}catch(Exception ex){
		}
		//setScrollbarFadingEnabled(fadeScrollbars)
		
    	mRenderFailedErrorText = getContext().getString(R.string.msg_render_failed);
		
        setFocusable(true);
        setFocusableInTouchMode(true);

        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);
        
		awakeScrollBars();
		
		mScheme = scheme;
		mRenderProgram = new RenderProgram(mScheme);

		mAttached = false;
		setMapRenderer(rendererType);
		mController = new MultiTouchController(getContext(),this);
		mKeyEventController = new KeyEventController(mController);
		mDblClickSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
	}
	
	public boolean setMapRenderer(int rendererType){
		switch(rendererType){
		case RENDERER_TYPE_SYNC:
			if(mMapView==null || mMapView instanceof AsyncVectorMapRenderer){
				if(mAttached && mMapView!=null){
					mMapView.onDetachedFromWindow();
				}
				mMapView = new VectorMapRenderer(this, mScheme, mRenderProgram);
				if(mAttached){
					mMapView.setMatrix(mController.getPositionAndScale());
					mMapView.setAntiAliasEnabled(mAntiAliasingEnabled);
					mMapView.setSchemeSelection(mStations, mSegment, mTransfers);
					mMapView.onAttachedToWindow();
				}
			}
			return true;
		case RENDERER_TYPE_ASYNC:
			if(mMapView==null || mMapView instanceof VectorMapRenderer){
				if(mAttached && mMapView!=null){
					mMapView.onDetachedFromWindow();
				}
				mMapView = new AsyncVectorMapRenderer(this, mScheme, mRenderProgram);
				if(mAttached){
					mMapView.setMatrix(mController.getPositionAndScale());
					mMapView.setAntiAliasEnabled(mAntiAliasingEnabled);
					mMapView.setSchemeSelection(mStations, mSegment, mTransfers);
					mMapView.onAttachedToWindow();
				}
			}
			return true;
		}
		return false;
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
    
    protected void onAttachedToWindow() {
    	mAttached = true;
    	mMapView.onAttachedToWindow();
    	super.onAttachedToWindow();
    }
    
    protected void onDetachedFromWindow() {
    	mAttached = true;
    	mMapView.onDetachedFromWindow();
    	super.onDetachedFromWindow();
    }
    
	protected void onDraw(Canvas canvas) {
		canvas.save();
		mMapView.draw(canvas);
		if(mMapView.isRenderFailed()){
			Paint p = new Paint();
			p.setColor(Color.WHITE);
			p.setTextAlign(Align.CENTER);
			canvas.drawText(mRenderFailedErrorText, getWidth()/2, getHeight()/2, p);
		}
		canvas.restore();
		super.onDraw(canvas);
	}
	
	public Matrix getPositionAndScaleMatrix() {
		return mController.getPositionAndScale();
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
				mController.doZoomAnimation(MultiTouchController.ZOOM_IN, mController.getTouchPoint());
			}else{
				performClick();
			}
		}else{
			mLastClickPosition = mController.getScreenTouchPoint();
			mPrivateHandler.removeCallbacks(performClickRunnable);
			mPrivateHandler.postDelayed(performClickRunnable, ViewConfiguration.getDoubleTapTimeout());
		}
	}
	
	public void setZoomControlsEnabled(boolean enabled) {
		mZoomController.setEnabled(enabled);
	}

	public void setZoomUsingVolumeEnabled(boolean enabled) {
		mKeyEventController.setEnabledVolumeZoom(enabled);
	}

	public void setTrackballScrollSpeed(int trackballScrollSpeed) {
		mKeyEventController.setTrackballScrollSpeed(trackballScrollSpeed);
	}
	
	public void onPerformLongClick(PointF position) {
		performLongClick();
	}
	
	public PointF getTouchPoint() {
		return mController.getTouchPoint();
	}

	public void setAntiAliasingDisableOnScroll(boolean disableOnScroll) {
		//mMapView.setAntiAliasDisabledOnChanges(disableOnScroll);
	}

	public void setAntiAliasingEnabled(boolean enabled) {
		mAntiAliasingEnabled = enabled;
		mMapView.setAntiAliasEnabled(enabled);
	}

	public void setScheme(SchemeView scheme) {
		mScheme = scheme;
		mRenderProgram = new RenderProgram(mScheme);
		mMapView.setScheme(mScheme, mRenderProgram);
	}

	public void setSchemeSelection(ArrayList<StationView> stations,
			ArrayList<SegmentView> segments,
			ArrayList<TransferView> transfers) {
		mStations = stations;
		mSegment = segments;
		mTransfers = transfers;
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
	
	public void setCenterPositionAndScale(PointF position, Float zoom, boolean animated) {
		if(!animated){
			mChangeCenterPoint = position;
			mChangeScale = zoom;
			invalidate();
		}else{
			mController.doScrollAndZoomAnimation(position, zoom);
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
		mController.setViewRect(mScheme.width, mScheme.height, new RectF(0, 0, getWidth(), getHeight()));
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

		//Log.d(TAG,"Scroll ranges: H=" + mHorizontalScrollOffset + "/" + mHorizontalScrollRange + ", V=" + mVerticalScrollOffset + "/" + mVerticalScrollRange);
		
		
        awakeScrollBars();
	}
	
	private void awakeScrollBars(){
		//Log.d(TAG,"Awake scrollbars.");
		setVerticalScrollBarEnabled(true);
		setHorizontalScrollBarEnabled(true);
        mPrivateHandler.removeCallbacks(hideScrollbarsRunnable);
        mPrivateHandler.postDelayed(hideScrollbarsRunnable, SCROLLBAR_TIMEOUT);
		invalidate();
	}
	
	private void fadeScrollBars(){
		//Log.d(TAG,"Fade scrollbars.");
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		invalidate();
	}
	
	private Runnable performClickRunnable = new Runnable() {
		public void run() {
			mLastClickPosition = null;
			performClick();
		}
	};
	
	private Runnable hideScrollbarsRunnable = new Runnable() {
		public void run() {
			fadeScrollBars();
		}
	};

}