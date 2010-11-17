package org.ametro.multitouch;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class MultiTouchController<T> {

	static final String TAG = "MultiTouchController";

	public static interface MultiTouchListener<T>{
		public Matrix getPositionAndScaleMatrix();
		public void setPositionAndScaleMatrix(Matrix matrix);
		public void onTouchModeChanged(int mode);
	}

	private MultiTouchListener<T> listener;

	private boolean initialized = false;
	private int touchSlopSquare;
	
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	private static final int MIN_FLING_TIME = 250;
	
	/** controller states **/
	private int mode = MODE_NONE;
	public static final int MODE_NONE = 1;
	public static final int MODE_INIT = 2;
	public static final int MODE_DRAG_START = 3;
	public static final int MODE_DRAG = 4;
	public static final int MODE_ZOOM = 5;

	/** point of first touch **/ 
	private PointF touchStartPoint = new PointF();
	/** first touch time **/
	private long touchStartTime;
	/** point between first and second touch **/
	private PointF zoomCenter = new PointF();
	
	/** starting length between first and second touch **/
	private float zoomBase = 1f;

	private float[] matrixValues = new float[9];
	private float maxZoom;
	private float minZoom;
	private float contentHeight;
	private float contentWidth;
	private RectF displayRect;
	
	private Scroller scroller;
	private VelocityTracker velocityTracker;
	
    private Handler handler = new Handler();
    private final  float density;
	
	public MultiTouchController(Context context, MultiTouchListener<T> multiTouchListener) {
		listener = multiTouchListener;
		scroller = new Scroller(context);
		final int slop = ViewConfiguration.get(context).getScaledTouchSlop();
		density = context.getResources().getDisplayMetrics().density;
		touchSlopSquare = slop * slop;
		velocityTracker = null;
	}

	/** Setup view rect and content size **/
	public void setViewRect(float newContentWidth, float newContentHeight, RectF newDisplayRect){
		contentWidth = newContentWidth;
		contentHeight = newContentHeight;

		if(displayRect!=null){
			matrix.postTranslate((newDisplayRect.width()-displayRect.width())/2, (newDisplayRect.height()-displayRect.height())/2);
		}
		
		displayRect = newDisplayRect;
		// calculate zoom bounds
		maxZoom = 2.0f * density;
		minZoom = Math.min(displayRect.width()/contentWidth, displayRect.height()/contentHeight);
		
		adjustZoom();
		adjustPan();
		listener.setPositionAndScaleMatrix(matrix);
	}
	
	public boolean onMultiTouchEvent(MotionEvent rawEvent) {
		MotionEventWrapper event = MotionEventWrapper.create(rawEvent);
		if(!initialized){
			matrix.set(listener.getPositionAndScaleMatrix());
			initialized = true;
		}
		int action = event.getAction();
		boolean handled = true;
		if(action == MotionEvent.ACTION_DOWN){
			handled = doActionDown(event);
		}else if (action == MotionEventWrapper.ACTION_POINTER_DOWN){
			handled = doActionPointerDown(event);
		}else if(action == MotionEvent.ACTION_UP || action == MotionEventWrapper.ACTION_POINTER_UP){
			handled = doActionUp(event);
		}else if(action == MotionEvent.ACTION_CANCEL ){
			handled = doActionCancel(event);
		}else if(action == MotionEvent.ACTION_MOVE){
			handled = doActionMove(event);
		}
		listener.setPositionAndScaleMatrix(matrix);
		return handled;
	}

	private boolean doActionDown(MotionEventWrapper event){
		if (!scroller.isFinished()) {
			scroller.abortAnimation();
			setMode(MODE_DRAG_START);
		} else {
			setMode(MODE_INIT);
		}
		if (mode == MODE_INIT) {
			// mPrivateHandler.sendMessageDelayed(mPrivateHandler.obtainMessage(SWITCH_TO_SHORTPRESS), TAP_TIMEOUT);
		}
		velocityTracker = VelocityTracker.obtain();
		savedMatrix.set(matrix);
		touchStartPoint.set(event.getX(), event.getY());
		touchStartTime = event.getEventTime();
		return true;
	}
	
	private boolean doActionPointerDown(MotionEventWrapper event){
		zoomBase = distance(event);
		Log.d(TAG, "oldDist=" + zoomBase);
		if (zoomBase > 10f) {
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
			}
			savedMatrix.set(matrix);
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			zoomCenter.set(x / 2, y / 2);
			setMode( MODE_ZOOM );
		}			
		return true;
	}
	
	private boolean doActionMove(MotionEventWrapper event){
		if (mode == MODE_NONE) {
			return false; // no dragging during scroll zoom animation
		} else if (mode == MODE_ZOOM) {
			float newDist = distance(event);
			if (newDist > 10f) {
				matrix.set(savedMatrix);
				float scale = newDist / zoomBase;

				matrix.getValues(matrixValues);
				float currentScale = matrixValues[Matrix.MSCALE_X];

				// limit zoom
				if (scale * currentScale > maxZoom) {
					scale = maxZoom / currentScale;
				} else if (scale * currentScale < minZoom) {
					scale = minZoom / currentScale;
				}
				matrix.postScale(scale, scale, zoomCenter.x, zoomCenter.y);
				adjustPan();
			}
			return true;
		}
		velocityTracker.addMovement(event.getEvent());

		if (mode != MODE_DRAG) {
			int deltaX = (int) (touchStartPoint.x - event.getX());
			int deltaY = (int) (touchStartPoint.y - event.getY());

			if ((deltaX * deltaX + deltaY * deltaY) < touchSlopSquare) {
				return false;
			}
			// if (mode == TOUCH_SHORTPRESS_MODE || mode ==
			// TOUCH_SHORTPRESS_START_MODE) {
			// mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
			// } else if (mode == TOUCH_INIT_MODE) {
			// mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
			// }
			setMode(MODE_DRAG);
		}
		matrix.set(savedMatrix);
		float dx = event.getX() - touchStartPoint.x;
		float dy = event.getY() - touchStartPoint.y;
		matrix.postTranslate(dx, dy);
		adjustPan();
		
		//touchStartPoint.set(event.getX(), event.getY());
		//touchStartTime = event.getEventTime();
		
		
		return true;
	}
	
	private boolean doActionUp(MotionEventWrapper event){
		
		switch (mode) {
		case MODE_INIT: // tap
			// case TOUCH_SHORTPRESS_START_MODE:
			// case TOUCH_SHORTPRESS_MODE:
			// mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
			// mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
			//performClick();
			break;
		case MODE_DRAG:
		case MODE_DRAG_START:
			// if the user waits a while w/o moving before the
			// up, we don't want to do a fling
			if ((event.getEventTime() - touchStartTime) <= MIN_FLING_TIME) {
				velocityTracker.addMovement(event.getEvent());
				velocityTracker.computeCurrentVelocity(1000);
				
				matrix.getValues(matrixValues);
				float currentY = matrixValues[Matrix.MTRANS_Y];
				float currentX = matrixValues[Matrix.MTRANS_X];
				float currentScale = matrixValues[Matrix.MSCALE_X];
				float currentHeight = contentHeight * currentScale;
				float currentWidth = contentWidth * currentScale;
				
				int vx = (int) -velocityTracker.getXVelocity() / 2;
				int vy = (int) -velocityTracker.getYVelocity() / 2;
				int maxX = (int) Math.max(currentWidth - displayRect.width(), 0);
				int maxY = (int) Math.max(currentHeight - displayRect.height(), 0);
				scroller.fling((int) -currentX, (int) -currentY, vx, vy, 0, maxX, 0, maxY);
				handler.post(flingRunnable);
				break;
			}
			break;
		case MODE_ZOOM:
			// ???
		case MODE_NONE:
			// do nothing
		}
		if (velocityTracker != null) {
			velocityTracker.recycle();
			velocityTracker = null;
		}
		setMode(MODE_NONE);
		return true;
	}
	
	private boolean doActionCancel(MotionEventWrapper event){
		setMode( MODE_NONE );
		return true;
	}
	
	private void setMode(int newMode){
		boolean fireUpdate = mode != newMode;
		mode = newMode;
		if(fireUpdate){
			listener.onTouchModeChanged(newMode);
		}
	}
	
	/** adjust map position to prevent zoom to outside of map **/
	private void adjustZoom() {
		matrix.getValues(matrixValues);
		float currentScale = matrixValues[Matrix.MSCALE_X];
		if(currentScale<minZoom){
			matrix.setScale(minZoom, minZoom);
		}
	}
	
	/** adjust map position to prevent pan to outside of map **/
	private void adjustPan(){
		matrix.getValues(matrixValues);
		float currentY = matrixValues[Matrix.MTRANS_Y];
		float currentX = matrixValues[Matrix.MTRANS_X];
		float currentScale = matrixValues[Matrix.MSCALE_X];
		float currentHeight = contentHeight * currentScale;
		float currentWidth = contentWidth * currentScale;
		float newX = currentX;
		float newY = currentY;

		RectF drawingRect = new RectF(newX, newY, newX + currentWidth,
				newY + currentHeight);
		float diffUp = Math.min(displayRect.bottom - drawingRect.bottom,
				displayRect.top - drawingRect.top);
		float diffDown = Math.max(displayRect.bottom - drawingRect.bottom,
				displayRect.top - drawingRect.top);
		float diffLeft = Math.min(displayRect.left - drawingRect.left,
				displayRect.right - drawingRect.right);
		float diffRight = Math.max(displayRect.left - drawingRect.left,
				displayRect.right - drawingRect.right);
		float dx=0, dy=0;
		if (diffUp > 0) {
			dy += diffUp;
		}
		if (diffDown < 0) {
			dy += diffDown;
		}
		if (diffLeft > 0) {
			dx += diffLeft;
		}
		if (diffRight < 0) {
			dx += diffRight;
		}
		if(currentWidth<displayRect.width()){
			dx = -currentX + (displayRect.width() - currentWidth)/2;
		}
		if(currentHeight<displayRect.height()){
			dy = -currentY + (displayRect.height() - currentHeight)/2;
		}
		matrix.postTranslate(dx, dy);
	}
	
	/** Determine the distance between the first two fingers */
	private float distance(MotionEventWrapper event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	public boolean computeScroll() {
		boolean more = scroller.computeScrollOffset();
		if (more) {
			float x = scroller.getCurrX();
			float y = scroller.getCurrY();

			matrix.getValues(matrixValues);
			float currentY = -matrixValues[Matrix.MTRANS_Y];
			float currentX = -matrixValues[Matrix.MTRANS_X];
			float dx = currentX - x;
			float dy = currentY - y;
			
			matrix.postTranslate(dx, dy);
			adjustPan();
			listener.setPositionAndScaleMatrix(matrix);
		}
		return more;
	}
	
    Runnable flingRunnable = new Runnable() {
        public void run() {
            boolean more = computeScroll();
            if (more) {
                handler.post(this);
            } 
        }
    };
}
