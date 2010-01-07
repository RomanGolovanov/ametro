package com.ametro.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.ametro.MapSettings;

public class TileImageView extends ScrollView {

	public interface IDataProvider {
		Point getContentSize();
		Bitmap getTile(Rect region);
		Bitmap getLoadingTile();

	}

	private class  RendererThread extends Thread{

		private boolean mWork = true;
		private boolean mUpdatePending = false;

		public void shutdownRendering(){
			synchronized (mRenderThread) {
				mWork = false;
				mRenderThread.notify();
			}
		}


		public void invalidateTiles(){
			synchronized (mRenderThread) {
				mUpdatePending = true;
				mRenderThread.notify();
			}
		}

		public void run() {
			while(mWork){
				try {
					if(mWork){
						Rect tiles = getVisibleTiles();
						recycleTiles(new Rect(tiles));
						invalidateTiles(tiles);
						synchronized (this) {
							if(!mUpdatePending){
								this.wait();
							}else{
								this.wait(10);
							}
						}

					}
				} catch (Exception e) {
					Log.e("TileImageView", "Exit render thread due error", e);
					return;
				}
			}
		}

		private void recycleTiles(Rect tiles) {
			int left = tiles.left - TILE_RECYCLE_BORDER - TILE_PRELOAD_BORDER;
			int top = tiles.top - TILE_RECYCLE_BORDER - TILE_PRELOAD_BORDER;
			int right = tiles.right + TILE_RECYCLE_BORDER + TILE_PRELOAD_BORDER;
			int bottom = tiles.bottom + TILE_RECYCLE_BORDER + TILE_PRELOAD_BORDER;
			boolean cleaned = false;
			for(int i = 0; i < mTileCountWidth; i++){
				for(int j = 0; j < mTileCountHeight; j++){
					boolean visible = (i >= left && i <= right && j >= top && j <= bottom);
					if( !visible && mTiles[i][j]!=null ){
						mTiles[i][j].recycle();
						mTiles[i][j] = null;
						cleaned = true;
					}
				}
			}
			if(cleaned){
				//Runtime.getRuntime().gc();
			}
		}

		private boolean invalidateTiles(Rect tiles) throws InterruptedException {
			int left = Math.max(tiles.left - TILE_PRELOAD_BORDER, 0);
			int top = Math.max(tiles.top - TILE_PRELOAD_BORDER, 0);
			int right = Math.min(tiles.right + TILE_PRELOAD_BORDER, mTileCountWidth-1);
			int bottom = Math.min(tiles.bottom + TILE_PRELOAD_BORDER, mTileCountHeight-1);
			for(int i = left; i<= right;i++){
				for(int j = top; j <= bottom; j++){
					Bitmap tile = mTiles[i][j];
					if(tile == null){
						mTiles[i][j] = mDataProvider.getTile(getTileContentPosition(i,j));
					}
				}
			}
			TileImageView.this.postInvalidate();
			return true;
		}
	};

	private boolean mInitialized = false;

	private float mScale = 1.0f;

	private final int TILE_RECYCLE_BORDER = 2;
	private final int TILE_PRELOAD_BORDER = 0;


	private IDataProvider mDataProvider;

	private Bitmap mTileLoading;
	private Bitmap[][] mTiles;

	private int mTileCountWidth;
	private int mTileCountHeight;

	private int mContentWidth;
	private int mContentHeight;

	//public int mTileScrollX = 0;
	//public int mTileScrollY = 0;

	private Rect mTileRect;
	private Context mContext;

	private RendererThread mRenderThread;

	private boolean mNeedScrollToCenter;

	private void prepareRenderer(){
		Point contentSize = mDataProvider.getContentSize();

		mContentWidth = contentSize.x;
		mContentHeight = contentSize.y;

		mTileRect = new Rect(0,0,MapSettings.TILE_WIDTH,MapSettings.TILE_HEIGHT);

		mTileCountWidth = getCeil(mContentWidth, MapSettings.TILE_WIDTH);
		mTileCountHeight = getCeil(mContentHeight, MapSettings.TILE_HEIGHT);

		mTileLoading = mDataProvider.getLoadingTile();
		mTiles = new Bitmap[mTileCountWidth][mTileCountHeight];
		for(int i = 0; i < mTileCountWidth; i++){
			for(int j = 0; j < mTileCountHeight; j++){
				mTiles[i][j] = null;
			}
		}

		mRenderThread = new RendererThread();
		mRenderThread.start();

		mRenderThread.invalidateTiles();

		mInitialized = true;
	}

	private void cleanupRenderer() {
		mInitialized = false;
		mDataProvider = null;
		mContentWidth = 0;
		mContentHeight = 0;
		mRenderThread.shutdownRendering();
		mRenderThread = null;
		for(int i = 0; i < mTileCountWidth; i++){
			for(int j = 0; j < mTileCountHeight; j++){
				if(mTiles[i][j]!=null && mTiles[i][j]!=mTileLoading ){
					mTiles[i][j].recycle();
					mTiles[i][j]= null;
				}
			}
		}	
		mTileLoading.recycle();
		mTileRect = null;
	}


	private int getCeil(int value, int divider){
		int mod = value % divider;
		return value / divider + ( mod!=0 ? 1 : 0 ) ;
	}

	private void initializeControls() {
		setVerticalScrollBarEnabled(true);
		setHorizontalScrollBarEnabled(true);
		
		mScroller = new Scroller(mContext);
		if(mInitialized){
			cleanupRenderer();			
		}
		prepareRenderer();
	}

	@Override
	protected int computeVerticalScrollOffset() {
		return  mInitialized ? mScrollY : 0;
	}
	
	@Override
	protected int computeVerticalScrollRange() {
		return mInitialized ? mContentHeight : 0;
	}
	
	@Override
	protected int computeHorizontalScrollOffset() {
		return  mInitialized ? mScrollX : 0;
	}
	
	@Override
	protected int computeHorizontalScrollRange() {
		return mInitialized ? mContentWidth : 0;
	}
		
    /*
     * Return the width of the view where the content of WebView should render
     * to.
     */
    private int getViewWidth() {
        if (!isVerticalScrollBarEnabled() ) {
            return getWidth();
        } else {
            return Math.max(0, getWidth() - getVerticalScrollbarWidth());
        }
    }

    /*
     * Return the height of the view where the content of WebView should render
     * to.
     */
    private int getViewHeight() {
        if (!isHorizontalScrollBarEnabled() ) {
            return getHeight();
        } else {
            return Math.max(0, getHeight() - getHorizontalScrollbarHeight());
        }
    }


	private Rect getVisibleTiles(){
		int left = Math.max(mScrollX / MapSettings.TILE_WIDTH, 0);
		int top = Math.max(mScrollY / MapSettings.TILE_HEIGHT, 0);
		int right = Math.min(getCeil(mScrollX+(int)(getViewWidth()/mScale),MapSettings. TILE_WIDTH), mTileCountWidth-1);
		int bottom = Math.min(getCeil(mScrollY+(int)(getViewHeight()/mScale), MapSettings.TILE_HEIGHT), mTileCountHeight-1);
		return new Rect(left,top,right,bottom);
	}

	private Rect getTileScreenPosition(int tileRow, int tileColumn){
		int dx = 0;
		int dy = 0;
		if(mContentWidth<getViewWidth()) {
			dx = (getViewWidth() - mContentWidth) / 2;
		}
		if(mContentHeight<getViewHeight()){
			dy = (getViewHeight() - mContentHeight) / 2;
		}
		int left = dx + tileRow * (int)(MapSettings.TILE_WIDTH*mScale) - (int)(mScrollX*mScale);
		int top = dy + tileColumn * (int)(MapSettings.TILE_HEIGHT*mScale) - (int)(mScrollY*mScale);
		int right = left + (int)(MapSettings.TILE_WIDTH*mScale);
		int bottom = top + (int)(MapSettings.TILE_HEIGHT*mScale);
		return new Rect(left,top,right,bottom);
	}

	private Rect getTileContentPosition(int tileRow, int tileColumn) {
		int left = tileRow * MapSettings.TILE_WIDTH;
		int top = tileColumn * MapSettings.TILE_HEIGHT;
		int right = left + MapSettings.TILE_WIDTH;
		int bottom = top + MapSettings.TILE_HEIGHT;
		return new Rect(left,top,right,bottom);
	}


	@Override
	protected void onDraw(Canvas canvas) { 
		if(mInitialized){
			Rect viewClip = new Rect(0,0,getViewWidth(), getViewHeight());
			int sc = canvas.save();
			canvas.clipRect(viewClip);
			
			if(mNeedScrollToCenter){
				mScrollX = mScrollToX - (getViewWidth()/2);
				mScrollY = mScrollToY - (getViewHeight()/2);
				invalidateScroll();
				mNeedScrollToCenter = false;
			}
			boolean needToInvalidateTiles = false;
			final int contentWidth = mContentWidth;
			final int contentHeight = mContentHeight;
			Rect tiles = getVisibleTiles();
			for(int row = tiles.left; row <= tiles.right; row++){
				for(int col = tiles.top; col <= tiles.bottom; col++){
					final int bottom = (col+1)*MapSettings.TILE_HEIGHT;
					final int right = (row+1)*MapSettings.TILE_WIDTH;	
					if( bottom <= contentHeight && right <= contentWidth ){
						needToInvalidateTiles |= drawEntireTile(canvas, col, row);
					}
					else{
						needToInvalidateTiles |= drawPartialTile(canvas, col, row);
					}
				}
			}
			if(needToInvalidateTiles){
				mRenderThread.invalidateTiles();
			}
			canvas.restoreToCount(sc);
		}
		super.onDraw(canvas);
	}

	private boolean drawPartialTile(Canvas canvas,int col, int row) {
		final int height = Math.min( MapSettings.TILE_HEIGHT, mContentHeight - col*MapSettings.TILE_HEIGHT);
		final int width = Math.min( MapSettings.TILE_WIDTH, mContentWidth - row*MapSettings.TILE_WIDTH);
		Rect dst = getTileScreenPosition(row, col);
		dst = new Rect(dst.left, dst.top, dst.left + width, dst.top + height);
		Rect src = new Rect(0,0,width,height);
		Bitmap tile = mTiles[row][col];
		if(tile!=null){
			canvas.drawBitmap(tile,src,dst,null);
		}else{
			canvas.drawBitmap(mTileLoading,src,dst,null);
			return true;
		}
		return false;
	}
	
	private boolean drawEntireTile(Canvas canvas, int col, int row) {
		Rect dst = getTileScreenPosition(row, col);
		Bitmap tile = mTiles[row][col];
		if(tile!=null){
			canvas.drawBitmap(tile,mTileRect,dst,null);
		}else{
			canvas.drawBitmap(mTileLoading,mTileRect,dst,null);
			return true;
		}
		return false;
	}

	public TileImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initializeControls();
	}

	public TileImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public TileImageView(Context context) {
		super(context);
		mContext = context;
	}

	public TileImageView(Context context, IDataProvider dataProvider) {
		super(context);
		mContext = context;
		setDataProvider(dataProvider);
	}

	public IDataProvider getDataProvider(){
		return mDataProvider;
	}

	public void setDataProvider(IDataProvider dataProvider){
		mDataProvider = dataProvider;
		initializeControls();
	}

	public int getContentWidth() {
		return mContentWidth;
	}

	public int getContentHeight() {
		return mContentHeight;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mScrollX = mScroller.getCurrX();
			mScrollY = mScroller.getCurrY();
			postInvalidate();
			mRenderThread.invalidateTiles();
		}

		//scrollTo(0, 0);
	}

	public void internalScroll(int dx, int dy){
		int x = mScrollX+dx;
		int y = mScrollY+dy;
		mScrollX = x;
		mScrollY = y;
		invalidateScroll();
		postInvalidate();
		mRenderThread.invalidateTiles();

		//scrollTo(0, 0);
	}

	public void setScrollCenter(int x, int y){
		mScrollToX = x;
		mScrollToY = y;
		mNeedScrollToCenter = true;
		postInvalidate();
	}

	private void invalidateScroll() {
		int maxX = Math.max(getContentWidth() - getViewWidth(), 0);
		int maxY = Math.max(getContentHeight() - getViewHeight(), 0);
		mScrollX = Math.max(0, Math.min(maxX, mScrollX));
		mScrollY = Math.max(0,Math.min(maxY, mScrollY));
	} 

	public Point getScrollCenter(){
		return new Point(mScrollX + (getViewWidth()/2) ,mScrollY + (getViewHeight()/2));
	}

	// adjustable parameters
	private static final boolean SNAP_ENABLED = false;
	private static final float MAX_SLOPE_FOR_DIAG = 1.5f;
	private static final int MIN_BREAK_SNAP_CROSS_DISTANCE = 20; //20
	private static final int MIN_FLING_TIME = 250; //250

	private int mScrollX = 0;
	private int mScrollY = 0;
	
	private int mScrollToX = 0;
	private int mScrollToY = 0;

	private float mLastTouchX;
	private float mLastTouchY;
	private long mLastTouchTime;
	private int mMinLockSnapReverseDistance;

	private int mTouchMode = TOUCH_DONE_MODE;
	private static final int TOUCH_INIT_MODE = 1;
	private static final int TOUCH_DRAG_START_MODE = 2;
	private static final int TOUCH_DRAG_MODE = 3;
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
		if (x > getViewWidth() - 1) {
			x = getViewWidth() - 1;
		}
		if (y > getViewHeight() - 1) {
			y = getViewHeight() - 1;
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
			mSnapScrollMode = SNAP_NONE;
			mVelocityTracker = VelocityTracker.obtain();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchMode == TOUCH_DONE_MODE) {
				// no dragging during scroll zoom animation
				break;
			}
			mVelocityTracker.addMovement(event);

			if (mTouchMode != TOUCH_DRAG_MODE) {

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
					//scrollBy(deltaX, 0);
					internalScroll(deltaX,0);
					mLastTouchX = x;
				} else if (mSnapScrollMode == SNAP_Y
						|| mSnapScrollMode == SNAP_Y_LOCK) {
					//scrollBy(0, deltaY);
					internalScroll(0,deltaY);
					mLastTouchY = y;
				} else {
					//scrollBy(deltaX, deltaY);
					internalScroll(deltaX,deltaY);
					mLastTouchX = x;
					mLastTouchY = y;
				}
				mLastTouchTime = eventTime;
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
		int maxX = Math.max(getContentWidth() - getViewWidth(), 0);
		int maxY = Math.max(getContentHeight() - getViewHeight(), 0);

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
			vx = vx / 2;// * 3 / 4;
			vy = vy / 2;// * 3 / 4;
		}

		mScroller.fling(mScrollX, mScrollY, -vx, -vy, 0, maxX, 0, maxY);
		//final int time = mScroller.getDuration();
		postInvalidate();
	}

	private void doShortPress() {
	}

	// Expects x in view coordinates
	private int pinLocX(int x) {
		return pinLoc(x, getViewWidth(), getContentWidth());
	}

	// Expects y in view coordinates
	private int pinLocY(int y) {
		return pinLoc(y, getViewHeight(), getContentHeight() ) ;
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


}
