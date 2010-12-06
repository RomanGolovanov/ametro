package org.ametro.render;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;

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

public class VectorMapRenderer implements IVectorMapRenderer {

	protected static final String TAG = "VectorMapView";

	private RenderProgram mRenderer;
	private SchemeView mScheme;

	private MapCache mCache;
	private MapCache mOldCache;
	
	private final Matrix mMatrix = new Matrix();
	private final Matrix mInvertedMatrix = new Matrix();
	private final Matrix mRenderMatrix = new Matrix();

	private final RectF mScreenRect = new RectF();
	private final RectF mSchemeRect = new RectF();
	
	private final RectF mRenderViewPort = new RectF();
	private final RectF mRenderViewPortVertical = new RectF();
	private final RectF mRenderViewPortHorizontal = new RectF();
	private final RectF mRenderViewPortIntersection = new RectF();
	
	private final int mMemoryClass;
	
	private float mScale;
	private float mCurrX;
	private float mCurrY;
	private float mCurrWidth;
	private float mCurrHeight;
	
	private View mCanvas;
	
	private boolean mAntiAliasEnabled;
//	private boolean mAntiAliasDisabledOnChanges;
	private boolean mAntiAliasCurrentState;
	
	private final float[] mMatrixValues = new float[9];
	
	private boolean mIsRenderFailed = false;
	private boolean isUpdatesEnabled;
	private boolean isEntireMapCached;
	
	private final Handler mPrivateHandler = new Handler();

	
	public VectorMapRenderer(View container, SchemeView scheme, RenderProgram renderProgram) {
		this.mCanvas = container;
		this.mScheme = scheme;
		mMemoryClass = getMemoryClass(container.getContext());
		setScheme(scheme, renderProgram);
	}

	public boolean isRenderFailed(){
		synchronized (this) {
			return mIsRenderFailed;
		}
	}
	
	public void setScheme(SchemeView scheme, RenderProgram renderProgram) {
		mRenderer = renderProgram;
		mRenderer.setRenderFilter(RenderProgram.ALL);
		mRenderer.setAntiAlias(mAntiAliasEnabled);
		mAntiAliasCurrentState = mAntiAliasEnabled;
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

	public void setUpdatesEnabled(boolean enabled){
		isUpdatesEnabled = enabled;
	}	
	
	public boolean isUpdatesEnabled() {
		return isUpdatesEnabled;
	}

	public void setAntiAliasEnabled(boolean enabled) {
		mAntiAliasEnabled = enabled;
	}

	public void draw(Canvas canvas) {
		//Log.d(TAG,"draw map");
		if(mCache==null){ 
			// render at first run
			rebuildCache();
		}
		if(mIsRenderFailed){
			return;
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
		mCurrWidth = mScheme.width * mScale;
		mCurrHeight = mScheme.height * mScale;
		
		updateViewRect();

		mIsRenderFailed = false;
	}

	public void updateViewRect() {
		mSchemeRect.set(0, 0, mCanvas.getWidth(), mCanvas.getHeight());
		mInvertedMatrix.mapRect(mSchemeRect);
		mScreenRect.set(mSchemeRect);
		mMatrix.mapRect(mScreenRect);
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
		try{
			//Log.w(TAG,"render entire");
			final RectF viewRect = new RectF(0,0,mCurrWidth,mCurrHeight);
			Matrix m = new Matrix(mMatrix);
			m.postTranslate(-mCurrX, -mCurrY);
			Matrix i = new Matrix();
			m.invert(i);
			
			final MapCache newCache = MapCache.reuse(
					mOldCache, 
					(int)mCurrWidth, 
					(int)mCurrHeight, 
					m, 
					i, 
					0, 
					0, 
					mScale, 
					viewRect);
			
			if( !mAntiAliasCurrentState && mAntiAliasEnabled ){
				mRenderer.setAntiAlias(true);
				mAntiAliasCurrentState = true;
			}
			
			Canvas c = new Canvas(newCache.Image);
			c.drawColor(Color.MAGENTA);
			c.setMatrix(newCache.CacheMatrix);
			
			ArrayList<RenderElement> elements = mRenderer.setVisibilityAll();
			c.drawColor(Color.WHITE);
			for (RenderElement elem : elements) {
				elem.draw(c);
			}
			
			mCache = newCache;
		}catch(Exception ex){
			mIsRenderFailed = true;
		}

	}

	private synchronized void renderPartialCache() {
		try{
			//Log.w(TAG,"render partial");
			final MapCache newCache = MapCache.reuse(
					mOldCache, 
					mCanvas.getWidth(), 
					mCanvas.getHeight(), 
					mMatrix, 
					mInvertedMatrix, 
					mCurrX, 
					mCurrY, 
					mScale, 
					mSchemeRect);
			
			if( !mAntiAliasCurrentState && mAntiAliasEnabled ){
				mRenderer.setAntiAlias(true);
				mAntiAliasCurrentState = true;
			}
			
			Canvas c = new Canvas(newCache.Image);
			c.setMatrix(newCache.CacheMatrix);
			c.clipRect(newCache.SchemeRect);
			ArrayList<RenderElement> elements = mRenderer.setVisibility(newCache.SchemeRect);
			c.drawColor(Color.WHITE);
			for (RenderElement elem : elements) {
				elem.draw(c);
			}
			mOldCache = mCache;
			mCache = newCache;		
		}catch(Exception ex){
			mIsRenderFailed = true;
		}
	}

	private synchronized void updatePartialCache() {
		try{
			//Log.w(TAG,"update partial");
			MapCache newCache = MapCache.reuse(
					mOldCache, 
					mCanvas.getWidth(), 
					mCanvas.getHeight(), 
					mMatrix, 
					mInvertedMatrix, 
					mCurrX, 
					mCurrY, 
					mScale, 
					mSchemeRect);
			
			Canvas c = new Canvas(newCache.Image);
			
			boolean renderAll = splitRenderViewPort(newCache.SchemeRect, mCache.SchemeRect);
			if(renderAll){
				if( !mAntiAliasCurrentState && mAntiAliasEnabled ){
					mRenderer.setAntiAlias(true);
					mAntiAliasCurrentState = true;
				}
				
				c.setMatrix(newCache.CacheMatrix);
				c.clipRect(newCache.SchemeRect);
				ArrayList<RenderElement> elements = mRenderer.setVisibility(newCache.SchemeRect);
				c.drawColor(Color.WHITE);
				for (RenderElement elem : elements) {
					elem.draw(c);
				}			
			}else{
				c.save();
				c.setMatrix(newCache.CacheMatrix);
				c.clipRect(newCache.SchemeRect);
				ArrayList<RenderElement> elements = mRenderer.setVisibilityTwice(mRenderViewPortHorizontal,mRenderViewPortVertical);
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
		}catch(Exception ex){
			mIsRenderFailed = true;
		}
	}

	private boolean splitRenderViewPort(RectF schemeRect, RectF cacheRect) {
		final RectF vp = mRenderViewPort;
		final RectF v = mRenderViewPortVertical;
		final RectF h = mRenderViewPortHorizontal;
		final RectF i = mRenderViewPortIntersection;
		vp.set(schemeRect);
		mRenderViewPortVertical.set(vp);
		mRenderViewPortHorizontal.set(vp);
		mRenderViewPortIntersection.set(vp);
		mRenderViewPortIntersection.intersect(cacheRect);
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
	
	private int getMemoryClass(Context context){
		try{
			Method getMemoryClassMethod = ActivityManager.class.getMethod("getMemoryClass");
			ActivityManager ac = (ActivityManager)context.getSystemService(Activity.ACTIVITY_SERVICE);
			return (Integer)getMemoryClassMethod.invoke(ac, new Object[]{});
		}catch(Exception ex){
			return 16;
		}
	}

	public void onAttachedToWindow() {
		// do nothing
	}

	public void onDetachedFromWindow() {
		// do nothing
	}	
}
