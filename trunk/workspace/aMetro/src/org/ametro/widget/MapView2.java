package org.ametro.widget;

import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.render.RenderProgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.Scroller;

public class MapView2 extends ScrollView {

	public static class DrawingCache
	{
		public Bitmap Image;
		public RectF ModelRect;
		public Rect ScreenRect;
		public float Scale;
		public float X;
		public float Y;
		public float Width;
		public float Height;
		public boolean IsEntireMapCached;
		
		public DrawingCache(Bitmap image, RectF modelRect, Rect screenRect, float scale, float x, float y, float width, float height, boolean isEntireMapCached) {
			super();
			Image = image;
			ModelRect = modelRect;
			ScreenRect = screenRect;
			Scale = scale;
			X = x;
			Y = y;
			Width = width;
			Height = height;
			IsEntireMapCached = isEntireMapCached;
		}

		public boolean isContain(RectF viewport) {
			return IsEntireMapCached || ModelRect.contains(viewport);
		}

		public boolean isScreenEquals(Rect screen) {
			return IsEntireMapCached || ScreenRect.equals(screen);
		}

		public void reuse(RectF modelRect, float scale, float x, float y, float width, float height, boolean isEntireMapCached) {
			ModelRect = modelRect;
			Scale = scale;
			X = x;
			Y = y;
			Width = width;
			Height = height;
			IsEntireMapCached = isEntireMapCached;
		}
	}
	
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

	DrawingCache cache;
	DrawingCache cacheOld;
	Paint cachePaint;

	class EntireMapRenderTask extends AsyncTask<Void, Void, DrawingCache> {

		float scale;
		float width;
		float height;
		
		//ProgressDialog dialog;
		
		protected void onPreExecute() {
			super.onPreExecute();
			scale = currentScale;
			width = currentWidth;
			height = currentHeight;
			//dialog = ProgressDialog.show(MapView2.this.getContext(),"Wait","map rendering...");
			//dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			//dialog.show();
			boolean execGC = false;
			if(cacheOld!=null){
				Bitmap img = cacheOld.Image;
				cacheOld = null;
				img.recycle();
				execGC = true;
			}
			if(cache!=null && cache.IsEntireMapCached){
				Bitmap img = cache.Image;
				cache = null;
				img.recycle();
				execGC = true;
			}
			if(execGC){
				System.gc();
			}
		}
		
		protected DrawingCache doInBackground(Void... params) {
			int imageWidth = (int)width;
			int imageHeight = (int)height;
			int size = imageWidth * imageHeight * 2;
			if(size < (4 * 1024 * 1024)){
				try{
					Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Config.RGB_565);
					DrawingCache cache = new DrawingCache(bmp, null, null, scale, 0, 0, width, height, true);
					Canvas c = new Canvas(cache.Image);
					//c.setMatrix(currentMatrix);
					c.scale(scale, scale);
					renderer.setVisibilityAll();
					renderer.draw(c);
					return cache;
				}catch(Throwable th){
					return null;
				}
			}
			return null;
		}

		protected void onPostExecute(DrawingCache result) {
			//dialog.dismiss();
			if(result!=null && currentScale == scale && currentWidth == width && currentHeight == height){
				dispatcher.removeCallbacks(updateCache);
				cache = result;
				cacheOld = null;
				invalidate();
			}
			super.onPostExecute(result);
		}
	};
	
	Runnable updateCache = new Runnable() {
		public void run() {
			invalidateCache(false);
			invalidate();
		}
	}; 

	void invalidateCache(boolean invalidateOnly){
		if(cachePaint==null){
			cachePaint = new Paint();
			cachePaint.setAntiAlias(true);
		}
		if(cache!=null){
			if(cache.Scale == currentScale){
				Rect screen = new Rect(0,0,getWidth(), getHeight());
				if(cache.IsEntireMapCached){
					return;
				}
				if(screen.equals(cache.ScreenRect)){
					if(invalidateOnly) return;
					if(mode != TOUCH_DONE_MODE || !scroller.isFinished() ){
						drawPartial();
					}
				}
			}
		}
		drawEntire();
	}
	
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		if(renderer!=null){
			if(cache == null){
				EntireMapRenderTask task = new EntireMapRenderTask();
				task.execute();
			}
			if(mode == TOUCH_ZOOM_MODE){
				float scale = currentScale / cache.Scale;
				canvas.save();
				canvas.scale(scale, scale, mid.x, mid.y);
				if(cache.IsEntireMapCached){
					canvas.drawBitmap(cache.Image, -zoomX, -zoomY, cachePaint);
				}else{
					canvas.drawBitmap(cache.Image, 0, 0, cachePaint);
				}
				canvas.restore();
			}else{
				invalidateCache(true);
				float dx = cache.X - currentX;
				float dy = cache.Y - currentY;
				canvas.drawBitmap(cache.Image, dx, dy, cachePaint);

				boolean isHitCache = cache.isContain(getModelVisibleRect());
				
				/// SAMSUNG GALAXY S bug workaround
				if(!isHitCache || overrenderCount<MAX_OVERRENDER_COUNT){
					dispatcher.post(updateCache);
					/// SAMSUNG GALAXY S bug workaround
					if(isHitCache){
						 overrenderCount++;
					}else{
						overrenderCount = 0;
					}
				}
				if(cache.IsEntireMapCached){
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		super.onDraw(canvas);
	}
	
	private void drawPartial() {
		RectF viewport = getModelVisibleRect();
		Rect screen = new Rect(0,0,getWidth(), getHeight());
		DrawingCache cacheNew;
		if(cacheOld!=null && cacheOld.isScreenEquals(screen) && !cacheOld.IsEntireMapCached){
			cacheNew = cacheOld;
			cacheNew.reuse(viewport, currentScale, currentX, currentY, currentWidth, currentHeight, false);
		}else{
			Bitmap bmp = Bitmap.createBitmap(screen.width(), screen.height(), Config.RGB_565);
			cacheNew = new DrawingCache(bmp, viewport, screen, currentScale, currentX, currentY, currentWidth, currentHeight, false);
		}
		
		final RectF v = new RectF(viewport);
		final RectF h = new RectF(viewport);
		final RectF i = new RectF(viewport);
		i.intersect(cache.ModelRect);

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

		Canvas c = new Canvas(cacheNew.Image);
		
		c.save();
		c.setMatrix(currentMatrix);
		c.clipRect(viewport);
		
		renderer.setVisibilityTwice(h,v);
		renderer.draw(c);

		c.restore();

		float dx = cache.X - currentX;
		float dy = cache.Y - currentY;
		c.drawBitmap(cache.Image, dx, dy, null);
		
		cacheOld = cache;
		cache = cacheNew;
	}

	private void drawEntire() {
		RectF viewport = getModelVisibleRect();
		Rect screen = new Rect(0,0,getWidth(), getHeight());
		DrawingCache cacheNew;
		if(cacheOld!=null && cacheOld.isScreenEquals(screen) && !cacheOld.IsEntireMapCached){
			cacheNew = cacheOld;
			cacheNew.reuse(viewport, currentScale, currentX, currentY, currentWidth, currentHeight, false);
		}else{
			Bitmap bmp = Bitmap.createBitmap(screen.width(), screen.height(), Config.RGB_565);
			cacheNew = new DrawingCache(bmp, viewport, screen, currentScale, currentX, currentY, currentWidth, currentHeight, false);
		}
		
		Canvas c = new Canvas(cacheNew.Image);
		c.setMatrix(currentMatrix);
		renderer.setVisibility(viewport);
		renderer.draw(c);
		
		cacheOld = cache;
		cache = cacheNew;
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
	}

	public int getContentWidth() {
		return scheme.width;
	}

	public int getContentHeight() {
		return scheme.height;
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		screenRect = new Rect(0, 0, w, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
    	case KeyEvent.KEYCODE_VOLUME_UP:
    		savedMatrix.set(matrix);
    		doZoom(currentScale*1.5f);
    		
    		return true;
    	case KeyEvent.KEYCODE_VOLUME_DOWN:
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
            
            
            savedMatrix.set(matrix);
            doDrag(-dx, -dy);
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
    
	public boolean onTouchEvent(MotionEvent event) {
		// Dump touch event to log
		//dumpEvent(event);

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

	private void setTouchStart(MotionEvent event) {
		savedMatrix.set(matrix);
		startTouchPoint.set(event.getX(), event.getY());
		startTouchTime = event.getEventTime();
	}
	

	private void doZoom(MotionEvent event) {
		float newDist = spacing(event);
		//Log.d(TAG, "newDist=" + newDist);
		if (newDist > 10f) {
			doZoom(newDist / oldDist);
		}
	}

	private void doZoom(float scale){
		matrix.set(savedMatrix);
		matrix.getValues(matrixValues);
		float currentScale = matrixValues[Matrix.MSCALE_X];

		// limit zoom
		if (scale * currentScale > maxZoom) {
			scale = maxZoom / currentScale;
		} else if (scale * currentScale < minZoom) {
			scale = minZoom / currentScale;
		}
		matrix.postScale(scale, scale, mid.x, mid.y);
		updateMatrix();
		invalidate();
	}
	

	private boolean doDrag(MotionEvent event) {
		float dx = event.getX() - startTouchPoint.x;
		float dy = event.getY() - startTouchPoint.y;
		if(doDrag(dx, dy)){
			setTouchStart(event);
			return true;
		}
		return false;
	}
	
	private boolean doDrag(float dx, float dy){
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
		if(dx!=0 || dy!=0){
			matrix.postTranslate(dx, dy);
			updateMatrix();
			invalidate();
			return true;
		}
		return false;
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
	}

	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			float x = scroller.getCurrX();
			float y = scroller.getCurrY();
			float dx = currentX - x;
			float dy = currentY - y;
			matrix.postTranslate(dx, dy);
			updateMatrix();
			invalidate();
			//Log.w(TAG, "Compute scroll " + dx + ", " + dy);
		}else{
			//Log.w(TAG, "Compute scroll end");
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
		scroller.fling((int) currentX, (int) currentY, -vx, -vy, 0, maxX, 0, maxY);
		//Log.w(TAG, "Fling " + vx + ", " + vy);
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

	private static final int MIN_FLING_TIME = 250; // 250
	private int touchSlopSquare;


	private float zoomX;
	private float zoomY;
	
	private Model model;
	private MapView scheme;
	private RenderProgram renderer;
	private RectF modelVisibleRect;
	private Rect screenRect;
	
	/// SAMSUNG GALAXY S bug workaround
	private int overrenderCount;
	private static final int MAX_OVERRENDER_COUNT = 5;


    private int mKeyScrollSpeed = KEY_SCROLL_MIN_SPEED;
    private long mKeyScrollLastSpeedTime;
    private int mKeyScrollMode = KEY_SCROLL_MODE_DONE;

    private static final int KEY_SCROLL_MIN_SPEED = 2;
    private static final int KEY_SCROLL_MAX_SPEED = 20;
    private static final int KEY_SCROLL_ACCELERATION_DELAY = 100;
    private static final int KEY_SCROLL_ACCELERATION_STEP = 2;

    private static final int KEY_SCROLL_MODE_DONE = 0;
    private static final int KEY_SCROLL_MODE_DRAG = 1;	
}
