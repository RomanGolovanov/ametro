package org.ametro.widget;

import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.render.RenderProgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;
import android.widget.Scroller;

public class MapView2 extends ScrollView implements OnTouchListener {

	public MapView2(Context context, Model model, MapView scheme) {
		super(context);
		this.model = model;
		this.scheme = scheme;
		this.renderer = new RenderProgram(scheme);
		renderer.setRenderFilter(RenderProgram.ALL);
		renderer.setAntiAlias(true);
		renderer.updateSelection(null, null, null);
		init(context);
	}

	public MapView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MapView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MapView2(Context context) {
		super(context);
		init(context);
	}

	Handler dispatcher = new Handler();
	Object mutex = new Object();

	Bitmap cacheImage0;
	Bitmap cacheImage;
	RectF cacheModelRect;
	Rect cacheScreenRect;
	Matrix cacheMatrix;
	float cacheScale;
	float cacheX;
	float cacheY;
	float cacheWidth;
	float cacheHeight;
	
	Runnable updateCache = new Runnable() {
		public void run() {
			drawOnCache(true);
		}
	}; 
	
	void drawOnCache(boolean force){
		if(!force && cacheImage!=null){
			if(currentScale == cacheScale){
				dispatcher.removeCallbacks(updateCache);
				dispatcher.post(updateCache);
				return;
			}
		}

		Rect screen = new Rect(0,0,getWidth(), getHeight());
		if(cacheScale == currentScale && screen.equals(cacheScreenRect) && (mode != TOUCH_DONE_MODE || !scroller.isFinished() ) ){
			drawPartial();
		}else{
			drawEntire();
		}
		
	}

	private void drawPartial() {
	
		final RectF cache = new RectF(cacheModelRect);
		final RectF viewport = getModelVisibleRect();
		final RectF v = new RectF(viewport);
		final RectF h = new RectF(viewport);
		final RectF i = new RectF(viewport);
		i.intersect(cache);

		if (viewport.right == i.right && viewport.bottom == i.bottom) {
			h.bottom = i.top;
			v.right = i.left;
		} else if (viewport.right == i.right && viewport.top == i.top) {
			h.top = i.bottom;
			v.right = i.left;
		} else if (viewport.left == i.left && viewport.bottom == i.bottom) {
			h.bottom = i.top;
			v.left = i.right;
		} else if (viewport.left == i.left && viewport.top == i.top) {
			h.top = i.bottom;
			v.left = i.right;
		} else {
			throw new RuntimeException("Invalid viewport splitting algorithm");
		}

		Rect screen = new Rect(0,0,getWidth(), getHeight());
		if(cacheImage0 == null){
			cacheImage0 = Bitmap.createBitmap(screen.width(), screen.height(), Config.RGB_565); 
		}
		Bitmap bmp = cacheImage0;
		
		Canvas c = new Canvas(bmp);
		
		c.save();
		c.setMatrix(currentMatrix);
		c.clipRect(viewport);
		
		renderer.setVisibilityTwice(h,v);
		renderer.draw(c);

		c.restore();

		float dx = cacheX - currentX;
		float dy = cacheY - currentY;
		c.drawBitmap(cacheImage, dx, dy, null);
		
		cacheImage0 = cacheImage;
		cacheImage = bmp;
		cacheModelRect = viewport;
		cacheScreenRect = screen;
		cacheMatrix = new Matrix(currentMatrix);
		cacheScale = currentScale;
		cacheX = currentX;
		cacheY = currentY;
	}

	private void drawEntire() {
		RectF viewport = getModelVisibleRect();
		Rect screen = new Rect(0,0,getWidth(), getHeight());
		
		Bitmap bmp = Bitmap.createBitmap(screen.width(), screen.height(), Config.RGB_565);
		
		Canvas c = new Canvas(bmp);
		c.setMatrix(currentMatrix);
		renderer.setVisibility(viewport);
		renderer.draw(c);
		
		cacheImage = bmp;
		cacheModelRect = viewport;
		cacheScreenRect = screen;
		cacheMatrix = new Matrix(currentMatrix);
		cacheScale = currentScale;
		cacheX = currentX;
		cacheY = currentY;
	}
	
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		if(renderer!=null){
			if(mode == TOUCH_ZOOM_MODE){
				float scale = currentScale / cacheScale;
				canvas.save();
				canvas.scale(scale, scale, mid.x, mid.y);
				canvas.drawBitmap(cacheImage, 0, 0, null);
				canvas.restore();
			}else{
				drawOnCache(false);
				float dx = cacheX - currentX;
				float dy = cacheY - currentY;
				canvas.drawBitmap(cacheImage, dx, dy, null);
			}
			
		}
		super.onDraw(canvas);
	}

	private RectF getModelVisibleRect() {
		RectF rect = new RectF(0, 0, getWidth(), getHeight());
		reversedMatrix.mapRect(rect);
		return rect;
	}

	protected int computeVerticalScrollOffset() {
		return (int) currentY;
	}

	protected int computeVerticalScrollRange() {
		return (int) currentHeight;
	}

	protected int computeHorizontalScrollOffset() {
		return (int) currentX;
	}

	protected int computeHorizontalScrollRange() {
		return (int) currentWidth;
	}

	private void init(Context context) {
		//img = BitmapFactory.decodeResource(context.getResources(), R.drawable.map);
		maxZoom = 4;
		minZoom = 0.25f;
		matrix.setTranslate(1f, 1f);
		updateMatrix();

		final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		touchSlopSquare = slop * slop;

		velocityTracker = null;
		scroller = new Scroller(context);
		setVerticalScrollBarEnabled(true);
		setHorizontalScrollBarEnabled(true);
		setOnTouchListener(this);
		
	}

	public int getContentWidth() {
		return scheme.width;
	}

	public int getContentHeight() {
		return scheme.height;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// Dump touch event to log
		//dumpEvent(event);
		long eventTime = event.getEventTime();

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
			savedMatrix.set(matrix);
			startTouchPoint.set(event.getX(), event.getY());
			startTouchTime = eventTime;
			velocityTracker = VelocityTracker.obtain();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) {
				if (!scroller.isFinished()) {
					scroller.abortAnimation();
				}
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = TOUCH_ZOOM_MODE;
			}
			break;
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
				if (eventTime - startTouchTime <= MIN_FLING_TIME) {
					velocityTracker.addMovement(event);
					doFling();
					break;
				}
				break;
			case TOUCH_ZOOM_MODE:
				postInvalidate();
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
			doDrag(event);
			break;
		}
		return true; // indicate event was handled
	}

	private void doZoom(MotionEvent event) {
		float newDist = spacing(event);
		Log.d(TAG, "newDist=" + newDist);
		if (newDist > 10f) {
			matrix.set(savedMatrix);
			float scale = newDist / oldDist;

			matrix.getValues(matrixValues);
			float currentScale = matrixValues[Matrix.MSCALE_X];

			// limit zoom
			if (scale * currentScale > maxZoom) {
				scale = maxZoom / currentScale;
			} else if (scale * currentScale < minZoom) {
				scale = minZoom / currentScale;
			}
			matrix.postScale(scale, scale, mid.x, mid.y);
		}
		updateMatrix();
	}

	private void doDrag(MotionEvent event) {
		matrix.set(savedMatrix);
		int height = getContentHeight();
		int width = getContentWidth();
		RectF viewRect = new RectF(0, 0, getViewWidth(), getViewHeight());
		// limit pan
		matrix.getValues(matrixValues);
		float currentY0 = matrixValues[Matrix.MTRANS_Y];
		float currentX0 = matrixValues[Matrix.MTRANS_X];
		float currentScale = matrixValues[Matrix.MSCALE_X];
		float currentHeight = height * currentScale;
		float currentWidth = width * currentScale;
		float dx = event.getX() - startTouchPoint.x;
		float dy = event.getY() - startTouchPoint.y;
		float newX = currentX0 + dx;
		float newY = currentY0 + dy;

		RectF drawingRect = new RectF(newX, newY, newX + currentWidth, newY
				+ currentHeight);
		float diffUp = Math.min(viewRect.bottom - drawingRect.bottom,
				viewRect.top - drawingRect.top);
		float diffDown = Math.max(viewRect.bottom - drawingRect.bottom,
				viewRect.top - drawingRect.top);
		float diffLeft = Math.min(viewRect.left - drawingRect.left,
				viewRect.right - drawingRect.right);
		float diffRight = Math.max(viewRect.left - drawingRect.left,
				viewRect.right - drawingRect.right);
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
		matrix.postTranslate(dx, dy);
		updateMatrix();
	}

	private void updateMatrix() {
		synchronized (mutex) {
			currentMatrix = matrix;
			matrix.getValues(matrixValues);
			currentY = -matrixValues[Matrix.MTRANS_Y];
			currentX = -matrixValues[Matrix.MTRANS_X];
			currentScale = matrixValues[Matrix.MSCALE_X];
			currentHeight = getContentHeight() * currentScale;
			currentWidth = getContentWidth() * currentScale;
			
			reversedMatrix.setTranslate(currentX, currentY);
			reversedMatrix.postScale(1/currentScale, 1/currentScale);
		}
		postInvalidate();
	}

	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			float x = scroller.getCurrX();
			float y = scroller.getCurrY();
			float dx = currentX - x;
			float dy = currentY - y;
			matrix.postTranslate(dx, dy);
			updateMatrix();
		}
	}

	private void doFling() {
		if (velocityTracker == null) {
			return;
		}
		velocityTracker.computeCurrentVelocity(1000);
		int vx = (int) velocityTracker.getXVelocity() / 2;
		int vy = (int) velocityTracker.getYVelocity() / 2;
		int maxX = (int) Math.max(currentWidth - getWidth(), 0);
		int maxY = (int) Math.max(currentHeight - getHeight(), 0);
		scroller.fling((int) currentX, (int) currentY, -vx, -vy, 0, maxX, 0,
				maxY);
	}

	@SuppressWarnings("unused")
	private void dumpEvent(MotionEvent event) {
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(
					action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]");
		Log.d(TAG, sb.toString());
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

	private static final String TAG = "Touch";
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	Matrix currentMatrix = new Matrix();
	Matrix reversedMatrix = new Matrix();

	private float currentX;
	private float currentY;
	private float currentScale;
	private float currentWidth;
	private float currentHeight;

	// We can be in one of these 3 states
	static final int TOUCH_DONE_MODE = 0;
	static final int TOUCH_INIT_MODE = 1;
	static final int TOUCH_DRAG_START_MODE = 2;
	static final int TOUCH_DRAG_MODE = 3;
	static final int TOUCH_ZOOM_MODE = 4;
	int mode = TOUCH_DONE_MODE;

	// Remember some things for zooming
	PointF startTouchPoint = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	long startTouchTime;

	// Limit zoomable/pannable image
	private float[] matrixValues = new float[9];
	private float maxZoom;
	private float minZoom;

	private Scroller scroller;
	private VelocityTracker velocityTracker;

	private static final int MIN_FLING_TIME = 500; // 250
	private int touchSlopSquare;

	//private Bitmap img;

	private Model model;
	private MapView scheme;
	private RenderProgram renderer;

}
