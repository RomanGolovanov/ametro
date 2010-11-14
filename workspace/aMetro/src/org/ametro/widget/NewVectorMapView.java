package org.ametro.widget;

import java.util.ArrayList;

import org.ametro.GlobalSettings;
import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.render.RenderElement;
import org.ametro.render.RenderProgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.FloatMath;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ScrollView;
import android.widget.Scroller;

public class NewVectorMapView extends ScrollView {

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
	
	public NewVectorMapView(Context context, Model model, MapView scheme) {
		super(context);
		this.model = model;
		this.scheme = scheme;
		this.renderer = new RenderProgram(scheme);
		renderer.setRenderFilter(RenderProgram.ALL);
		renderer.setAntiAlias(true);
		renderer.updateSelection(null, null, null);
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
		Bitmap bmp;
		
		protected void onPreExecute() {
			super.onPreExecute();
			scale = currentScale;
			width = currentWidth;
			height = currentHeight;
			if(cacheOld!=null){
				Bitmap img = cacheOld.Image;
				cacheOld = null;
				img.recycle();
			}
			if(cache!=null && cache.IsEntireMapCached){

				Rect screen = new Rect(0,0,getWidth(),getHeight());
				RectF viewport = getModelVisibleRect();
				
				Bitmap bmp0 = Bitmap.createBitmap(getWidth(), getHeight(), Config.RGB_565);
				Canvas canvas = new Canvas(bmp0);
				float scale = currentScale / cache.Scale;
				canvas.save();
				canvas.scale(scale, scale, mid.x, mid.y);
				canvas.drawBitmap(cache.Image, -zoomX, -zoomY, cachePaint);
				canvas.restore();
				
				Bitmap img = cache.Image;
				cache = null;
				img.recycle();
				
				cache = new DrawingCache(bmp0, viewport, screen, currentScale, currentX, currentY, currentWidth, currentHeight, false);
			}
			
			try{
				int imageWidth = (int)width;
				int imageHeight = (int)height;
				int size = imageWidth * imageHeight * 2;
				if(size < (4 * 1024 * 1024)){
					bmp = Bitmap.createBitmap(imageWidth, imageHeight, Config.RGB_565);
					if(mode == TOUCH_AFTER_ZOOM_MODE){
						mode = TOUCH_AFTER_ZOOM_RENDER_MODE;
						invalidate();
					}
				}				
			}catch(Throwable th){
				bmp = null;
			}
		}
		
		protected DrawingCache doInBackground(Void... params) {
			if(bmp!=null){
				try{
					DrawingCache cache = new DrawingCache(bmp, null, null, scale, 0, 0, width, height, true);
					Canvas c = new Canvas(cache.Image);
					c.scale(scale, scale);
					ArrayList<RenderElement> elements = renderer.setVisibilityAll();
					c.drawColor(Color.WHITE);
					for (RenderElement elem : elements) {
						elem.draw(c);
					}
					return cache;
				}catch(Throwable th){
					return null;
				}
			}
			return null;
		}

		protected void onPostExecute(DrawingCache result) {
			mode = TOUCH_DONE_MODE;
			if(result!=null){
				if(cache!=null){
					Bitmap img = cache.Image;
					cache = null;
					img.recycle();
				}
				cache = result;
				cacheOld = null;
			}
			overrenderCount = 0;
			invalidate();
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
						return;
					}
				}
			}
		}
		drawEntire();
	}

	
	
	protected void onDraw(Canvas canvas) {
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		canvas.save();
		
		zero.x = Math.max(viewWidth - currentWidth, 0)/2;
		zero.y = Math.max(viewHeight - currentHeight, 0)/2;
		if((mode == TOUCH_ZOOM_MODE || mode == TOUCH_AFTER_ZOOM_MODE || mode == TOUCH_AFTER_ZOOM_RENDER_MODE)){
			if (zoomZero.x != 0 || zoomZero.y != 0) {
				canvas.translate(zoomZero.x, zoomZero.y);
			}
			canvas.clipRect(0, 0, zoomZero.x==0 ? viewWidth : currentWidth , zoomZero.y==0 ? viewHeight : currentHeight);
			canvas.drawColor(Color.BLACK);
		}else{
			if (zero.x != 0 || zero.y != 0) {
				canvas.translate(zero.x, zero.y);
			}
			canvas.clipRect(0, 0, zero.x==0 ? viewWidth : currentWidth , zero.y==0 ? viewHeight : currentHeight);
			canvas.drawColor(Color.BLACK);
		}
		
		if(renderer!=null){
			if(cache == null){
				EntireMapRenderTask task = new EntireMapRenderTask();
				task.execute();
			}
			if(mode == TOUCH_ZOOM_MODE || mode == TOUCH_AFTER_ZOOM_MODE || mode == TOUCH_AFTER_ZOOM_RENDER_MODE){
				float scale = currentScale / cache.Scale;
				canvas.save();
				canvas.scale(scale, scale, mid.x, mid.y);
				if(cache.IsEntireMapCached){
					canvas.drawBitmap(cache.Image, -zoomX, -zoomY, cachePaint);
				}else{
					canvas.drawBitmap(cache.Image, 0, 0, cachePaint);
				}
				if(mode == TOUCH_AFTER_ZOOM_RENDER_MODE){
					canvas.drawColor(0x80000000, Mode.DST_ATOP);
				}
				canvas.restore();
				
			}else{
				canvas.save();
				invalidateCache(true);
				float dx = cache.X - currentX;
				float dy = cache.Y - currentY;
				
				canvas.drawBitmap(cache.Image, dx, dy, cachePaint);
				canvas.restore();
				
				boolean isHitCache = cache.isContain(getModelVisibleRect());
				if(!isHitCache || overrenderCount<MAX_OVERRENDER_COUNT){
					//Log.w(TAG,"draw, post overrender");
					dispatcher.post(updateCache);
					if(isHitCache){
						 overrenderCount++;
					}else{
						overrenderCount = 0;
					}
				}else{
					//Log.w(TAG,"draw");
				}

			}
		}
		
		canvas.restore();
		if(GlobalSettings.isDebugMessagesEnabled(getContext())){
			Paint p = new Paint();
			p.setTextAlign(Align.LEFT);
			p.setStrokeWidth(2);
			p.setColor(Color.WHITE);
			p.setStyle(Style.FILL_AND_STROKE);
			canvas.drawText("X0: " + zero.x,10, 10, p);
			canvas.drawText("Y0: " + zero.y,10, 20, p);
			canvas.drawText("X: " + currentX,10, 30, p);
			canvas.drawText("Y: " + currentY,10, 40, p);
			canvas.drawText("W: " + currentWidth,10, 50, p);
			canvas.drawText("H: " + currentHeight,10, 60, p);
			canvas.drawText("Scale: " + currentScale,10, 70, p);
			p.setColor(Color.BLACK);
			p.setStyle(Style.FILL);
			canvas.drawText("X0: " + zero.x,10, 10, p);
			canvas.drawText("Y0: " + zero.y,10, 20, p);
			canvas.drawText("X: " + currentX,10, 30, p);
			canvas.drawText("Y: " + currentY,10, 40, p);
			canvas.drawText("W: " + currentWidth,10, 50, p);
			canvas.drawText("H: " + currentHeight,10, 60, p);
			canvas.drawText("Scale: " + currentScale,10, 70, p);
			
		}
		super.onDraw(canvas);
	}

	protected void drawPartial() {
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
		
		ArrayList<RenderElement> elements = renderer.setVisibilityTwice(h,v);
		c.drawColor(Color.WHITE);
		for (RenderElement elem : elements) {
			elem.draw(c);
		}

		c.restore();

		float dx = cache.X - currentX;
		float dy = cache.Y - currentY;
		c.drawBitmap(cache.Image, dx, dy, null);
		
		cacheOld = cache;
		cache = cacheNew;
	}

	protected void drawEntire() {
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
		ArrayList<RenderElement> elements = renderer.setVisibility(viewport);
		c.drawColor(Color.WHITE);
		for (RenderElement elem : elements) {
			elem.draw(c);
		}
		
		cacheOld = cache;
		cache = cacheNew;
	}

	protected RectF getModelVisibleRect() {
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

	protected void init(Context context) {
		//img = BitmapFactory.decodeResource(context.getResources(), R.drawable.map);
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
		if(screenRect == null){
			screenRect = new Rect(0,0,w, h);
		}else{
			screenRect.set(0, 0, w, h);
			savedMatrix.set(matrix);
			doDrag((w-oldw)/2, (h-oldh)/2);
			limitPan();			
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
    	case KeyEvent.KEYCODE_ENDCALL:
    		savedMatrix.set(matrix);
    		doZoom(currentScale*1.5f);
    		return true;
    	case KeyEvent.KEYCODE_CALL:
    		savedMatrix.set(matrix);
    		doZoom(currentScale/1.5f);
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
		if(mode == TOUCH_AFTER_ZOOM_MODE || mode == TOUCH_AFTER_ZOOM_RENDER_MODE){
			return false;
		}

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (!scroller.isFinished()) {
				//Log.w(TAG,"scroll aborted at action-down");
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
					//Log.w(TAG,"scroll aborted at action-pointer-down");
					scroller.abortAnimation();
				}
				savedMatrix.set(matrix);
				midPoint(mid, event);
				zoomX = currentX;
				zoomY = currentY;
				zoomZero.set(zero);
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

				float x = event.getX() - zero.x;
				float y = event.getY() - zero.y;
				int deltaX = (int) (start.x - x);
				int deltaY = (int) (start.y - y);

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
				// limit zoom pan
				limitPan();
				mode = TOUCH_AFTER_ZOOM_MODE;
				EntireMapRenderTask task = new EntireMapRenderTask();
				task.execute();
				invalidate();
				break;
			case TOUCH_DONE_MODE:
				// do nothing
				break;
			}
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}

			break;
		
		case MotionEvent.ACTION_CANCEL:
			mode = TOUCH_DONE_MODE;

			break;
		}
		return true; // indicate event was handled
	}

	private void limitPan() {
		float dx=0, dy=0;
		int width = getWidth();
		int height = getHeight();
		if(currentX<0 || width >= currentWidth){
			dx = currentX;
		}else if( (currentX+width)>currentWidth ){
			dx = -(currentWidth - (currentX + width));
		}
		if(currentY<0 || height >= currentHeight){
			dy = currentY;
		}else if( (currentY+height)>currentHeight ){
			dy = -(currentHeight - (currentY + height));
		}
		
		if(dx!=0 || dy!=0){
			matrix.postTranslate(dx, dy);
			updateMatrix();
		}
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
		float x = event.getX(0) + event.getX(1) - 2 * zero.x;
		float y = event.getY(0) + event.getY(1) - 2 * zero.y;
		point.set(x / 2, y / 2);
	}

	protected void setTouchStart(MotionEvent event) {
		savedMatrix.set(matrix);
		start.set(event.getX() - zero.x, event.getY() - zero.y);
		startTouchTime = event.getEventTime();
	}

	protected void doZoom(float scale){
		float maxZoom = 2.0f;
		float minZoom = Math.min((float)getWidth()/getContentWidth(), (float)getHeight()/getContentHeight()) ;
		matrix.set(savedMatrix);
		matrix.getValues(matrixValues);
		float currentScale = matrixValues[Matrix.MSCALE_X];

		float newScale = currentScale * scale;
		if((currentScale == minZoom && newScale <= minZoom) ||
			(currentScale == maxZoom && newScale >= maxZoom)){
			return;
		}
		
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
	

	protected boolean doDrag(MotionEvent event) {
		float dx = (event.getX() - zero.x) - start.x;
		float dy = (event.getY() - zero.y) - start.y;
		doDrag(dx, dy);
		setTouchStart(event);
		return true;
	}
	
	protected boolean doDrag(float dx, float dy){
		matrix.set(savedMatrix);
		if(getWidth() >= currentWidth){
			dx = currentX;
		}
		if(getHeight() >= currentHeight){
			dy = currentY;
		}
		matrix.postTranslate(dx, dy);
		updateMatrix();
		invalidate();
		return true;
	}

	protected void updateMatrix() {
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
			//Log.w(TAG, "compute scroll " + dx + ", " + dy);
			invalidate();
		}else{
			//Log.w(TAG, "compute scroll end");
		}
	}

	protected void doFling() {
		if (velocityTracker == null) {
			return;
		}
		velocityTracker.computeCurrentVelocity(1000);
		int vx = (int) -velocityTracker.getXVelocity() / 2;
		int vy = (int) -velocityTracker.getYVelocity() / 2;
		int maxX = (int) Math.max(currentWidth - getWidth(), 0);
		int maxY = (int) Math.max(currentHeight - getHeight(), 0);
		scroller.fling((int) currentX, (int) currentY, vx, vy, 0, maxX, 0, maxY);
		//Log.w(TAG, "fling vx=" + vx + ", vy=" + vy + ", maxx=" + maxX + ", maxy=" + maxY + ", fromx=" + currentX + ", fromy=" + currentY);
		postInvalidate();
	}
	
//	protected int getViewWidth() {
//		if (!isVerticalScrollBarEnabled()) {
//			return getWidth();
//		} else {
//			return Math.max(0, getWidth() - getVerticalScrollbarWidth());
//		}
//	}
//
//	protected int getViewHeight() {
//		if (!isHorizontalScrollBarEnabled()) {
//			return getHeight();
//		} else {
//			return Math.max(0, getHeight() - getHorizontalScrollbarHeight());
//		}
//	}

	protected static final String TAG = "Touch";
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	Matrix currentMatrix = new Matrix();
	Matrix reversedMatrix = new Matrix();

	protected float currentX;
	protected float currentY;
	protected float currentScale;
	protected float currentWidth;
	protected float currentHeight;

	// We can be in one of these 3 states
	static final int TOUCH_DONE_MODE = 0;
	static final int TOUCH_INIT_MODE = 1;
	static final int TOUCH_DRAG_START_MODE = 2;
	static final int TOUCH_DRAG_MODE = 3;
	static final int TOUCH_ZOOM_MODE = 4;
	static final int TOUCH_AFTER_ZOOM_MODE = 5;
	static final int TOUCH_AFTER_ZOOM_RENDER_MODE = 6;
	
	int mode = TOUCH_DONE_MODE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF zero = new PointF();
	PointF zoomZero = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	long startTouchTime;

	// Limit zoomable/pannable image
	protected float[] matrixValues = new float[9];

	protected Scroller scroller;
	protected VelocityTracker velocityTracker;

	protected static final int MIN_FLING_TIME = 250; // 250
	protected int touchSlopSquare;

	protected float zoomX;
	protected float zoomY;
	
	protected Model model;
	protected MapView scheme;
	protected RenderProgram renderer;
	protected RectF modelVisibleRect;
	protected Rect screenRect;
	
	protected int overrenderCount;
	protected static final int MAX_OVERRENDER_COUNT = 1;


    protected int mKeyScrollSpeed = KEY_SCROLL_MIN_SPEED;
    protected long mKeyScrollLastSpeedTime;
    protected int mKeyScrollMode = KEY_SCROLL_MODE_DONE;

    protected static final int KEY_SCROLL_MIN_SPEED = 2;
    protected static final int KEY_SCROLL_MAX_SPEED = 20;
    protected static final int KEY_SCROLL_ACCELERATION_DELAY = 100;
    protected static final int KEY_SCROLL_ACCELERATION_STEP = 2;

    protected static final int KEY_SCROLL_MODE_DONE = 0;
    protected static final int KEY_SCROLL_MODE_DRAG = 1;	
}
