package org.ametro.ui.controllers;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import org.ametro.utils.AnimationInterpolator;

public class MultiTouchController {

	public interface IMultiTouchListener {
		Matrix getPositionAndScaleMatrix();
		void setPositionAndScaleMatrix(Matrix matrix);
		void onTouchModeChanged(int mode);
		void onPerformClick(PointF position);
		void onPerformLongClick(PointF position);
	}

	private IMultiTouchListener listener;

	private boolean initialized = false;
	
	private int touchSlopSquare;
	
	private Matrix matrix = new Matrix();
	private Matrix invertedMatrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	private AnimationInterpolator animationInterpolator = new AnimationInterpolator();
	
	private static final int MIN_FLING_TIME = 250;
	private static final int ANIMATION_TIME = 250;
	
	/** controller states **/
	private int mode = MODE_NONE;
	public static final int MODE_NONE = 1;
	public static final int MODE_INIT = 2;
	public static final int MODE_DRAG_START = 3;
	public static final int MODE_DRAG = 4;
	public static final int MODE_ZOOM = 5;
    public static final int MODE_SHORTPRESS_START = 6;
    public static final int MODE_SHORTPRESS_MODE = 7;
    public static final int MODE_LONGPRESS_START = 8;
    
    public static final int MODE_ANIMATION = 100;

	private static final int MSG_SWITCH_TO_SHORTPRESS = 1;
	private static final int MSG_SWITCH_TO_LONGPRESS = 2;
	private static final int MSG_PROCESS_FLING = 3;
	private static final int MSG_PROCESS_ANIMATION = 4;

	public static final int ZOOM_IN = 1;
	public static final int ZOOM_OUT = 2;

	private static final float ZOOM_LEVEL_DISTANCE = 1.5f;

	
	/** point of first touch **/ 
	private PointF touchStartPoint = new PointF();
	/** first touch time **/
	private long touchStartTime;
	/** point between first and second touch **/
	private PointF zoomCenter = new PointF();
	
	/** starting length between first and second touch **/
	private float zoomBase = 1f;

	private float[] matrixValues = new float[9];
	private float maxScale;
	private float minScale;
	
	private float contentHeight;
	private float contentWidth;
	private RectF displayRect;
	
	private Scroller scroller;
	private VelocityTracker velocityTracker;
	
    final Handler privateHandler = new PrivateHandler();
    private final  float density;

	private PointF animationEndPoint = new PointF();
	private PointF animationStartPoint = new PointF();
	
	public MultiTouchController(Context context, IMultiTouchListener IMultiTouchListener) {
		listener = IMultiTouchListener;
		scroller = new Scroller(context);
		ViewConfiguration vc = ViewConfiguration.get(context);
		final int slop = vc.getScaledTouchSlop();
		touchSlopSquare = slop * slop;
		density = context.getResources().getDisplayMetrics().density;
		velocityTracker = null;
	}

	/** Map point from model to screen coordinates **/
	public void mapPoint(PointF point){
		float[] pts = new float[2];
		pts[0] = point.x;
		pts[1] = point.y;
		matrix.mapPoints(pts);
		point.x = pts[0];
		point.y = pts[1];
	}
	
	/** Map point from screen to model coordinates **/
	public void unmapPoint(PointF point){
		matrix.invert(invertedMatrix);
		float[] pts = new float[2];
		pts[0] = point.x;
		pts[1] = point.y;
		invertedMatrix.mapPoints(pts);
		point.x = pts[0];
		point.y = pts[1];
	}

    public void setViewRect(float newContentWidth, float newContentHeight, RectF newDisplayRect){
		contentWidth = newContentWidth;
		contentHeight = newContentHeight;
		if(displayRect!=null){
			matrix.postTranslate((newDisplayRect.width()-displayRect.width())/2, (newDisplayRect.height()-displayRect.height())/2);
		}
		displayRect = newDisplayRect;
		// calculate zoom bounds
		maxScale = 2.0f * density;
		minScale = Math.min(displayRect.width()/contentWidth, displayRect.height()/contentHeight);
		adjustScale();
		adjustPan();
		listener.setPositionAndScaleMatrix(matrix);
	}
	
	public boolean onMultiTouchEvent(MotionEvent rawEvent) {
		if(mode == MODE_ANIMATION){
			return false;
		}
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
			setControllerMode(MODE_DRAG_START);
		} else {
			setControllerMode(MODE_INIT);
		}
		if (mode == MODE_INIT) {
			privateHandler.sendEmptyMessageDelayed(MSG_SWITCH_TO_SHORTPRESS, ViewConfiguration.getTapTimeout());
		}
		velocityTracker = VelocityTracker.obtain();
		savedMatrix.set(matrix);
		touchStartPoint.set(event.getX(), event.getY());
		touchStartTime = event.getEventTime();
		return true;
	}
	
	private boolean doActionPointerDown(MotionEventWrapper event){
		zoomBase = distance(event);
		//Log.d(TAG, "oldDist=" + zoomBase);
		if (zoomBase > 10f) {
			if (!scroller.isFinished()) {
				scroller.abortAnimation();
			}
			savedMatrix.set(matrix);
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			zoomCenter.set(x / 2, y / 2);
			setControllerMode( MODE_ZOOM );
		}			
		return true;
	}
	
	private boolean doActionMove(MotionEventWrapper event){
		if (mode == MODE_NONE || mode == MODE_LONGPRESS_START) {
			// no dragging during scroll zoom animation or while long press is not released
			return false; 
		} else if (mode == MODE_ZOOM) {
			float newDist = distance(event);
			if (newDist > 10f) {
				matrix.set(savedMatrix);
				float scale = newDist / zoomBase;

				matrix.getValues(matrixValues);
				float currentScale = matrixValues[Matrix.MSCALE_X];

				// limit zoom
				if (scale * currentScale > maxScale) {
					scale = maxScale / currentScale;
				} else if (scale * currentScale < minScale) {
					scale = minScale / currentScale;
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
			if (mode == MODE_SHORTPRESS_MODE || mode == MODE_SHORTPRESS_START) {
				privateHandler.removeMessages(MSG_SWITCH_TO_LONGPRESS);
			} else if (mode == MODE_INIT) {
				privateHandler.removeMessages(MSG_SWITCH_TO_SHORTPRESS);
			}
			setControllerMode(MODE_DRAG);
		}
		matrix.set(savedMatrix);
		float dx = event.getX() - touchStartPoint.x;
		float dy = event.getY() - touchStartPoint.y;
		matrix.postTranslate(dx, dy);
		adjustPan();
		return true;
	}
	
	private boolean doActionUp(MotionEventWrapper event){
		
		switch (mode) {
		case MODE_INIT: // tap
		case MODE_SHORTPRESS_START:
		case MODE_SHORTPRESS_MODE:
			privateHandler.removeMessages(MSG_SWITCH_TO_SHORTPRESS);
			privateHandler.removeMessages(MSG_SWITCH_TO_LONGPRESS);
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}
			setControllerMode(MODE_NONE);
			performClick();
			return true;
		case MODE_LONGPRESS_START:
			// do nothing
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
				privateHandler.sendEmptyMessage(MSG_PROCESS_FLING);
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
		setControllerMode(MODE_NONE);
		return true;
	}
	
	private boolean doActionCancel(MotionEventWrapper event){
        privateHandler.removeMessages(MSG_SWITCH_TO_SHORTPRESS);
        privateHandler.removeMessages(MSG_SWITCH_TO_LONGPRESS);
		setControllerMode( MODE_NONE );
		return true;
	}
	
	/*package*/ void setControllerMode(int newMode){
		boolean fireUpdate = mode != newMode;
		mode = newMode;
		if(fireUpdate){
			listener.onTouchModeChanged(newMode);
		}
	}
	
	public int getControllerMode(){
		return mode;
	}
	
	/** adjust map position to prevent zoom to outside of map **/
	private void adjustScale() {
		matrix.getValues(matrixValues);
		float currentScale = matrixValues[Matrix.MSCALE_X];
		if(currentScale<minScale){
			matrix.setScale(minScale, minScale);
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

		RectF drawingRect = new RectF(currentX, currentY, currentX + currentWidth,
				currentY + currentHeight);
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
		return (float)Math.sqrt(x * x + y * y);
	}

	boolean computeScroll() {
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
	
	boolean computeAnimation(){
		if(mode == MODE_ANIMATION){
			animationInterpolator.next();
			if(animationInterpolator.hasScale()){
				float scale = animationInterpolator.getScale() / getScale();
				matrix.postScale(scale, scale);
			}
			if(animationInterpolator.hasScroll()){
				PointF newCenter = animationInterpolator.getPoint();
				mapPoint(newCenter);
				float dx = newCenter.x - displayRect.width()/2;
				float dy = newCenter.y - displayRect.height()/2;
				matrix.postTranslate(-dx, -dy);
			}
			adjustScale();
			adjustPan();
			listener.setPositionAndScaleMatrix(matrix);
			return animationInterpolator.more();
		}
		return false;
	}
	
	public PointF getScreenTouchPoint(){
		return new PointF(touchStartPoint.x, touchStartPoint.y);
	}
	
	/** Return last touch point in model coordinates **/
	public PointF getTouchPoint(){
		PointF p = new PointF();
		p.set(touchStartPoint);
		unmapPoint(p);
		//Log.w(TAG,"point=" + touchStartPoint.x + "," + touchStartPoint.y);
		return p;
	}
	
	public float getScale(){
		matrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}
	
	public float getTouchRadius(){
		return touchSlopSquare;
	}

	public float getPositionAndScale(PointF position) {
		matrix.getValues(matrixValues);
		float scale = matrixValues[Matrix.MSCALE_X];
		if(position!=null){
			position.set(-matrixValues[Matrix.MTRANS_X]/scale, -matrixValues[Matrix.MTRANS_Y]/scale);
		}
		return scale;
	}
	
	public void setPositionAndScale(PointF position, float scale) {
		matrix.setScale(scale, scale);
		matrix.postTranslate(-position.x * scale, -position.y * scale);
		adjustScale();
		adjustPan();
		listener.setPositionAndScaleMatrix(matrix);
	}

	public void performLongClick() {
		listener.onPerformLongClick(getTouchPoint());
	}

	public void performClick() {
		listener.onPerformClick(getTouchPoint());
	}


	public void doZoomAnimation(int scaleMode, PointF scaleCenter){
		float scaleFactor = scaleMode == ZOOM_IN ? ZOOM_LEVEL_DISTANCE : 1/ZOOM_LEVEL_DISTANCE;
		float currentScale = getScale();
		float targetScale = Math.min( Math.max(minScale, scaleFactor * currentScale), maxScale );
		// do nothing is we're on ends of zoom range
		if(targetScale!=currentScale){
			// fix target zoom to snap to zoom limits
			float nextScale = Math.min( Math.max(minScale, scaleFactor * targetScale), maxScale );
			if(nextScale == maxScale && ( nextScale / targetScale ) < scaleFactor*0.8f ){
				targetScale = maxScale;
			}else if(nextScale == minScale && ( targetScale / nextScale ) < scaleFactor*0.8f  ){
				targetScale = minScale;
			}
			doScrollAndZoomAnimation(scaleCenter, targetScale);
		}
	}
	
	public void doScrollAndZoomAnimation(PointF center, Float scale){
		if(mode==MODE_NONE || mode==MODE_LONGPRESS_START){
			animationStartPoint.set(displayRect.width()/2, displayRect.height()/2);
			unmapPoint(animationStartPoint);
			if(center!=null){
				animationEndPoint.set(center);
			}else{
				animationEndPoint.set(animationStartPoint);
			}
			float currentScale = getScale();
			animationInterpolator.begin(animationStartPoint, animationEndPoint, currentScale, scale!=null ? scale : currentScale, ANIMATION_TIME);
			privateHandler.sendEmptyMessage(MSG_PROCESS_ANIMATION);
			setControllerMode(MODE_ANIMATION);
		}
	}

	class PrivateHandler extends Handler {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_PROCESS_ANIMATION: {
				if(mode == MODE_ANIMATION){
					boolean more = computeAnimation();
					if(more){
						privateHandler.sendEmptyMessage(MSG_PROCESS_ANIMATION);
					}else{
						setControllerMode(MODE_NONE);
						listener.setPositionAndScaleMatrix(matrix);
					}
				}
				break;
			}
			case MSG_PROCESS_FLING: {
	            boolean more = computeScroll();
	            if (more) {
	                privateHandler.sendEmptyMessage(MSG_PROCESS_FLING);
	            } 
				break;
			}
			case MSG_SWITCH_TO_SHORTPRESS: {
				if (mode == MODE_INIT) {
					setControllerMode(MODE_SHORTPRESS_START);
					privateHandler.sendEmptyMessageDelayed(MSG_SWITCH_TO_LONGPRESS, ViewConfiguration.getLongPressTimeout());
				}
				break;
			}
			case MSG_SWITCH_TO_LONGPRESS: {
				setControllerMode(MODE_LONGPRESS_START);
				performLongClick();
				break;
			}
			default:
				super.handleMessage(msg);
				break;
			}
		}
	}

	public Matrix getPositionAndScale() {
		return new Matrix(matrix);
	}

}
