package com.ametro;

import com.ametro.model.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

public class MapImageView extends FrameLayout {

	private class MapImage extends ImageView {

		private Model mModel;
		private Bitmap mBuffer;
		private boolean mIsBuffered;
		
		public MapImage(Context context, Model model) {
			super(context);
			mModel = model;
			mBuffer = null;
			mIsBuffered = false;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int width = 0;
			int height = 0;
			if(mModel!=null){
				//DisplayMetrics
				
				width = mModel.getWidth();
				height = mModel.getHeight();
			}
			setMeasuredDimension(width,height);
		}	

		@Override
		protected void onDraw(Canvas canvas) {
			if(mModel!=null){
				if(!mIsBuffered){
					fillBuffer();
				}
				canvas.drawBitmap(mBuffer, 0, 0, null);
				canvas.save();
			}else{
				super.onDraw(canvas);
			}
		}

		private void fillBuffer() {
			if(mBuffer!=null){
				mBuffer.recycle();
			}
			mBuffer = Bitmap.createBitmap(mModel.getWidth(), mModel.getHeight(), Config.RGB_565 );
			Canvas bufferCanvas = new Canvas(mBuffer);
			mModel.render(bufferCanvas);
			mIsBuffered = true;
		}

		public void setModel(Model model) {
			mModel = model;
			mIsBuffered = false;
			mBuffer.recycle();
			mBuffer = null;
		}

		public void preRender() {
			fillBuffer();
		}
	}

	public MapImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initControls(context,null);
	}

	public MapImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initControls(context,null);
	}

	private Model mModel;
	
	public MapImageView(Context context, Model model) {
		super(context);
		mModel = model;
		initControls(context, model);
	}

	private MapImage mMapImage;

	private void initControls(Context context, Model model){
		if(!isInitialized){
			mScroller = new Scroller(context);
			isInitialized = true;
			mMapImage = new MapImage(context,model);
			addView(mMapImage);
			mMapImage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mScrollX = mScroller.getCurrX();
			mScrollY = mScroller.getCurrY();
			//mMapImage.scrollTo(mScrollX, mScrollY);
			//mMapImage.postInvalidate();  // So we draw again
			scrollTo(mScrollX, mScrollY);
			postInvalidate();  // So we draw again
		}
		//this.scrollTo(0,0);
	}


	private boolean isInitialized = false;
	// adjustable parameters
	private static final boolean SNAP_ENABLED = false;
	private static final float MAX_SLOPE_FOR_DIAG = 1.5f;
	private static final int MIN_BREAK_SNAP_CROSS_DISTANCE = 20; //20
	private static final int MIN_FLING_TIME = 250; //250
	private static final int MIN_DRAG_TIME = 300; 

	private int mScrollX;
	private int mScrollY; 

	private float mLastTouchX;
	private float mLastTouchY;
	private long mLastTouchTime;
	private long mStartTouchTime;
	private int mMinLockSnapReverseDistance;

	private int mTouchMode = TOUCH_DONE_MODE;
	private static final int TOUCH_INIT_MODE = 1;
	private static final int TOUCH_DRAG_START_MODE = 2;
	private static final int TOUCH_DRAG_MODE = 3;
	private static final int TOUCH_DRAG_CONTINUOUS_MODE = 4;
	private static final int TOUCH_DONE_MODE = 7;

	private int mSnapScrollMode = SNAP_NONE;
	private static final int SNAP_NONE = 1;
	private static final int SNAP_X = 2;
	private static final int SNAP_Y = 3;
	private static final int SNAP_X_LOCK = 4;
	private static final int SNAP_Y_LOCK = 5;
	private boolean mSnapPositive;

	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		long eventTime = event.getEventTime();
		if (x > getWidth() - 1) {
			x = getWidth() - 1;
		}
		if (y > getHeight() - 1) {
			y = getHeight() - 1;
		}

		int deltaX = (int) (mLastTouchX - x);
		int deltaY = (int) (mLastTouchY - y);

		switch(action){
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
				mTouchMode = TOUCH_DRAG_START_MODE;
			}else{
				mTouchMode = TOUCH_INIT_MODE;				
			}
			mTouchMode = TOUCH_INIT_MODE;
			mLastTouchX = x;
			mLastTouchY = y;
			mLastTouchTime = eventTime;
			mStartTouchTime = eventTime;
			mSnapScrollMode = SNAP_NONE;
			mVelocityTracker = VelocityTracker.obtain();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchMode == TOUCH_DONE_MODE) {
				// no dragging during scroll zoom animation
				break;
			}
			mVelocityTracker.addMovement(event);

			if (mTouchMode != TOUCH_DRAG_MODE && mTouchMode != TOUCH_DRAG_CONTINUOUS_MODE) {

				// if it starts nearly horizontal or vertical, enforce it
				if(SNAP_ENABLED){
					int ax = Math.abs(deltaX);
					int ay = Math.abs(deltaY);
					if (ax > MAX_SLOPE_FOR_DIAG * ay) {
						mSnapScrollMode = SNAP_X;
						mSnapPositive = deltaX > 0;
					} else if (ay > MAX_SLOPE_FOR_DIAG * ax) {
						mSnapScrollMode = SNAP_Y;
						mSnapPositive = deltaY > 0;
					}
				}
				mTouchMode = TOUCH_DRAG_MODE;

			}

			// do pan
			int newScrollX = pinLocX(mScrollX + deltaX);
			deltaX = newScrollX - mScrollX;
			int newScrollY = pinLocY(mScrollY + deltaY);
			deltaY = newScrollY - mScrollY;
			boolean done = false;
			if (deltaX == 0 && deltaY == 0) {
				done = true;
			} else {
				if (mSnapScrollMode == SNAP_X || mSnapScrollMode == SNAP_Y) {
					int ax = Math.abs(deltaX);
					int ay = Math.abs(deltaY);
					if (mSnapScrollMode == SNAP_X) {
						// radical change means getting out of snap mode
						if (ay > MAX_SLOPE_FOR_DIAG * ax
								&& ay > MIN_BREAK_SNAP_CROSS_DISTANCE) {
							mSnapScrollMode = SNAP_NONE;
						}
						// reverse direction means lock in the snap mode
						if ((ax > MAX_SLOPE_FOR_DIAG * ay) &&
								((mSnapPositive && 
										deltaX < -mMinLockSnapReverseDistance)
										|| (!mSnapPositive &&
												deltaX > mMinLockSnapReverseDistance))) {
							mSnapScrollMode = SNAP_X_LOCK;
						}
					} else {
						// radical change means getting out of snap mode
						if ((ax > MAX_SLOPE_FOR_DIAG * ay)
								&& ax > MIN_BREAK_SNAP_CROSS_DISTANCE) {
							mSnapScrollMode = SNAP_NONE;
						}
						// reverse direction means lock in the snap mode
						if ((ay > MAX_SLOPE_FOR_DIAG * ax) &&
								((mSnapPositive && 
										deltaY < -mMinLockSnapReverseDistance)
										|| (!mSnapPositive && 
												deltaY > mMinLockSnapReverseDistance))) {
							mSnapScrollMode = SNAP_Y_LOCK;
						}
					}
				}

				if (mSnapScrollMode == SNAP_X
						|| mSnapScrollMode == SNAP_X_LOCK) {
					scrollBy(deltaX, 0);
					mLastTouchX = x;
				} else if (mSnapScrollMode == SNAP_Y
						|| mSnapScrollMode == SNAP_Y_LOCK) {
					scrollBy(0, deltaY);
					mLastTouchY = y;
				} else {
					scrollBy(deltaX, deltaY);
					mLastTouchX = x;
					mLastTouchY = y;
				}
				mLastTouchTime = eventTime;
				
				if(mTouchMode == TOUCH_DRAG_MODE && ((mStartTouchTime + MIN_DRAG_TIME ) > eventTime)  ){
					mTouchMode = TOUCH_DRAG_CONTINUOUS_MODE;
				}

				if(mTouchMode == TOUCH_DRAG_CONTINUOUS_MODE){
					scrollTo(mScrollX, mScrollY);
				}
				
			}
			if (done) {
				// return false to indicate that we can't pan out of the
				// view space
				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
			switch (mTouchMode) {
			case TOUCH_INIT_MODE: // tap
				mTouchMode = TOUCH_DONE_MODE;
				doShortPress();
				break;
			case TOUCH_DRAG_MODE:
			case TOUCH_DRAG_CONTINUOUS_MODE:
				// if the user waits a while w/o moving before the
				// up, we don't want to do a fling
				if (eventTime - mLastTouchTime <= MIN_FLING_TIME) {
					mVelocityTracker.addMovement(event);
					doFling();
					break;
				}
				break;
			case TOUCH_DRAG_START_MODE:
			case TOUCH_DONE_MODE:
				// do nothing
				break;
			}
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			break;
		case MotionEvent.ACTION_CANCEL: 
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchMode = TOUCH_DONE_MODE;
			break;
		}
		return true;
	}

	private void doFling() {
		if (mVelocityTracker == null) {
			return;
		}
		int maxX = Math.max(mModel.getWidth() - getWidth(), 0);
		int maxY = Math.max(mModel.getHeight() - getHeight(), 0);

		mVelocityTracker.computeCurrentVelocity(1000);
		int vx = (int) mVelocityTracker.getXVelocity();
		int vy = (int) mVelocityTracker.getYVelocity();

		if(SNAP_ENABLED){
			if (mSnapScrollMode != SNAP_NONE) {
				if (mSnapScrollMode == SNAP_X || mSnapScrollMode == SNAP_X_LOCK) {
					vy = 0;
				} else {
					vx = 0;
				}
			}
		}

		if (true /* EMG release: make our fling more like Maps' */) {
			// maps cuts their velocity in half
			vx = vx * 3 / 4;
			vy = vy * 3 / 4;
		}

		mScroller.fling(mScrollX, mScrollY, -vx, -vy, 0, maxX, 0, maxY);
		//final int time = mScroller.getDuration();
		invalidate();
	}

	private void doShortPress() {
		// TODO Auto-generated method stub

	}

	// Expects x in view coordinates
	private int pinLocX(int x) {
		return pinLoc(x, getWidth(), computeHorizontalScrollRange());
	}

	// Expects y in view coordinates
	private int pinLocY(int y) {
		return pinLoc(y, getHeight(), computeVerticalScrollRange());
	}

	private static int pinLoc(int x, int viewMax, int docMax) {
		if (docMax < viewMax) {   // the doc has room on the sides for "blank"
			x = 0;
		} else if (x < 0) {
			x = 0;
		} else if (x + viewMax > docMax) {
			x = docMax - viewMax;
		}
		return x;
	}

	public void setModel(Model model) {
		mModel = model;
		mMapImage.setModel(model);
	}

	public void preRender() {
		mMapImage.preRender();
	}

}