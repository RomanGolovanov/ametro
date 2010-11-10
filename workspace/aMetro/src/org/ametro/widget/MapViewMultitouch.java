package org.ametro.widget;

import org.ametro.model.MapView;
import org.ametro.model.Model;

import android.content.Context;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class MapViewMultitouch extends MapViewBasic {

	public MapViewMultitouch(Context context, Model model, MapView scheme) {
		super(context, model, scheme);
	}

	public boolean onTouchEvent(MotionEvent event) {
		if(mode == TOUCH_AFTER_ZOOM_MODE){
			return false;
		}

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
				mode = TOUCH_DRAG_START_MODE;
			} else {
				mode = TOUCH_INIT_MODE;
			}
			// Trigger the link
			if (mode == TOUCH_INIT_MODE) {
				// mPrivateHandler.sendMessageDelayed(mPrivateHandler.obtainMessage(SWITCH_TO_SHORTPRESS),
				// TAP_TIMEOUT);
			}
			// Remember where the motion event started
			setTouchStart(event);
			velocityTracker = VelocityTracker.obtain();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			//Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) {
				if (!scroller.isFinished()) {
					scroller.abortAnimation();
				}
				savedMatrix.set(matrix);
				midPoint(mid, event);
				zoomX = currentX;
				zoomY = currentY;
				mode = TOUCH_ZOOM_MODE;
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mode == TOUCH_DONE_MODE) {
				break; // no dragging during scroll zoom animation
			} else if (mode == TOUCH_ZOOM_MODE) {
				doZoom(event);
				break;
			}
			velocityTracker.addMovement(event);

			if (mode != TOUCH_DRAG_MODE) {

				float x = event.getX();
				float y = event.getY();
				int deltaX = (int) (startTouchPoint.x - x);
				int deltaY = (int) (startTouchPoint.y - y);

				if ((deltaX * deltaX + deltaY * deltaY) < touchSlopSquare) {
					break;
				}
				// if (mode == TOUCH_SHORTPRESS_MODE || mode ==
				// TOUCH_SHORTPRESS_START_MODE) {
				// mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
				// } else if (mode == TOUCH_INIT_MODE) {
				// mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
				// }
				mode = TOUCH_DRAG_MODE;
			}
			return doDrag(event);
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			switch (mode) {
			case TOUCH_INIT_MODE: // tap
				// case TOUCH_SHORTPRESS_START_MODE:
				// case TOUCH_SHORTPRESS_MODE:
				// mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
				// mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
				mode = TOUCH_DONE_MODE;
				performClick();
				break;
			case TOUCH_DRAG_MODE:
			case TOUCH_DRAG_START_MODE:
				// if the user waits a while w/o moving before the
				// up, we don't want to do a fling
				if ((event.getEventTime() - startTouchTime) <= MIN_FLING_TIME) {
					velocityTracker.addMovement(event);
					doFling();
					break;
				}
				break;
			case TOUCH_ZOOM_MODE:
				EntireMapRenderTask task = new EntireMapRenderTask();
				task.execute();
				invalidate();
				mode = TOUCH_AFTER_ZOOM_MODE;
				return true;
			case TOUCH_DONE_MODE:
				// do nothing
				break;
			}
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}

			mode = TOUCH_DONE_MODE;
			break;
			
		}
		return true; // indicate event was handled
	}

	private void doZoom(MotionEvent event) {
		float newDist = spacing(event);
		//Log.d(TAG, "newDist=" + newDist);
		if (newDist > 10f) {
			doZoom(newDist / oldDist);
		}
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	

}
