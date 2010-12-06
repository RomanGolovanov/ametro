package org.ametro.ui.controllers;

import android.view.KeyEvent;
import android.view.MotionEvent;

public class KeyEventController {

	private MultiTouchController mController; 
	
	private boolean mEnabledVolumeZoom;
	private float mTrackballScrollSpeed;
	
	/** key-handled scroll constants and state **/
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
    
		
	public KeyEventController(MultiTouchController controller) {
		mEnabledVolumeZoom = true;
		mController = controller;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int mode = mController.getControllerMode();
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
        	if(!mEnabledVolumeZoom){
        		break;
        	}
        	if(mode == MultiTouchController.MODE_NONE){
        		mController.doZoomAnimation(MultiTouchController.ZOOM_IN);
        	}
        	return true;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        	if(!mEnabledVolumeZoom){
        		break;
        	}
        	if(mode == MultiTouchController.MODE_NONE){
        		mController.doZoomAnimation(MultiTouchController.ZOOM_OUT);
        	}
        	return true;
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
            mController.doScroll(dx, dy);
            return true;
        }
		return false;
	}

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
        	if(!mEnabledVolumeZoom){
        		break;
        	}
        	return true;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        	if(!mEnabledVolumeZoom){
        		break;
        	}
        	return true;
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            mKeyScrollMode = KEY_SCROLL_MODE_DONE;
            return true;
        }
        return false;
    }
    
    public boolean onTrackballEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
	            float dx = event.getX() * event.getXPrecision() * mTrackballScrollSpeed;
	            float dy = event.getY() * event.getYPrecision() * mTrackballScrollSpeed;
	            mController.doScroll(dx, dy);
	            return true;
        }
        return false;
    }

	public void setEnabledVolumeZoom(boolean enabled) {
		mEnabledVolumeZoom = enabled;
	}

	public void setTrackballScrollSpeed(int trackballScrollSpeed) {
		float k = (float)trackballScrollSpeed / 100.0f;
		mTrackballScrollSpeed = 10 * TRACKBALL_SCROLL_SPEED * k + TRACKBALL_SCROLL_SPEED / 2;
	}
	
	
}
