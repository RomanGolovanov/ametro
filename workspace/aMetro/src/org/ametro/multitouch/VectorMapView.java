package org.ametro.multitouch;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.render.RenderElement;
import org.ametro.render.RenderProgram;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;

public class VectorMapView extends View {

	protected static final String TAG = "VectorMapView";

	RenderProgram renderer;
	SchemeView scheme;

	MapCache cache;
	MapCache oldCache;
	
	final Matrix matrix = new Matrix();
	final Matrix invertedMatrix = new Matrix();
	final Matrix renderMatrix = new Matrix();

	final RectF screenRect = new RectF();
	final RectF viewRect = new RectF();
	
	final int memoryClass;
	
	float scale;
	float x;
	float y;
	float scaledWidth;
	float scaledHeight;
	
	float[] values = new float[9];
	
	private boolean updatesEnabled;
	private boolean entireMapCached;
	private final Handler handler = new Handler();
	
	public void enableUpdates(boolean enabled){
		updatesEnabled = enabled;
	}
	
	private int getMemoryClass(Context context){
		try{
			Method getMemoryClassMethod = ActivityManager.class.getMethod("getMemoryClass");
			ActivityManager ac = (ActivityManager)context.getSystemService(Activity.ACTIVITY_SERVICE);
			return (Integer)getMemoryClassMethod.invoke(ac, new Object[]{});
		}catch(Exception ex){
			return 16;
		}
	}
	
	public VectorMapView(Context context, SchemeView scheme) {
		super(context);
		this.scheme = scheme;
		
		
		memoryClass = getMemoryClass(context);

		setScheme(scheme);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateViewRect();
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	public void draw(Canvas canvas) {
		if(cache==null){ // render immediately at first run
			rebuildCache();
		}
		
		// prepare transform matrix
		final Matrix m = renderMatrix;
		if(cache.Scale!=scale){
			// if we're zooming - at first "roll-back" previous cache transform
			m.set(cache.InvertedMatrix);
			// next apply current transformation
			m.postConcat(matrix);
		}else{
			// if we're using cache - simple translate origin 
			m.setTranslate(x - cache.X, y - cache.Y);
		}
		canvas.clipRect(screenRect);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(cache.Image, m, null);
		
		if(updatesEnabled && !entireMapCached){
			//if updated enabled (not zoom) 
			if(!cache.validate(getWidth(), getHeight())){
				postRebuildCache();
			}else if(!cache.hit(viewRect)){
				// and not entire map cached and we aren't in cache viewport
				// or if cache size is invalid
				// request cache update
				postUpdateCache();
			}
		}
		
	}

	/** set transformation matrix for content **/
	public void setMatrix(Matrix newMatrix, boolean invalidate) {
		matrix.set(newMatrix);
		matrix.invert(invertedMatrix);
		matrix.getValues(values);
		scale = values[Matrix.MSCALE_X];
		x = values[Matrix.MTRANS_X];
		y = values[Matrix.MTRANS_Y];
		scaledWidth = getContentWidth() * scale;
		scaledHeight = getContentHeight() * scale;
		
		updateViewRect();
		
		if(invalidate){
			invalidate();
		}
	}

	private void updateViewRect() {
		viewRect.set(0, 0, getWidth(), getHeight());
		invertedMatrix.mapRect(viewRect);
		
		screenRect.set(viewRect);
		matrix.mapRect(screenRect);
	}

	/** get current transformation matrix**/
	public Matrix getMatrix(){
		return this.matrix;
	}
	
	public float getContentWidth() {
		return scheme.width;
	}

	public float getContentHeight() {
		return scheme.height;
	}

	public void rebuildCache() {
		recycleCache();
		try{
			int memoryLimit = 4 * 1024 * 1024 * memoryClass / 16;
			int bitmapSize = (int)scaledWidth * (int)scaledHeight * 2; 
			if( bitmapSize <= memoryLimit ){
				renderEntireCache();
				entireMapCached = true;
				return;
			}
		}catch(OutOfMemoryError ex){
			// eat out-of-memory exception 
		}
		renderPartialCache();
		entireMapCached = false;
	}

	private void renderEntireCache() {
		//Log.w(TAG,"render entire");
		final MapCache newCache = new MapCache();
		final RectF viewRect = new RectF(0,0,scaledWidth,scaledHeight);
		
		Matrix m = new Matrix(matrix);
		m.postTranslate(-x, -y);
		Matrix i = new Matrix();
		m.invert(i);
		
		newCache.InvertedMatrix = i;
		newCache.X = 0;
		newCache.Y = 0;
		newCache.Scale = scale;
		newCache.ViewRect = viewRect;
		
		newCache.Image = Bitmap.createBitmap((int)viewRect.width(), (int)viewRect.height(), Config.RGB_565);
		
		Canvas c = new Canvas(newCache.Image);
		c.drawColor(Color.MAGENTA);
		c.scale(scale, scale);
		
		ArrayList<RenderElement> elements = renderer.setVisibilityAll();
		c.drawColor(Color.WHITE);
		for (RenderElement elem : elements) {
			elem.draw(c);
		}
		
		cache = newCache;
	}

	private void renderPartialCache() {
		//Log.w(TAG,"render partial");
		final int width = getWidth();
		final int height = getHeight();
		final MapCache newCache = new MapCache();
		final RectF viewRect = new RectF(0, 0, width, height);
		
		Matrix i = new Matrix(invertedMatrix);
		i.mapRect(viewRect);
		
		newCache.InvertedMatrix = i;
		newCache.X = x;
		newCache.Y = y;
		newCache.Scale = scale;
		newCache.ViewRect = viewRect;
		
		if(oldCache!=null && oldCache.equals(width, height)){
			newCache.Image = oldCache.Image;
		}else if(oldCache!=null){
			oldCache.Image.recycle();
			oldCache.Image = null;
			oldCache = null;
			System.gc();
		}
		if(newCache.Image==null){
			newCache.Image = Bitmap.createBitmap(width, height, Config.RGB_565);
		}
		
		Canvas c = new Canvas(newCache.Image);
		c.setMatrix(matrix);
		c.clipRect(viewRect);
		ArrayList<RenderElement> elements = renderer.setVisibility(viewRect);
		c.drawColor(Color.WHITE);
		for (RenderElement elem : elements) {
			elem.draw(c);
		}
		
		oldCache = cache;
		cache = newCache;
	}

	private void updatePartialCache() {
		//Log.w(TAG,"update partial");
		final int width = getWidth();
		final int height = getHeight();
		final MapCache newCache = new MapCache();
		final RectF viewRect = new RectF(0, 0, width, height);
		
		Matrix im = new Matrix(invertedMatrix);
		im.mapRect(viewRect);
		
		newCache.InvertedMatrix = im;
		newCache.X = x;
		newCache.Y = y;
		newCache.Scale = scale;
		newCache.ViewRect = viewRect;
		
		if(oldCache!=null && oldCache.equals(width, height)){
			newCache.Image = oldCache.Image;
		}else if(oldCache!=null){
			oldCache.Image.recycle();
			oldCache.Image = null;
			oldCache = null;
			System.gc();
		}
		if(newCache.Image==null){
			newCache.Image = Bitmap.createBitmap(width, height, Config.RGB_565);
		}
		
		Canvas c = new Canvas(newCache.Image);

		RectF viewport = new RectF(viewRect);
		final RectF v = new RectF(viewport);
		final RectF h = new RectF(viewport);
		final RectF i = new RectF(viewport);
		i.intersect(cache.ViewRect);

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

		c.save();
		c.setMatrix(matrix);
		c.clipRect(viewport);
		ArrayList<RenderElement> elements = renderer.setVisibilityTwice(h,v);
		c.drawColor(Color.WHITE);
		for (RenderElement elem : elements) {
			elem.draw(c);
		}
		c.restore();

		c.drawBitmap(cache.Image,newCache.X - cache.X, newCache.Y - cache.Y, null);

		oldCache = cache;
		cache = newCache;
		
		handler.removeCallbacks(renderPartialCacheRunnable);
		handler.postDelayed(renderPartialCacheRunnable, 300);
	}

	
	private Runnable renderPartialCacheRunnable = new Runnable() {
		public void run() {
			renderPartialCache();
			invalidate();
		}
	};
	
	private Runnable rebuildCacheRunnable = new Runnable() {
		public void run() {
			rebuildCache();
			invalidate();
		}
	};
	
	private Runnable updateCacheRunnable = new Runnable() {
		public void run() {
			if(oldCache!=null && oldCache.Scale == scale){
				updatePartialCache();
			}else{
				renderPartialCache();
			}
			invalidate();
		}
	};
	
	public void recycleCache(){
		if(cache!=null){
			cache.Image.recycle();
			cache.Image = null;
			cache = null;
		}
		if(oldCache!=null){
			oldCache.Image.recycle();
			oldCache.Image = null;
			oldCache = null;
		}
		System.gc();
	}

	private static class MapCache
	{
		Matrix InvertedMatrix;
		
		float Scale;
		float X;
		float Y;
		
		RectF ViewRect;
		Bitmap Image;
		
		public boolean validate(int width, int height) {
			return Image.getWidth() == width && Image.getHeight() == height;
		}

		public boolean equals(int width, int height) {
			return Image.getWidth() == width && Image.getHeight() == height;
		}

		public boolean hit(RectF viewRect) {
			return ViewRect.contains(viewRect);
		}
		
	}

	public void setSchemeSelection(ArrayList<StationView> stations, ArrayList<SegmentView> segments, ArrayList<TransferView> transfers) {
		renderer.setSelection(stations, segments, transfers);
		postRebuildCache();
	}

	private void postRebuildCache(){
		handler.removeCallbacks(rebuildCacheRunnable);
		handler.removeCallbacks(renderPartialCacheRunnable);
		handler.removeCallbacks(updateCacheRunnable);
		handler.post(rebuildCacheRunnable);
	}

	private void postUpdateCache() {
		handler.removeCallbacks(rebuildCacheRunnable);
		handler.removeCallbacks(renderPartialCacheRunnable);
		handler.removeCallbacks(updateCacheRunnable);
		handler.post(updateCacheRunnable);
	}

	
	public void setScheme(SchemeView scheme) {
		renderer = new RenderProgram(scheme);
		renderer.setRenderFilter(RenderProgram.ALL);
		renderer.setAntiAlias(true);
		renderer.setSelection(null, null, null);
		
		Matrix m = new Matrix();
		m.setTranslate(1.0f, 1.0f);
		setMatrix(m, false);
		
		postRebuildCache();
	}


}
