package org.ametro.render;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;

import org.ametro.model.entities.MapScheme;
import org.ametro.render.elements.DrawingElement;

import java.util.List;

public class CanvasRenderer {

	private RenderProgram renderProgram;
    private MapScheme mapScheme;

	private MapCache cache;
	private MapCache oldCache;

	private final Matrix matrix = new Matrix();
	private final Matrix mInvertedMatrix = new Matrix();
	private final Matrix renderMatrix = new Matrix();

	private final RectF screenRect = new RectF();
	private final RectF schemeRect = new RectF();

	private final RectF renderViewPort = new RectF();
	private final RectF renderViewPortVertical = new RectF();
	private final RectF renderViewPortHorizontal = new RectF();
	private final RectF renderViewPortIntersection = new RectF();

	private final int memoryClass;
    private int maximumBitmapWidth;
    private int maximumBitmapHeight;

	private float scale;
	private float currentX;
	private float currentY;
	private float currentWidth;
	private float currentHeight;

	private View canvasView;

	private final float[] matrixValues = new float[9];

	private boolean isRenderFailed = false;
	private boolean isUpdatesEnabled;
	private boolean isEntireMapCached;

	private final Handler mPrivateHandler = new Handler();

	public CanvasRenderer(View container, MapScheme scheme, RenderProgram renderProgram) {

		this.canvasView = container;
		this.mapScheme = scheme;

        ActivityManager ac = (ActivityManager)container.getContext().getSystemService(Activity.ACTIVITY_SERVICE);
        memoryClass = ac.getMemoryClass();
		setScheme(renderProgram);
	}

    public void setScheme(RenderProgram renderProgram) {
		this.renderProgram = renderProgram;

		Matrix m = new Matrix();
		m.setTranslate(1.0f, 1.0f);
		setMatrix(m);

		recycleCache();
	}

	public void setUpdatesEnabled(boolean enabled){
		isUpdatesEnabled = enabled;
	}

	public boolean draw(Canvas canvas) {

        maximumBitmapWidth = canvas.getMaximumBitmapWidth();
        maximumBitmapHeight = canvas.getMaximumBitmapHeight();

		//Log.d(TAG,"draw map");
		if(cache ==null){
			// render at first run
			rebuildCache();
		}
		if(isRenderFailed){
			return false;
		}
		// prepare transform matrix
		final Matrix m = renderMatrix;
		if(cache.Scale!= scale){
			// if we're zooming - at first "roll-back" previous cache transform
			m.set(cache.InvertedMatrix);
			// next apply current transformation
			m.postConcat(matrix);

		}else{
			// if we're using cache - simple translate origin
			m.setTranslate(currentX - cache.X, currentY - cache.Y);
		}
		canvas.clipRect(screenRect);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(cache.Image, m, null);

		if(isUpdatesEnabled){
			//Log.w(TAG, "cache: " + StringUtil.formatRectF(cache.ViewRect) + " vs. screen: " + StringUtil.formatRectF(schemeRect) + ", hit = "+ cache.hit(schemeRect) );
			if(cache.Scale!= scale){
				postRebuildCache();
			}else if(!isEntireMapCached && !cache.hit(schemeRect)){
				postUpdateCache();
			}
		}

        return !isRenderFailed;
	}

	/** set transformation matrix for content **/
	public synchronized void setMatrix(Matrix newMatrix) {
		matrix.set(newMatrix);
		matrix.invert(mInvertedMatrix);
		matrix.getValues(matrixValues);
		scale = matrixValues[Matrix.MSCALE_X];
		currentX = matrixValues[Matrix.MTRANS_X];
		currentY = matrixValues[Matrix.MTRANS_Y];
		currentWidth = mapScheme.getWidth() * scale;
		currentHeight = mapScheme.getHeight() * scale;

		updateViewRect();

		isRenderFailed = false;
	}

	public void updateViewRect() {
		schemeRect.set(0, 0, canvasView.getWidth(), canvasView.getHeight());
		mInvertedMatrix.mapRect(schemeRect);
		screenRect.set(schemeRect);
		matrix.mapRect(screenRect);
	}

	public synchronized void rebuildCache() {
		recycleCache();
        isEntireMapCached = false;
        if(currentWidth>maximumBitmapWidth || currentHeight > maximumBitmapHeight){
            renderPartialCache();
            return;
        }

        int memoryLimit = 4 * 1024 * 1024 * memoryClass / 16;
        int bitmapSize = (int) currentWidth * (int) currentHeight * 2;
        if( bitmapSize > memoryLimit ) {
            renderPartialCache();
            return;
        }
		try{
            renderEntireCache();
            isEntireMapCached = true;
        }catch(OutOfMemoryError ex){
            recycleCache();
            renderPartialCache();
        }
	}

	private synchronized void renderEntireCache() {
		try{
			//Log.w(TAG,"render entire");
			final RectF viewRect = new RectF(0,0, currentWidth, currentHeight);
			Matrix m = new Matrix(matrix);
			m.postTranslate(-currentX, -currentY);
			Matrix i = new Matrix();
			m.invert(i);

			final MapCache newCache = MapCache.reuse(
                    oldCache,
					(int) currentWidth,
					(int) currentHeight,
					m,
					i,
					0,
					0,
                    scale,
					viewRect);

			Canvas c = new Canvas(newCache.Image);
			c.drawColor(Color.WHITE);
			c.setMatrix(newCache.CacheMatrix);

			List<DrawingElement> elements = renderProgram.getAllDrawingElements();
			c.drawColor(Color.WHITE);
			for (DrawingElement elem : elements) {
				elem.draw(c);
			}

			cache = newCache;
		}catch(Exception ex){
			isRenderFailed = true;
		}

	}

	private synchronized void renderPartialCache() {
		try{
			//Log.w(TAG,"render partial");
			final MapCache newCache = MapCache.reuse(
                    oldCache,
                    canvasView.getWidth(),
                    canvasView.getHeight(),
                    matrix,
                    mInvertedMatrix,
                    currentX,
                    currentY,
                    scale,
                    schemeRect);

			Canvas c = new Canvas(newCache.Image);
			c.setMatrix(newCache.CacheMatrix);
			c.clipRect(newCache.SchemeRect);
			List<DrawingElement> elements = renderProgram.getClippedDrawingElements(newCache.SchemeRect);
			c.drawColor(Color.WHITE);
			for (DrawingElement elem : elements) {
				elem.draw(c);
			}
			oldCache = cache;
			cache = newCache;
		}catch(Exception ex){
            //Log.w(TAG,"render partial failed", ex);
			isRenderFailed = true;
		}
	}

	private synchronized void updatePartialCache() {
		try{
			//Log.w(TAG,"update partial");
			MapCache newCache = MapCache.reuse(
                    oldCache,
                    canvasView.getWidth(),
                    canvasView.getHeight(),
                    matrix,
                    mInvertedMatrix,
                    currentX,
                    currentY,
                    scale,
                    schemeRect);

			Canvas c = new Canvas(newCache.Image);

			boolean renderAll = splitRenderViewPort(newCache.SchemeRect, cache.SchemeRect);
			if(renderAll){
				c.setMatrix(newCache.CacheMatrix);
				c.clipRect(newCache.SchemeRect);
				List<DrawingElement> elements = renderProgram.getClippedDrawingElements(newCache.SchemeRect);
				c.drawColor(Color.WHITE);
				for (DrawingElement elem : elements) {
					elem.draw(c);
				}
			}else{
				c.save();
				c.setMatrix(newCache.CacheMatrix);
				c.clipRect(newCache.SchemeRect);
                List<DrawingElement> elements = renderProgram.getClippedDrawingElements(renderViewPortHorizontal, renderViewPortVertical);
				c.drawColor(Color.WHITE);
				for (DrawingElement elem : elements) {
					elem.draw(c);
				}
				c.restore();
				c.drawBitmap(cache.Image,newCache.X - cache.X, newCache.Y - cache.Y, null);
			}

			oldCache = cache;
			cache = newCache;

			if(!renderAll){
				mPrivateHandler.removeCallbacks(renderPartialCacheRunnable);
				mPrivateHandler.postDelayed(renderPartialCacheRunnable, 300);
			}
		}catch(Exception ex){
			isRenderFailed = true;
		}
	}

	private boolean splitRenderViewPort(RectF schemeRect, RectF cacheRect) {
		final RectF vp = renderViewPort;
		final RectF v = renderViewPortVertical;
		final RectF h = renderViewPortHorizontal;
		final RectF i = renderViewPortIntersection;
		vp.set(schemeRect);
		renderViewPortVertical.set(vp);
		renderViewPortHorizontal.set(vp);
		renderViewPortIntersection.set(vp);
		renderViewPortIntersection.intersect(cacheRect);
		boolean renderAll = false;

		if (vp.right == i.right && vp.bottom == i.bottom) {
			h.bottom = i.top;
			v.right = i.left;
		} else if (vp.right == i.right && vp.top == i.top) {
			h.top = i.bottom;
			v.right = i.left;
		} else if (vp.left == i.left && vp.bottom == i.bottom) {
			h.bottom = i.top;
			v.left = i.right;
		} else if (vp.left == i.left && vp.top == i.top) {
			h.top = i.bottom;
			v.left = i.right;
		} else {
			renderAll = true;
		}
		return renderAll;
	}


	private Runnable renderPartialCacheRunnable = new Runnable() {
		public void run() {
			renderPartialCache();
			canvasView.invalidate();
		}
	};

	private Runnable rebuildCacheRunnable = new Runnable() {
		public void run() {
			rebuildCache();
			canvasView.invalidate();
		}
	};

	private Runnable updateCacheRunnable = new Runnable() {
		public void run() {
			if(oldCache !=null && oldCache.Scale == scale){
				updatePartialCache();
				//canvasView.invalidate();
			}else{
				renderPartialCache();
				canvasView.invalidate();
			}
		}
	};

	public void recycleCache(){
		if(cache !=null){
			cache.Image.recycle();
			cache.Image = null;
			cache = null;
		}
		if(oldCache !=null){
			oldCache.Image.recycle();
			oldCache.Image = null;
			oldCache = null;
		}
		System.gc();
	}

	private static class MapCache
	{
		Matrix CacheMatrix = new Matrix();
		Matrix InvertedMatrix = new Matrix();

		float Scale;
		float X;
		float Y;

		RectF SchemeRect = new RectF();
		Bitmap Image;

		public boolean equals(int width, int height) {
			return Image.getWidth() == width && Image.getHeight() == height;
		}

		public static MapCache reuse(MapCache oldCache, int width, int height, Matrix matrix, Matrix invertedMatrix, float x, float y, float scale, RectF schemeRect) {
			MapCache newCache;

			if(oldCache!=null){
				newCache = oldCache;
				if(!newCache.equals(width, height)){
					newCache.Image.recycle();
					newCache.Image = null;
					System.gc();
				}
			}else{
				newCache = new MapCache();
			}
			if(newCache.Image==null){
				newCache.Image = Bitmap.createBitmap(width, height, Config.RGB_565);
			}

			newCache.CacheMatrix.set(matrix);
			newCache.InvertedMatrix.set(invertedMatrix);
			newCache.X = x;
			newCache.Y = y;
			newCache.Scale = scale;
			newCache.SchemeRect.set(schemeRect);

			return newCache;
		}

		public boolean hit(RectF viewRect) {
			return SchemeRect.contains(viewRect);
		}
	}

	private void postRebuildCache(){
		mPrivateHandler.removeCallbacks(rebuildCacheRunnable);
		mPrivateHandler.removeCallbacks(renderPartialCacheRunnable);
		mPrivateHandler.removeCallbacks(updateCacheRunnable);
		mPrivateHandler.post(rebuildCacheRunnable);
	}

	private void postUpdateCache() {
		mPrivateHandler.removeCallbacks(rebuildCacheRunnable);
		mPrivateHandler.removeCallbacks(renderPartialCacheRunnable);
		mPrivateHandler.removeCallbacks(updateCacheRunnable);
		mPrivateHandler.post(updateCacheRunnable);
	}

	public void onAttachedToWindow() {
		// do nothing
	}

	public void onDetachedFromWindow() {
		// do nothing
	}
}
