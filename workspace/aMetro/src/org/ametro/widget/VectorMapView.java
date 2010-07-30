/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.widget;

import java.util.List;

import org.ametro.Constants;
import org.ametro.model.MapView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.render.RenderProgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ZoomControls;

public class VectorMapView extends BaseMapView{

	public static final float[] ZOOMS = new float[]{1.5f, 1.0f, 0.8f, 0.6f, 0.4f, 0.3f, 0.2f, 0.1f};
	public static final int[] STEPS = new int[]{15, 10, 8, 6, 4, 3, 2, 1};
	public static final int[] FILTERS = new int[]{
			RenderProgram.ALL,
			RenderProgram.ALL,
			RenderProgram.ALL,
			RenderProgram.ALL,
			RenderProgram.ALL,
			RenderProgram.ALL,
			RenderProgram.ALL,
			RenderProgram.ALL,
	};

	public static final int MIN_ZOOM_LEVEL = 0;
	public static final int MAX_ZOOM_LEVEL = 7;
	public static final int DEFAULT_ZOOM_LEVEL = 1;
	public static final int ZOOM_CONTROLS_TIMEOUT = 2000;
	
	private int mZoom = DEFAULT_ZOOM_LEVEL;

	private ZoomControls mZoomControls;
	
	public void setZoomControls(ZoomControls zoomControls) {
		mZoomControls = zoomControls;
		mZoomControls.setVisibility(View.INVISIBLE);
		
		mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onZoomIn();
			}
		});
		mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onZoomOut();
			}
		});
	}

	public int getModelZoom() {
		return mZoom;
	}
	
	public void setModelZoom(int zoom){
		mZoom = zoom;
	}
	
	private Runnable mZoomControlRunnable = new Runnable() {
		public void run() {
			if (!mZoomControls.hasFocus()) {
				hideZoom();
			} else {
				delayZoom();
			}
		}
	};
	
	public boolean performClick() {
		if (mZoomControls.getVisibility() != View.VISIBLE) {
			showZoom();
		}
		delayZoom();
		return super.performClick();
	};
	
	public boolean performMove(int x, int y, int nx, int ny){
		if (mZoomControls.getVisibility() != View.VISIBLE) {
			showZoom();
		}
		delayZoom();
		return super.performMove(x, y, nx, ny);
	}

	private void onZoomIn() {
		setZoom(mZoom - 1);
	}

	private void onZoomOut() {
		setZoom(mZoom + 1);
	}

	public void setZoom(int zoom) {
		int newZoom = Math.min(Math.max(zoom, MIN_ZOOM_LEVEL), MAX_ZOOM_LEVEL);
		boolean disableZoomOut = true;
		if(newZoom<MAX_ZOOM_LEVEL){
			final float newScale = ZOOMS[newZoom];
			final int widgetWidth = getWidth();
			final int widgetHeight = getHeight();
			final int width = (int) Math.ceil(mMapView.width * newScale);
			final int height = (int) Math.ceil(mMapView.height * newScale);
			disableZoomOut = width<=widgetWidth && height<=widgetHeight;
		}
		mZoom = newZoom;
		mZoomControls.setIsZoomInEnabled(mZoom > MIN_ZOOM_LEVEL);
		mZoomControls.setIsZoomOutEnabled(!disableZoomOut);
		setRenderFilter(FILTERS[mZoom]);
		setScale(ZOOMS[mZoom], STEPS[mZoom]);
		clearRenderFailed();
	}

	private void delayZoom() {
		mPrivateHandler.removeCallbacks(mZoomControlRunnable);
		mPrivateHandler.postDelayed(mZoomControlRunnable, ZOOM_CONTROLS_TIMEOUT);
	}

	public void showZoom() {
		fadeZoom(View.VISIBLE, 0.0f, 1.0f);
	}

	public void hideZoom() {
		fadeZoom(View.INVISIBLE, 1.0f, 0.0f);
	}

	private void fadeZoom(int visibility, float startAlpha, float endAlpha) {
		AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
		anim.setDuration(500);
		mZoomControls.startAnimation(anim);
		mZoomControls.setVisibility(visibility);
	}
	
	
	
	public VectorMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VectorMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VectorMapView(Context context) {
		super(context);
	}

	public void setRenderFilter(int renderFilter) {
		mRenderProgram.setRenderFilter(renderFilter);
	}    

	public void setModel(MapView map) {
		if (map != null) {
			mMapView = map;
			mRenderProgram = new RenderProgram(map);
			calculateDimensions();
			setInitialized(true);
		} else {
			setInitialized(false);
			mRenderProgram = null;
			mMapView = null;
		}
	}

	public void updateModel(){
		mRenderProgram = new RenderProgram(mMapView);
	}

	public void setModelSelection(List<StationView> stations, List<SegmentView> segments, List<TransferView> transfers){
		mRenderProgram.updateSelection(stations, segments, transfers);
		destroyTileCache();
	}

	public void setScale(float scale, int step) {
		PointF p = getModelScrollCenter();

		mScale = scale;
		mTileSize = (int) (step / scale);
		calculateDimensions();

		setModelScrollCenter(p);
		postInvalidate();
	}

	public void setModelScrollCenter(float x, float y) {
		Point p0 = new Point();
		p0.x = (int) (x * mScale);
		p0.y = (int) (y * mScale);
		super.setScrollCenter(p0);
	}

	public void scrollModelCenterTo(int x, int y) {
		Point p0 = new Point();
		p0.x = (int) (x * mScale);
		p0.y = (int) (y * mScale);
		super.scrollCenterTo(p0);
	}

	public void setModelScrollCenter(PointF p) {
		setModelScrollCenter(p.x, p.y);
	}

	public PointF getModelScrollCenter() {
		Point p = super.getScrollCenter();
		float x = p.x / mScale;
		float y = p.y / mScale;
		return new PointF(x, y);
	}

	protected int getContentHeight() {
		return mContentHeight;
	}

	protected int getContentWidth() {
		return mContentWidth;
	}

	protected void onDrawRect(Canvas canvas, Rect viewport) {
		invalidateTileCache(viewport, false);
		final Rect tileOuter = screenToOuterTileRect(viewport);
		Rect cache = mTileCacheRect;

		if (!Rect.intersects(tileOuter, cache) || mForceCacheInvalidate) {
			mForceCacheInvalidate = false;
			// redraw entire page
			synchronized(sync){			
				mCreateTileCacheRect = new Rect(viewport);
				mUpdateTileCacheRect = null;
				mPrivateHandler.removeCallbacks(mCreateTileCacheRunnable);
				mPrivateHandler.removeCallbacks(mUpdateTileCacheRunnable);
				mPrivateHandler.post(mCreateTileCacheRunnable);
			}
		} else if (!cache.contains(tileOuter)) {
			// redraw part of page
			synchronized(sync){
				mCreateTileCacheRect = null;
				mUpdateTileCacheRect = new Rect(viewport);
				mPrivateHandler.removeCallbacks(mCreateTileCacheRunnable);
				mPrivateHandler.removeCallbacks(mUpdateTileCacheRunnable);
				mPrivateHandler.post(mUpdateTileCacheRunnable);
			}
		}
		synchronized(sync){
			cache = mTileCacheRect;
			int dx = cache.left * mTileSize - viewport.left;
			int dy = cache.top * mTileSize - viewport.top;
			//canvas.drawColor(Color.WHITE);
			canvas.drawBitmap(mTileCache, dx, dy, null);
		}
	}

	private void updateTileCache(Rect screenCoords) {
		if(mTileCacheBuffer == null) return;
		try{
			final Rect tileOuter = screenToOuterTileRect(screenCoords);
			RectF modelOuter = tileToModelRect(tileOuter);
	
			Rect entireCache;
			Bitmap cacheImage;
			synchronized(sync){
				entireCache = mTileCacheRect;
				cacheImage = mTileCache;
			}
	
			final Rect cache = new Rect(entireCache);
			cache.intersect(tileOuter);
	
			final Rect dst = new Rect(cache); // control canvas position
			dst.offsetTo(cache.left - tileOuter.left, cache.top - tileOuter.top);
			final Rect dstOnScreen = tileToScreenRect(dst);
	
			final Rect src = new Rect(cache); // cache canvas position
			src.offsetTo(cache.left - entireCache.left, cache.top - entireCache.top);
			final Rect srcOnScreen = tileToScreenRect(src);
	
			final Rect verticalSpan = new Rect(tileOuter);
			final Rect horizontalSpan = new Rect(tileOuter);
	
			if (tileOuter.right == cache.right && tileOuter.bottom == cache.bottom) {
				horizontalSpan.bottom = cache.top;
				verticalSpan.right = cache.left;
			} else if (tileOuter.right == cache.right && tileOuter.top == cache.top) {
				horizontalSpan.top = cache.bottom;
				verticalSpan.right = cache.left;
			} else if (tileOuter.left == cache.left && tileOuter.bottom == cache.bottom) {
				horizontalSpan.bottom = cache.top;
				verticalSpan.left = cache.right;
			} else if (tileOuter.left == cache.left && tileOuter.top == cache.top) {
				horizontalSpan.top = cache.bottom;
				verticalSpan.left = cache.right;
			} else {
				throw new RuntimeException("Invalid viewport splitting algorithm");
			}
	
			final RectF horizontalSpanInModel = tileToModelRect(horizontalSpan);
			final RectF verticalSpanInModel = tileToModelRect(verticalSpan);
	
			final Canvas canvas = new Canvas(mTileCacheBuffer);
			canvas.drawColor(Color.WHITE);
	
			canvas.save();
			canvas.scale(mScale, mScale);
			canvas.translate(-modelOuter.left, -modelOuter.top);
			if(screenCoords != mUpdateTileCacheRect) return;
	
			mRenderProgram.setVisibilityTwice(horizontalSpanInModel, verticalSpanInModel);
			if(screenCoords != mUpdateTileCacheRect) return;
	
			mRenderProgram.draw(canvas);
			if(screenCoords != mUpdateTileCacheRect) return;
	
			canvas.restore();
	
			canvas.save();
			canvas.clipRect(dstOnScreen);
			canvas.drawBitmap(cacheImage, srcOnScreen, dstOnScreen, null);
			canvas.restore();
	
			synchronized (sync) {
				if(screenCoords == mUpdateTileCacheRect){
					mTileCacheRect = tileOuter;
					Bitmap swap = mTileCache;
					mTileCache = mTileCacheBuffer;
					mTileCacheBuffer = swap;
				}
			}
		}catch(Exception ex){
			setRenderFailed(ex);
		}
	}

	private boolean isCacheEntireImage(){
		int imageSize = getContentWidth() * getContentHeight() * 2;
		return imageSize < mEntireCacheLimits && !mDisableEntireImageCaching;
	}

	private void invalidateTileCache(Rect screenCoords, boolean force) {
		try{
			if(mScale < mTileCacheScale || isCacheEntireImage()){
				mDisableEntireImageCaching = false;
				int width = getContentWidth();
				int height = getContentHeight();
				final boolean isViewportChanged = mTileCacheScale != mScale || mTileCacheWidth != width || mTileCacheHeight != height;
				if(mTileCache == null || isViewportChanged || force){
					destroyTileCache();

					mTileCache = Bitmap.createBitmap(width, height, Config.RGB_565);
					mTileCacheBuffer = null;

					final Canvas canvas = new Canvas(mTileCache);
					canvas.drawColor(Color.WHITE);
					canvas.clipRect(0, 0, width, height);
					canvas.scale(mScale, mScale);
					//canvas.translate(-modelOuter.left, -modelOuter.top);
					if(mAntiAliasEnabled && !mAntiAlias){
						mAntiAlias = true;
						mUpdatedAntiAlias = true;
						mRenderProgram.setAntiAlias(mAntiAlias);
					}
					mRenderProgram.setVisibilityAll();
					mRenderProgram.draw(canvas);

					synchronized(sync){
						mTileCacheRect = new Rect(0,0,width, height);
						mTileCacheScale = mScale;
						mTileCacheWidth = mTileCacheRect.width();
						mTileCacheHeight = mTileCacheRect.height();
					}		
				}
				return;
			}
		}catch(OutOfMemoryError e){
			mDisableEntireImageCaching = true;
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN, "Disable entire map caching limits due out of memory");
			}
		}
		final Rect tileOuter = screenToOuterTileRect(screenCoords);
		final int width = getWidth() + mTileSize * 2;
		final int height = getHeight() + mTileSize * 2;
		final boolean isViewportChanged = mTileCacheScale != mScale || mTileCacheWidth != width || mTileCacheHeight != height;
		if (mTileCache == null || isViewportChanged || force) {
			if (isViewportChanged) {
				destroyTileCache();
			}
			if (mTileCache == null) {
				mTileCache = Bitmap.createBitmap(width, height, Config.RGB_565);
				mTileCacheBuffer = Bitmap.createBitmap(width, height, Config.RGB_565);
			}

			if(mAntiAlias!=mUpdatedAntiAlias){
				mAntiAlias = mUpdatedAntiAlias;
				mRenderProgram.setAntiAlias(mAntiAlias);
			}

			final RectF modelOuter = tileToModelRect(tileOuter);

			final Canvas canvas = new Canvas(mTileCache);
			canvas.drawColor(Color.WHITE);
			canvas.clipRect(0, 0, tileOuter.width() * mTileSize, tileOuter.height() * mTileSize);

			canvas.scale(mScale, mScale);
			canvas.translate(-modelOuter.left, -modelOuter.top);
			mRenderProgram.setVisibility(modelOuter);
			mRenderProgram.draw(canvas);

			synchronized(sync){
				mTileCacheRect = tileOuter;
				mTileCacheScale = mScale;
				mTileCacheWidth = width;
				mTileCacheHeight = height;
			}
		}
	}

	private void destroyTileCache() {
		if (mTileCache != null) {
			mTileCache.recycle();
			mTileCache = null;
		}
		if(mTileCacheBuffer!=null){
			mTileCacheBuffer.recycle();
			mTileCacheBuffer = null;
		}
		System.gc();
	}

	private Rect tileToScreenRect(Rect src) {
		return new Rect(
				src.left * mTileSize,
				src.top * mTileSize,
				src.right * mTileSize,
				src.bottom * mTileSize);
	}

	private Rect screenToOuterTileRect(Rect src) {
		final int step = mTileSize;

		final int left = src.left;
		final int top = src.top;
		final int right = src.right;
		final int bottom = src.bottom;

		final int rightMod = right % step;
		final int bottomMod = bottom % step;

		return new Rect(
				left / step,
				top / step,
				right / step + (rightMod > 0 ? 1 : 0),
				bottom / step + (bottomMod > 0 ? 1 : 0));
	}

	private RectF tileToModelRect(Rect src) {

		final int size = mTileSize;
		final float scale = mScale;

		int screenLeft = src.left * size;
		int screenTop = src.top * size;
		int screenRight = src.right * size;
		int screenBottom = src.bottom * size;

		float modelLeft = screenLeft / scale;
		float modelTop = screenTop / scale;
		float modelRight = screenRight / scale;
		float modelBottom = screenBottom / scale;
		return new RectF(
				modelLeft,
				modelTop,
				modelRight,
				modelBottom);
	}


	private void calculateDimensions() {
		mContentWidth = (int) Math.ceil(mMapView.width * mScale);
		mContentHeight = (int) Math.ceil(mMapView.height * mScale);
	}

	private MapView mMapView;
	private RenderProgram mRenderProgram;

	private Object sync = new Object();
	private Handler mPrivateHandler = new Handler();


	private Rect mUpdateTileCacheRect;
	private Runnable mUpdateTileCacheRunnable = new Runnable() {
		public void run() {
			if(mAntiAlias!=mUpdatedAntiAlias){
				mAntiAlias = mUpdatedAntiAlias;
				mRenderProgram.setAntiAlias(mAntiAlias);
			}
			final Rect rect = mUpdateTileCacheRect;
			updateTileCache(mUpdateTileCacheRect);
			if(rect == mUpdateTileCacheRect){
				postInvalidate();
			}
		}
	};

	private Rect mCreateTileCacheRect;
	private Runnable mCreateTileCacheRunnable = new Runnable() {
		public void run() {
			if(mAntiAlias!=mUpdatedAntiAlias){
				mAntiAlias = mUpdatedAntiAlias;
				mRenderProgram.setAntiAlias(mAntiAlias);
			}
			final Rect rect = mCreateTileCacheRect;
			invalidateTileCache(mCreateTileCacheRect, true);
			if(rect == mCreateTileCacheRect){
				postInvalidate();
			}
		}
	};

	private int mContentWidth;
	private int mContentHeight;

	private float mScale = 1.0f;
	private int mTileSize = 10;


	private Rect mTileCacheRect;
	private float mTileCacheScale;
	private int mTileCacheWidth;
	private int mTileCacheHeight;

	private Bitmap mTileCache;
	private Bitmap mTileCacheBuffer;

	private boolean mForceCacheInvalidate;
	private boolean mAntiAlias = true;
	private boolean mUpdatedAntiAlias = true;

	private boolean mAntiAliasEnabled = true;
	private boolean mAntiAliasDisabledOnScroll = true;

	private boolean mDisableEntireImageCaching = false;
	private int mEntireCacheLimits = 4 * 1024 * 1024;

	public void setAntiAliasingEnabled(boolean enabled){
		mUpdatedAntiAlias = enabled;
		mAntiAliasEnabled = enabled;
		mForceCacheInvalidate = true;
	}

	public void setAntiAliasingDisableOnScroll(boolean enabled){
		mAntiAliasDisabledOnScroll = enabled;
	}

	public void onScrollBegin() {
		if(mAntiAliasEnabled && !isCacheEntireImage()){
			if(mAntiAliasDisabledOnScroll){
				mUpdatedAntiAlias = false;
			}
		}
	}

	public void onScrollDone() {
		if(mAntiAliasEnabled && !isCacheEntireImage()){
			if(mAntiAliasDisabledOnScroll){
				mUpdatedAntiAlias = true;
				mForceCacheInvalidate = true;
			}		
		}
	}

}
