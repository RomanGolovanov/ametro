package org.ametro.ui.controllers;

import android.util.Log;
import android.view.MotionEvent;

public class MotionEventWrapper {

	public static final String TAG = "MotionEventWrapper";
	
	public static final int ACTION_POINTER_UP;
	public static final int ACTION_POINTER_DOWN;
	
	public static final boolean HasMultiTouchSupport;

	protected MotionEvent event;
	
	static {
		boolean succeeded = false;
		try {
			MotionEvent.class.getMethod("getPointerCount");
			succeeded = true;
		} catch (Exception e) {
			Log.e(TAG, "Methods static initializer failed", e);
		}
		HasMultiTouchSupport = succeeded;
		int pointerDown = 5;
		int pointerUp = 6;
		if (HasMultiTouchSupport) {
			try{
				pointerDown = MotionEvent.class.getField("ACTION_POINTER_DOWN").getInt(null);
				pointerUp = MotionEvent.class.getField("ACTION_POINTER_UP").getInt(null);
			}catch(Exception e){
				Log.e(TAG, "Constants static initializer failed", e);
			}
		}
		ACTION_POINTER_DOWN = pointerDown;
		ACTION_POINTER_UP = pointerUp;
	}	
	
	public MotionEventWrapper(MotionEvent event) {
		this.event = event;
	}

	public static MotionEventWrapper create(MotionEvent event){
		if(HasMultiTouchSupport){
			return new MotionEventMultiTouchWrapper(event);
		}else{
			return new MotionEventWrapper(event);
		}
	}
	
	public int getAction(){
		return event.getAction();
	}
	
	public float getX(){
		return event.getX();
	}
	
	public float getY(){
		return event.getY();
	}
	
	public float getX(int pos){
		return 0; 
	}
	
	public float getY(int pos){
		return 0;
	}

	public MotionEvent getEvent() {
		return event;
	}

	public long getEventTime() {
		return event.getEventTime();
	}
	
	
	
}
