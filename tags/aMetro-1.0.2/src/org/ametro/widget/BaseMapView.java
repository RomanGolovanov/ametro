/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.widget;

import static org.ametro.Constants.LOG_TAG_MAIN;

import org.ametro.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Toast;

public abstract class BaseMapView extends ScrollView {

    private Runnable mShowRenderFailedRunnable = new Runnable() {
		public void run() {
			Toast.makeText(getContext(), R.string.toast_render_failed, Toast.LENGTH_LONG).show();
		}
	};

	public BaseMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initializeControls();
    }

    public BaseMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initializeControls();
    }

    public BaseMapView(Context context) {
        super(context);
        mContext = context;
        initializeControls();
    }

    protected abstract void onDrawRect(Canvas canvas, Rect viewport);

    protected abstract int getContentWidth();

    protected abstract int getContentHeight();

    protected void setInitialized(boolean status) {
        invalidateScroll();
        if(mInitialized){
    		clearRenderFailed();
        }
        mInitialized = status;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int cw = getContentWidth();
        final int ch = getContentHeight();
        int x = mScrollX + oldw / 2;
        int y = mScrollY + oldh / 2;
        if (oldw > cw) {
            x = cw / 2;
        }
        if (oldh > ch) {
            y = ch / 2;
        }
        int scrollX = x - w / 2;
        int scrollY = y - h / 2;
        int maxX = Math.max(cw - w, 0);
        int maxY = Math.max(ch - w, 0);
        mScrollX = Math.max(0, Math.min(maxX, scrollX));
        mScrollY = Math.max(0, Math.min(maxY, scrollY));
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void setRenderFailed(Throwable th){
        mPrivateHandler.post(mShowRenderFailedRunnable);
    	mRenderFailed = true;
    }
    
    protected void clearRenderFailed(){
    	mRenderFailed = false;
    }
    
    protected void onDraw(Canvas canvas) {
        if (mInitialized && !mRenderFailed) {
	    	try{
	            if (mIsScrollNeeded) {
	                mScrollX = mScrollCenterX - getWidth() / 2;
	                mScrollY = mScrollCenterY - getHeight() / 2;
	                if (Log.isLoggable(LOG_TAG_MAIN, Log.DEBUG))
	                    Log.d(LOG_TAG_MAIN, "Set scroll center to " + mScrollCenterX + "x"
	                            + mScrollCenterY);
	                invalidateScroll();
	                mIsScrollNeeded = false;
	            }
	            final int left = mScrollX;
	            final int top = mScrollY;
	            final int right = left + getWidth();
	            final int bottom = top + getHeight();
	            Rect viewport = new Rect(left, top, right, bottom);
	            final int dx = Math.max(getWidth() - getContentWidth(), 0);
	            final int dy = Math.max(getHeight() - getContentHeight(), 0);
	            if (dx != 0 || dy != 0) {
	                canvas.translate(dx / 2, dy / 2);
	            }
	            canvas.save();
	            canvas.drawColor(Color.WHITE);
	            onDrawRect(canvas, viewport);
	            canvas.restore();
	    	}catch(Exception ex){
	    		setRenderFailed(ex);
	    	}
        }
    	if(mRenderFailed){
			Paint p = new Paint();
			p.setColor(Color.WHITE);
			p.setTextAlign(Align.CENTER);
			canvas.drawText(mRenderFailedErrorText, getWidth()/2, getHeight()/2, p);
    	}
        super.onDraw(canvas);
    }

    public void setScrollCenter(int x, int y) {
        setScrollCenter(new Point(x, y));
    }

	public void scrollCenterTo(Point p) {
        int scrollX = p.x - getWidth() / 2;
        int scrollY = p.y - getHeight() / 2;
        mScroller.startScroll(mScrollX, mScrollY, scrollX-mScrollX,  scrollY-mScrollY, 1000);
        
	}
    
    public void setScrollCenter(Point p) {
        mScrollX = p.x - getWidth() / 2;
        mScrollY = p.y - getHeight() / 2;
        mScrollCenterX = p.x;
        mScrollCenterY = p.y;
        mIsScrollNeeded = true;
        invalidateScroll();
        postInvalidate();
    }

    public Point getScrollCenter() {
        final int width = getWidth();
        final int height = getHeight();
        int x = mScrollX + width / 2;
        int y = mScrollY + height / 2;
        if (width > getContentWidth()) {
            x = getContentWidth() / 2;
        }
        if (height > getContentHeight()) {
            y = getContentHeight() / 2;
        }
        return new Point(x, y);
    }

    protected int computeVerticalScrollOffset() {
        return mInitialized && !mRenderFailed ? mScrollY : 0;
    }

    protected int computeVerticalScrollRange() {
        return mInitialized && !mRenderFailed ? getContentHeight() : 0;
    }

    protected int computeHorizontalScrollOffset() {
        return mInitialized && !mRenderFailed ? mScrollX : 0;
    }

    protected int computeHorizontalScrollRange() {
        return mInitialized && !mRenderFailed ? getContentWidth() : 0;
    }

    public abstract void onScrollBegin();
    public abstract void onScrollDone();
    
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
        	if(mScroller.isFinished()){
        		onScrollDone();
        	}
            mScrollX = mScroller.getCurrX();
            mScrollY = mScroller.getCurrY();
            invalidateScroll();
            postInvalidate();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                long eventTime = System.currentTimeMillis();
                if (mKeyScrollMode == KEY_SCROLL_MODE_DONE) {
                    mKeyScrollMode = KEY_SCROLL_MODE_DRAG;
                    mKeyScrollSpeed = KEY_SCROLL_MIN_SPEED;
                    mKeyScrollLastSpeedTime = eventTime;
                }
                if (mKeyScrollSpeed < KEY_SCROLL_MAX_SPEED
                        && (mKeyScrollLastSpeedTime + KEY_SCROLL_ACCELERATION_DELAY) < eventTime) {
                    mKeyScrollSpeed = Math.min(mKeyScrollSpeed
                            + KEY_SCROLL_ACCELERATION_STEP, KEY_SCROLL_MAX_SPEED);
                    mKeyScrollLastSpeedTime = eventTime;
                }
                int dx = 0;
                int dy = 0;
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                    dx = -mKeyScrollSpeed;
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                    dx = mKeyScrollSpeed;
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
                    dy = -mKeyScrollSpeed;
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    dy = mKeyScrollSpeed;
                internalScroll(dx, dy);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mKeyScrollMode = KEY_SCROLL_MODE_DONE;
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mContext != null) {
                    float x = event.getX() * event.getXPrecision()
                            * TRACKBALL_SCROLL_SPEED;
                    float y = event.getY() * event.getYPrecision()
                            * TRACKBALL_SCROLL_SPEED;
                    internalScroll((int) x, (int) y);
                    return true;
                }
        }
        return super.onTrackballEvent(event);
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

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mTouchMode = TOUCH_DRAG_START_MODE;
                } else {
                    mTouchMode = TOUCH_INIT_MODE;
                }
                
                // Trigger the link
                if (mTouchMode == TOUCH_INIT_MODE) {
                    mPrivateHandler.sendMessageDelayed(mPrivateHandler
                            .obtainMessage(SWITCH_TO_SHORTPRESS), TAP_TIMEOUT);
                }
                // Remember where the motion event started
                
                mLastTouchX = x;
                mLastTouchY = y;
                mLastTouchTime = eventTime;
                mVelocityTracker = VelocityTracker.obtain();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == TOUCH_DONE_MODE) {
                    // no dragging during scroll zoom animation
                    break;
                }
                mVelocityTracker.addMovement(event);

                if (mTouchMode != TOUCH_DRAG_MODE) {

                    if ((deltaX * deltaX + deltaY * deltaY) < mTouchSlopSquare) {
                        break;
                    }
                	
                    if (mTouchMode == TOUCH_SHORTPRESS_MODE || mTouchMode == TOUCH_SHORTPRESS_START_MODE) {
                        mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
                    } else if (mTouchMode == TOUCH_INIT_MODE) {
                        mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
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
                    internalScroll(deltaX, deltaY);
                    mLastTouchX = x;
                    mLastTouchY = y;
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
	                case TOUCH_SHORTPRESS_START_MODE:
	                case TOUCH_SHORTPRESS_MODE:
	                    mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
	                    mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
	                    mTouchMode = TOUCH_DONE_MODE;
	                    performClick();
                        //fireShortClickEvent((int) (mScrollX + event.getX()), (int) (mScrollY + event.getY()));
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
        int maxX = Math.max(getContentWidth() - getWidth(), 0);
        int maxY = Math.max(getContentHeight() - getHeight(), 0);

        mVelocityTracker.computeCurrentVelocity(1000);
        int vx = (int) mVelocityTracker.getXVelocity();
        int vy = (int) mVelocityTracker.getYVelocity();

        vx = vx / 2;
        vy = vy / 2;

        onScrollBegin();
        // mRenderProgram.setRenderFilter(RenderProgram.ONLY_TRANSPORT);
        mScroller.fling(mScrollX, mScrollY, -vx, -vy, 0, maxX, 0, maxY);
        postInvalidate();
    }

    private void initializeControls() {
    	mRenderFailedErrorText = getContext().getString(R.string.msg_render_failed);
        mInitialized = false;
        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(true);
        mScroller = new Scroller(mContext);
        
        final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mTouchSlopSquare = slop * slop;
    }

    private void internalScroll(int dx, int dy) {
        int x = mScrollX + dx;
        int y = mScrollY + dy;
        mScrollX = x;
        mScrollY = y;
        invalidateScroll();
        postInvalidate();
    }

    private void invalidateScroll() {
        int maxX = Math.max(getContentWidth() - getWidth(), 0);
        int maxY = Math.max(getContentHeight() - getHeight(), 0);
        mScrollX = Math.max(0, Math.min(maxX, mScrollX));
        mScrollY = Math.max(0, Math.min(maxY, mScrollY));
        if (mScrollX != mPreviousScrollX || mScrollCenterY != mPreviousScrollY) {
        	performMove(mScrollX, mScrollY, mPreviousScrollX, mPreviousScrollY);
            mPreviousScrollX = mScrollX;
            mPreviousScrollY = mScrollY;
        }
    }
    
    public boolean performMove(int x, int y, int nx, int ny){
    	return true;
    }

    // Expects x in view coordinates
    private int pinLocX(int x) {
        return pinLoc(x, getViewWidth(), getContentWidth());
    }

    // Expects y in view coordinates
    private int pinLocY(int y) {
        return pinLoc(y, getViewHeight(), getContentHeight());
    }

    private static int pinLoc(int x, int viewMax, int docMax) {
        if (docMax < viewMax) { // the doc has room on the sides for "blank"
            x = 0;
        } else if (x < 0) {
            x = 0;
        } else if (x + viewMax > docMax) {
            x = docMax - viewMax;
        }
        return x;
    }

    protected int getViewWidth() {
        if (!isVerticalScrollBarEnabled()) {
            return getWidth();
        } else {
            return Math.max(0, getWidth() - getVerticalScrollbarWidth());
        }
    }

    protected int getViewHeight() {
        if (!isHorizontalScrollBarEnabled()) {
            return getHeight();
        } else {
            return Math.max(0, getHeight() - getHorizontalScrollbarHeight());
        }
    }

    private boolean mInitialized;


    public void destroy() {
        if (mPrivateHandler != null) {
            // Remove any pending messages that might not be serviced yet.
        }
    }
    
    final Handler mPrivateHandler = new PrivateHandler();
    
    // adjustable parameters
    private static final int MIN_FLING_TIME = 250; // 250

	private Context mContext;
    
    private int mScrollX;
    private int mScrollY;

    private int mPreviousScrollX;
    private int mPreviousScrollY;

    private boolean mIsScrollNeeded;
    private int mScrollCenterX;
    private int mScrollCenterY;

    private int mKeyScrollSpeed = KEY_SCROLL_MIN_SPEED;
    private long mKeyScrollLastSpeedTime;
    private int mKeyScrollMode = KEY_SCROLL_MODE_DONE;

    private static final int KEY_SCROLL_MIN_SPEED = 2;
    private static final int KEY_SCROLL_MAX_SPEED = 20;
    private static final int KEY_SCROLL_ACCELERATION_DELAY = 100;
    private static final int KEY_SCROLL_ACCELERATION_STEP = 2;

    private static final int KEY_SCROLL_MODE_DONE = 0;
    private static final int KEY_SCROLL_MODE_DRAG = 1;

    private static final int TRACKBALL_SCROLL_SPEED = 10;

    private float mTouchSlopSquare; 
    private float mLastTouchX;
    private float mLastTouchY;
    private long mLastTouchTime;
    private int mTouchMode = TOUCH_DONE_MODE;

    
    private static final int TOUCH_INIT_MODE = 1;
    private static final int TOUCH_DRAG_START_MODE = 2;
    private static final int TOUCH_DRAG_MODE = 3;
    private static final int TOUCH_SHORTPRESS_START_MODE = 4;
    private static final int TOUCH_SHORTPRESS_MODE = 5;
    private static final int TOUCH_DONE_MODE = 7;


    // This should be ViewConfiguration.getTapTimeout()
    // But system time out is 100ms, which is too short for the browser.
    // In the browser, if it switches out of tap too soon, jump tap won't work.
    private static final int TAP_TIMEOUT = 200;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private static final int SWITCH_TO_SHORTPRESS = 3;
    private static final int SWITCH_TO_LONGPRESS = 4;

    private boolean mRenderFailed = false;
    private String mRenderFailedErrorText;
    
    
    /**
     * General handler to receive message coming from webkit thread
     */
    class PrivateHandler extends Handler {

    	public void handleMessage(Message msg) {
            switch (msg.what) {
                case SWITCH_TO_SHORTPRESS: {
                    if (mTouchMode == TOUCH_INIT_MODE) {
                        mTouchMode = TOUCH_SHORTPRESS_START_MODE;
                        //updateSelection();
                        performClick();
                    }
                    break;
                }
                case SWITCH_TO_LONGPRESS: {
                    mTouchMode = TOUCH_DONE_MODE;
                    performLongClick();
                    //updateTextEntry();
                    break;
                }
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
    
}
