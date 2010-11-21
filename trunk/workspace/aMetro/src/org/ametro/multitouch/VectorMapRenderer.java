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
import android.util.Log;
import android.view.View;

public class VectorMapRenderer {

	protected static final String TAG = "VectorMapView";

	RenderProgram mRenderer;
	SchemeView mScheme;

	MapCache mCache;
	MapCache mOldCache;
	
	//protected static final int BORDER = 0;
	
	final Matrix mMatrix = new Matrix();
	final Matrix mInvertedMatrix = new Matrix();
	final Matrix mRenderMatrix = new Matrix();

	final RectF mScreenRect = new RectF();
	final RectF mSchemeRect = new RectF();
	
	final int mMemoryClass;
	
	float mScale;
	float mCurrX;
	float mCurrY;
	float mCurrWidth;
	float mCurrHeight;
	
	View mCanvas;
	
	float[] mMatrixValues = new float[9];
	
	private boolean isUpdatesEnabled;
	private boolean isEntireMapCached;
	
	private final Handler mPrivateHandler = new Handler();
	
	public void setUpdatesEnabled(boolean enabled){
		isUpdatesEnabled = enabled;
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
	
	public int getWidth(){
		return mCanvas.getWidth();
	}
	
	public int getHeight(){
		return mCanvas.getHeight();
	}
	
	public VectorMapRenderer(View container, SchemeView scheme) {
		this.mCanvas = container;
		this.mScheme = scheme;
		mMemoryClass = getMemoryClass(container.getContext());
		setScheme(scheme);
	}

	
	public void draw(Canvas canvas) {
		if(mCache==null){ 
			// render at first run
			rebuildCache();
		}
		// prepare transform matrix
		final Matrix m = mRenderMatrix;
		if(mCache.Scale!=mScale){
			// if we're zooming - at first "roll-back" previous cache transform
			m.set(mCache.InvertedMatrix);
			// next apply current transformation
			m.postConcat(mMatrix);

		}else{
			// if we're using cache - simple translate origin 
			m.setTranslate(mCurrX - mCache.X, mCurrY - mCache.Y);
		}
		canvas.clipRect(mScreenRect);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(mCache.Image, m, null);
		
		if(isUpdatesEnabled){
			//Log.w(TAG, "cache: " + StringUtil.formatRectF(mCache.ViewRect) + " vs. screen: " + StringUtil.formatRectF(mSchemeRect) + ", hit = "+ mCache.hit(mSchemeRect) );
			if(mCache.Scale!=mScale){
				postRebuildCache();
			}else if(!isEntireMapCached && !mCache.hit(mSchemeRect)){
				postUpdateCache();
			}
		}
	}

	/** set transformation matrix for content **/
	public synchronized void setMatrix(Matrix newMatrix) {
		mMatrix.set(newMatrix);
		mMatrix.invert(mInvertedMatrix);
		mMatrix.getValues(mMatrixValues);
		mScale = mMatrixValues[Matrix.MSCALE_X];
		mCurrX = mMatrixValues[Matrix.MTRANS_X];
		mCurrY = mMatrixValues[Matrix.MTRANS_Y];
		mCurrWidth = getContentWidth() * mScale;
		mCurrHeight = getContentHeight() * mScale;
		
		updateViewRect();
	}

	public void updateViewRect() {
		mSchemeRect.set(0, 0, getWidth(), getHeight());
		mInvertedMatrix.mapRect(mSchemeRect);
		mScreenRect.set(mSchemeRect);
		mMatrix.mapRect(mScreenRect);
	}

	/** get current transformation matrix**/
	public Matrix getMatrix(){
		return this.mMatrix;
	}
	
	public float getContentWidth() {
		return mScheme.width;
	}

	public float getContentHeight() {
		return mScheme.height;
	}

	public synchronized void rebuildCache() {
		//Log.w(TAG, "rebuild cache");
		recycleCache();
		try{
			int memoryLimit = 4 * 1024 * 1024 * mMemoryClass / 16;
			int bitmapSize = (int)mCurrWidth * (int)mCurrHeight * 2; 
			if( bitmapSize <= memoryLimit ){
				renderEntireCache();
				isEntireMapCached = true;
				return;
			}else{
				Log.w(TAG,"Not enough memory to make image: memoryLimit = " + memoryLimit + ", bitmapSize=" + bitmapSize );
			}
		}catch(OutOfMemoryError ex){
			// eat out-of-memory exception
			Log.w(TAG,"Not enough memory to make image", ex);
		}
		renderPartialCache();
		isEntireMapCached = false;
	}

	private synchronized void renderEntireCache() {
		//Log.w(TAG,"render entire");
		final MapCache newCache = new MapCache();
		
		final RectF viewRect = new RectF(0,0,mCurrWidth,mCurrHeight);
		
		Matrix m = new Matrix(mMatrix);
		m.postTranslate(-mCurrX, -mCurrY);
		Matrix i = new Matrix();
		m.invert(i);
		
		newCache.InvertedMatrix = i;
		newCache.X = 0;
		newCache.Y = 0;
		newCache.Scale = mScale;
		newCache.ViewRect = viewRect;
		
		newCache.Image = Bitmap.createBitmap((int)viewRect.width(), (int)viewRect.height(), Config.RGB_565);
		
		Canvas c = new Canvas(newCache.Image);
		c.drawColor(Color.MAGENTA);
		c.scale(mScale, mScale);
		
		ArrayList<RenderElement> elements = mRenderer.setVisibilityAll();
		c.drawColor(Color.WHITE);
		for (RenderElement elem : elements) {
			elem.draw(c);
		}
		
		mCache = newCache;
	}

	private synchronized void renderPartialCache() {
		//Log.w(TAG,"render partial");
		final MapCache newCache = MapCache.reuse(mOldCache, getWidth(),getHeight(), mInvertedMatrix, mCurrX, mCurrY, mScale, mSchemeRect);
		
		Canvas c = new Canvas(newCache.Image);
		c.setMatrix(mMatrix);
		c.clipRect(mSchemeRect);
		ArrayList<RenderElement> elements = mRenderer.setVisibility(mSchemeRect);
		c.drawColor(Color.WHITE);
		for (RenderElement elem : elements) {
			elem.draw(c);
		}
		mOldCache = mCache;
		mCache = newCache;
	}

	private synchronized void updatePartialCache() {
		//Log.w(TAG,"update partial");
		MapCache newCache = MapCache.reuse(mOldCache, getWidth(), getHeight(), mInvertedMatrix, mCurrX, mCurrY, mScale, mSchemeRect);
		
		Canvas c = new Canvas(newCache.Image);
		RectF viewport = new RectF(mSchemeRect);
		final RectF v = new RectF(viewport);
		final RectF h = new RectF(viewport);
		final RectF i = new RectF(viewport);
		i.intersect(mCache.ViewRect);
		boolean renderAll = false;

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
			renderAll = true;
		}

		if(renderAll){
			c.setMatrix(mMatrix);
			c.clipRect(mSchemeRect);
			ArrayList<RenderElement> elements = mRenderer.setVisibility(mSchemeRect);
			c.drawColor(Color.WHITE);
			for (RenderElement elem : elements) {
				elem.draw(c);
			}			
		}else{
			c.save();
			c.setMatrix(mMatrix);
			c.clipRect(viewport);
			ArrayList<RenderElement> elements = mRenderer.setVisibilityTwice(h,v);
			c.drawColor(Color.WHITE);
			for (RenderElement elem : elements) {
				elem.draw(c);
			}
			c.restore();
			c.drawBitmap(mCache.Image,newCache.X - mCache.X, newCache.Y - mCache.Y, null);
		}
		

		mOldCache = mCache;
		mCache = newCache;
		
		if(!renderAll){
			mPrivateHandler.removeCallbacks(renderPartialCacheRunnable);
			mPrivateHandler.postDelayed(renderPartialCacheRunnable, 300);
		}
	}

	
	private Runnable renderPartialCacheRunnable = new Runnable() {
		public void run() {
			renderPartialCache();
			mCanvas.invalidate();
		}
	};
	
	private Runnable rebuildCacheRunnable = new Runnable() {
		public void run() {
			rebuildCache();
			mCanvas.invalidate();
		}
	};
	
	private Runnable updateCacheRunnable = new Runnable() {
		public void run() {
			if(mOldCache!=null && mOldCache.Scale == mScale){
				updatePartialCache();
				//mCanvas.invalidate();
			}else{
				renderPartialCache();
				mCanvas.invalidate();
			}
		}
	};
	
	public void recycleCache(){
		if(mCache!=null){
			mCache.Image.recycle();
			mCache.Image = null;
			mCache = null;
		}
		if(mOldCache!=null){
			mOldCache.Image.recycle();
			mOldCache.Image = null;
			mOldCache = null;
		}
		System.gc();
	}

	private static class MapCache
	{
		Matrix InvertedMatrix = new Matrix();
		
		float Scale;
		float X;
		float Y;
		
		RectF ViewRect = new RectF();
		Bitmap Image;

		public boolean equals(int width, int height) {
			return Image.getWidth() == width && Image.getHeight() == height;
		}

		public static MapCache reuse(MapCache oldCache, int width, int height, Matrix matrix, float x, float y, float scale, RectF schemeRect) {
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

			newCache.InvertedMatrix.set(matrix);
			newCache.X = x;
			newCache.Y = y;
			newCache.Scale = scale;
			newCache.ViewRect.set(schemeRect);
			
			return newCache;
		}

		public boolean hit(RectF viewRect) {
			return ViewRect.contains(viewRect);
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

	
	public void setScheme(SchemeView scheme) {
		mRenderer = new RenderProgram(scheme);
		mRenderer.setRenderFilter(RenderProgram.ALL);
		mRenderer.setAntiAlias(true);
		mRenderer.setSelection(null, null, null);
		
		Matrix m = new Matrix();
		m.setTranslate(1.0f, 1.0f);
		setMatrix(m);

		recycleCache();
	}

	public void setSchemeSelection(ArrayList<StationView> stations, ArrayList<SegmentView> segments, ArrayList<TransferView> transfers) {
		mRenderer.setSelection(stations, segments, transfers);
		
		recycleCache();
	}

	public boolean isUpdatesEnabled() {
		return isUpdatesEnabled;
	}

	public void setAntiAlias(boolean enabled) {
		//mRenderer.setAntiAlias(enabled);
	}


}
