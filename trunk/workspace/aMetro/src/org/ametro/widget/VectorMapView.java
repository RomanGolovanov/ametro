/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.widget;

import org.ametro.model.Model;
import org.ametro.render.RenderProgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.ScrollView;
import android.widget.Scroller;

public class VectorMapView extends ScrollView {

	private Model mModel;
	private RenderProgram mRenderProgram;
	private Context mContext;

	public VectorMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initializeControls();
	}

	public VectorMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initializeControls();
	}

	public VectorMapView(Context context) {
		super(context);
		mContext = context;
		initializeControls();
	}

	private Bitmap mCache;
	private Bitmap mCacheBuffer;
	private Rect mCacheViewport;
	private float mCacheScale;


	protected void onDraw(Canvas canvas) {
		if(mInitialized){

			final int left = mScrollX;
			final int top = mScrollY;
			final int right = left + (int)( getWidth() / mScale );
			final int bottom = top + (int)( getHeight() / mScale );
			Rect viewport = new Rect(left,top,right, bottom);


			invalidateCache(viewport,false);

			if(mCacheViewport.equals(viewport)){
				canvas.drawBitmap(mCache, 0, 0, null);
			}else{
				if(!Rect.intersects(mCacheViewport, viewport)){
					invalidateCache(viewport,true);
					canvas.drawBitmap(mCache, 0, 0, null);
				}else{

//					final Rect crect = mCacheViewport;
//					Rect rect = new Rect(mCacheViewport);
//					rect.intersect(viewport);
//					Rect dst = new Rect(rect); // control canvas position
//					dst.offsetTo(rect.left - viewport.left , rect.top - viewport.top );
//					Rect src = new Rect(rect); // cache canvas position
//					src.offsetTo(rect.left - crect.left, rect.top - crect.top);
//					convertToScreen(src,mScale);
//					convertToScreen(dst,mScale);
//					canvas.drawBitmap(mCache,src,dst,null);					
					
					updateCache(viewport);
					canvas.drawBitmap(mCache, 0, 0, null);
				}
			}
		}
		super.onDraw(canvas);
	}

	private static Rect convertToScreen(Rect rect, float scale)
	{
		rect.left = Math.round(rect.left * scale);
		rect.top = Math.round(rect.top * scale);
		rect.right = Math.round(rect.right * scale);
		rect.bottom = Math.round(rect.bottom * scale);
		return rect;
	}
	
	private void updateCache(Rect viewport) {
		final Rect crect = mCacheViewport;
		
		Rect rect = new Rect(mCacheViewport);
		rect.intersect(viewport);

		Rect dst = new Rect(rect); // control canvas position
		dst.offsetTo(rect.left - viewport.left , rect.top - viewport.top );

		
		Rect src = new Rect(rect); // cache canvas position
		src.offsetTo(rect.left - crect.left, rect.top - crect.top);

		//canvas.drawBitmap(mCache, src, dst, null);
		
		Rect verticalSpan = new Rect(viewport);
		Rect horizontalSpan = new Rect(viewport);

		if(viewport.right == rect.right && viewport.bottom == rect.bottom){
			horizontalSpan.bottom = rect.top;
			verticalSpan.right = rect.left;
		}else if(viewport.right == rect.right && viewport.top == rect.top){
			horizontalSpan.top = rect.bottom;
			verticalSpan.right = rect.left;
		}else if(viewport.left == rect.left && viewport.bottom == rect.bottom){
			horizontalSpan.bottom = rect.top;
			verticalSpan.left = rect.right;
		}else if(viewport.left == rect.left && viewport.top == rect.top){
			horizontalSpan.top = rect.bottom;
			verticalSpan.left = rect.right;
		}else{
			throw new RuntimeException("Invalid viewport splitting algorithm");
		}

		int hx = horizontalSpan.left - viewport.left; 
		int hy = horizontalSpan.top - viewport.top;
		Rect horizontalClip = new Rect(hx,hy,hx+horizontalSpan.width(), hy+horizontalSpan.height());
		convertToScreen(horizontalClip,mScale);

		int vx = verticalSpan.left - viewport.left; 
		int vy = verticalSpan.top - viewport.top;
		Rect verticalClip = new Rect(vx, vy, vx+verticalSpan.width(), vy+verticalSpan.height());
		convertToScreen(verticalClip, mScale);

		Canvas cacheCanvas = new Canvas(mCacheBuffer);
		cacheCanvas.drawColor(Color.MAGENTA);

		// show previous map block
		convertToScreen(src,mScale);
		convertToScreen(dst,mScale);
		cacheCanvas.drawBitmap(mCache, src, dst, null);
		// scale to current coordinated
		cacheCanvas.scale(mScale,mScale);
		// draw horizontal line
		if(!horizontalSpan.isEmpty()){
			cacheCanvas.save();
			cacheCanvas.clipRect(horizontalClip,Op.REPLACE);
			cacheCanvas.scale(mScale, mScale);
			cacheCanvas.translate(hx-horizontalSpan.left, hy-horizontalSpan.top);
			mRenderProgram.invalidateVisible(horizontalSpan);
			mRenderProgram.draw(cacheCanvas);
			cacheCanvas.restore();
		}
		// draw vertical line
		if(!verticalSpan.isEmpty()){
			cacheCanvas.save();
			cacheCanvas.clipRect(verticalClip,Op.REPLACE);
			cacheCanvas.scale(mScale, mScale);
			cacheCanvas.translate(vx-verticalSpan.left, vy-verticalSpan.top);
			mRenderProgram.invalidateVisible(verticalSpan);
			mRenderProgram.draw(cacheCanvas);
			cacheCanvas.restore();
		}
		Bitmap swap = mCache;
		mCache = mCacheBuffer;
		mCacheBuffer = swap;
		mCacheViewport = viewport;
		mCacheScale = mScale;
	}

	private void invalidateCache(Rect viewport, boolean force) {
		if(mCache==null || mCacheScale!=mScale || force){
			if(mCacheScale!=mScale){
				destroyCache();
			}
			if(mCache==null){
				mCache = Bitmap.createBitmap(getWidth(), getHeight(), Config.RGB_565);
				mCacheBuffer = Bitmap.createBitmap(getWidth(), getHeight(), Config.RGB_565);
			}
			Canvas cacheCanvas = new Canvas(mCache);
			cacheCanvas.scale(mScale, mScale);
			cacheCanvas.translate(-viewport.left, -viewport.top);
			mRenderProgram.invalidateVisible(viewport);
			mRenderProgram.draw(cacheCanvas);
			mCacheViewport = viewport;
			mCacheScale = mScale;		
		}
	}

	private void destroyCache() {
		if(mCache!=null){
			mCache.recycle();
			mCache = null;
			mCacheBuffer.recycle();
			mCacheBuffer = null;
		}
	}

	public void setModel(Model model) {
		if(model!=null){
			mModel = model;
			mRenderProgram = new RenderProgram(model);
			//mRenderProgram.setRenderFilter(RenderProgram.ONLY_TRANSPORT);
			mInitialized = true;
			calculateDimensions();
		}else{
			mInitialized = false;
			mRenderProgram = null;
			mModel = null;
		}
	}	

	protected int computeVerticalScrollOffset() {
		return  mInitialized ? mScrollY : 0;
	}

	protected int computeVerticalScrollRange() {
		return mInitialized ? mContentHeight : 0;
	}

	protected int computeHorizontalScrollOffset() {
		return  mInitialized ? mScrollX : 0;
	}

	protected int computeHorizontalScrollRange() {
		return mInitialized ? mContentWidth : 0;
	}


	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mScrollX = mScroller.getCurrX();
			mScrollY = mScroller.getCurrY();
			postInvalidate();
		}
	}

	public void setScrollCenter(int x, int y){
		setScrollCenter(new Point(x,y));
	}

	public void setScrollCenter(Point p) {
		mScrollX = (int)(p.x*mScale);
		mScrollY = (int)(p.y*mScale);
		invalidateScroll();
		postInvalidate();
	}

	public Point getScrollCenter(){
		final int screenX = mScrollX;
		final int screenY = mScrollY;
		return new Point( (int)(screenX/mScale), (int)(screenY/mScale) );
	}

	public void setScale(float scale){
		Point p = getScrollCenter();
		mScale = scale;
		calculateDimensions();
		setScrollCenter(p);
		postInvalidate();
	}

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
		int maxX = Math.max(mContentWidth - getWidth(), 0);
		int maxY = Math.max(mContentHeight - getHeight(), 0);

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
		vx = vx / 2;
		vy = vy / 2;

		//mRenderProgram.setRenderFilter(RenderProgram.ONLY_TRANSPORT);
		mScroller.fling(mScrollX, mScrollY, -vx, -vy, 0, maxX, 0, maxY);
		postInvalidate();
	}

	private void doShortPress() {
	}

	private void calculateDimensions(){
		mContentWidth = (int)(mModel.getWidth() * mScale);
		mContentHeight = (int)(mModel.getHeight() * mScale);
	}

	private void initializeControls() {
		setVerticalScrollBarEnabled(true);
		setHorizontalScrollBarEnabled(true);
		mScroller = new Scroller(mContext);
	}

	private void internalScroll(int dx, int dy){
		int x = mScrollX+dx;
		int y = mScrollY+dy;
		mScrollX = x;
		mScrollY = y;
		invalidateScroll();
		postInvalidate();
	}


	private void invalidateScroll() {
		int maxX = Math.max(mContentWidth - (int)(getWidth()*mScale), 0);
		int maxY = Math.max(mContentHeight - (int)(getHeight()*mScale), 0);
		mScrollX = Math.max(0, Math.min(maxX, mScrollX));
		mScrollY = Math.max(0,Math.min(maxY, mScrollY));
	} 	

	// Expects x in view coordinates
	private int pinLocX(int x) {
		return pinLoc(x, getViewWidth(), mContentWidth);
	}

	// Expects y in view coordinates
	private int pinLocY(int y) {
		return pinLoc(y, getViewHeight(), mContentHeight) ;
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

	private int getViewWidth() {
		if (!isVerticalScrollBarEnabled() ) {
			return getWidth();
		} else {
			return Math.max(0, getWidth() - getVerticalScrollbarWidth());
		}
	}

	private int getViewHeight() {
		if (!isHorizontalScrollBarEnabled() ) {
			return getHeight();
		} else {
			return Math.max(0, getHeight() - getHorizontalScrollbarHeight());
		}
	}

	private int mContentWidth;
	private int mContentHeight;
	private boolean mInitialized;
	private float mScale = 1.0f;


	// adjustable parameters
	private static final boolean SNAP_ENABLED = false;
	private static final float MAX_SLOPE_FOR_DIAG = 1.5f;
	private static final int MIN_BREAK_SNAP_CROSS_DISTANCE = 20; //20
	private static final int MIN_FLING_TIME = 250; //250

	private int mScrollX = 400;
	private int mScrollY = 400;

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


}
