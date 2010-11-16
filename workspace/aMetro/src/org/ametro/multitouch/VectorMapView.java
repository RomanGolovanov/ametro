package org.ametro.multitouch;

import java.util.ArrayList;

import org.ametro.model.MapView;
import org.ametro.render.RenderElement;
import org.ametro.render.RenderProgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.View;


public class VectorMapView extends View {

	protected static final String TAG = "VectorMapView";

	RenderProgram renderer;
	MapView scheme;
	
	Matrix matrix = new Matrix();
	Matrix invertedMatrix = new Matrix();
	
	MapCache cache;
	MapCache oldCache;
	Matrix renderMatrix = new Matrix();

	RectF screenRect = new RectF();
	RectF viewRect = new RectF();
	
	float scale;
	float x;
	float y;
	float scaledWidth;
	float scaledHeight;
	
	float[] values = new float[9];
	
	private boolean updatesEnabled;
	private boolean entireMapCached;
	private Handler handler = new Handler();
	
	public void enableUpdates(boolean enabled){
		updatesEnabled = enabled;
	}
	
	public VectorMapView(Context context, MapView scheme) {
		super(context);
		this.scheme = scheme;
		
		renderer = new RenderProgram(scheme);
		renderer.setRenderFilter(RenderProgram.ALL);
		renderer.setAntiAlias(true);
		renderer.updateSelection(null, null, null);
		
		Matrix m = new Matrix();
		m.setTranslate(1.0f, 1.0f);
		setMatrix(m, false);
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
				handler.removeCallbacks(rebuildCacheRunnable);
				handler.removeCallbacks(updateCacheRunnable);
				handler.post(rebuildCacheRunnable);
			}else if(!cache.hit(viewRect)){
				// and not entire map cached and we aren't in cache viewport
				// or if cache size is invalid
				// request cache update
				handler.removeCallbacks(rebuildCacheRunnable);
				handler.removeCallbacks(updateCacheRunnable);
				handler.post(updateCacheRunnable);
			}
		}
		
	}

	/** set transformation matrix for content **/
	public void setMatrix(Matrix newMatrix, boolean invalidate) {
		matrix = newMatrix;
		matrix.invert(invertedMatrix);
		matrix.getValues(values);
		scale = values[Matrix.MSCALE_X];
		x = values[Matrix.MTRANS_X];
		y = values[Matrix.MTRANS_Y];
		scaledWidth = getContentWidth() * scale;
		scaledHeight = getContentHeight() * scale;
		
		viewRect.set(0, 0, getWidth(), getHeight());
		invertedMatrix.mapRect(viewRect);
		
		screenRect.set(viewRect);
		matrix.mapRect(screenRect);
		
		if(invalidate){
			invalidate();
		}
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
			renderEntireCache();
			entireMapCached = true;
		}catch(OutOfMemoryError ex){
			renderPartialCache();
			entireMapCached = false;
		}
	}

	private void renderEntireCache() {
		Log.w(TAG,"render entire");
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
		Log.w(TAG,"render partial");
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
		Log.w(TAG,"update partial");
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
			cache = null;
		}
		if(oldCache!=null){
			oldCache.Image.recycle();
			oldCache = null;
		}
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
			return Image.getWidth() >= width && Image.getHeight() >= height;
		}

		public boolean equals(int width, int height) {
			return Image.getWidth() == width && Image.getHeight() == height;
		}

		public boolean hit(RectF viewRect) {
			return ViewRect.contains(viewRect);
		}
		
	}
	
}
