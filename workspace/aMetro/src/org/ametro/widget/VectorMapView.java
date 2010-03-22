/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.widget;

import java.util.List;

import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.util.AttributeSet;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwaySegment;
import org.ametro.model.SubwayStation;
import org.ametro.model.SubwayTransfer;
import org.ametro.render.RenderProgram;

public class VectorMapView extends BaseMapView {

    public VectorMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VectorMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VectorMapView(Context context) {
        super(context);
    }

    public void setModel(SubwayMap subwayMap) {
        if (subwayMap != null) {
            mSubwayMap = subwayMap;
            mRenderProgram = new RenderProgram(subwayMap);
            calculateDimensions();
            setInitialized(true);
        } else {
            setInitialized(false);
            mRenderProgram = null;
            mSubwayMap = null;
        }
    }

    public void setModelSelection(List<SubwayStation> stations, List<SubwaySegment> segments, List<SubwayTransfer> transfers){
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

        synchronized(sync){
	        if (!Rect.intersects(tileOuter, cache)) {
	            // redraw entire page
	            invalidateTileCache(viewport, true);
	        } else if (!cache.contains(tileOuter)) {
	            // redraw part of page
	        	mUpdateTileCacheRect = new Rect(viewport);
	        	mPrivateHandler.removeCallbacks(mUpdateTileCacheRunnable);
	        	mPrivateHandler.post(mUpdateTileCacheRunnable);
	        }
	        cache = mTileCacheRect;
	        int dx = cache.left * mTileSize - viewport.left;
	        int dy = cache.top * mTileSize - viewport.top;
	        canvas.drawColor(Color.WHITE);
	        canvas.drawBitmap(mTileCache, dx, dy, null);
        }
    }

    private void updateTileCache(Rect screenCoords) {
    	final Rect tileOuter = screenToOuterTileRect(screenCoords);
        RectF modelOuter = tileToModelRect(tileOuter);
        final Rect entireCache = mTileCacheRect;
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
        canvas.drawColor(Color.MAGENTA);

        canvas.save();
        canvas.scale(mScale, mScale);
        canvas.translate(-modelOuter.left, -modelOuter.top);

        mRenderProgram.clearVisibility();
        mRenderProgram.addVisibility2(horizontalSpanInModel, verticalSpanInModel);
        mRenderProgram.draw(canvas);

        canvas.restore();

        canvas.save();
        canvas.clipRect(dstOnScreen);
        canvas.drawColor(Color.GRAY);
        canvas.drawBitmap(mTileCache, srcOnScreen, dstOnScreen, null);
        canvas.restore();

        synchronized (sync) {
            mTileCacheRect = tileOuter;
            Bitmap swap = mTileCache;
            mTileCache = mTileCacheBuffer;
            mTileCacheBuffer = swap;
		}
    }


    private void invalidateTileCache(Rect screenCoords, boolean force) {
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

            final RectF modelOuter = tileToModelRect(tileOuter);

            final Canvas canvas = new Canvas(mTileCache);
            canvas.drawColor(Color.MAGENTA);
            canvas.clipRect(0, 0, tileOuter.width() * mTileSize, tileOuter.height() * mTileSize);

            canvas.scale(mScale, mScale);
            canvas.translate(-modelOuter.left, -modelOuter.top);
            mRenderProgram.invalidateVisible(modelOuter);
            mRenderProgram.draw(canvas);

            mTileCacheRect = tileOuter;
            mTileCacheScale = mScale;
            mTileCacheWidth = width;
            mTileCacheHeight = height;
        }
    }

    private void destroyTileCache() {
        if (mTileCache != null) {
            mTileCache.recycle();
            mTileCache = null;
            mTileCacheBuffer.recycle();
            mTileCacheBuffer = null;
        }
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
        mContentWidth = (int) Math.ceil(mSubwayMap.width * mScale);
        mContentHeight = (int) Math.ceil(mSubwayMap.height * mScale);
    }

    private SubwayMap mSubwayMap;
    private RenderProgram mRenderProgram;

    private Object sync = new Object();
    private Handler mPrivateHandler = new Handler();
    
    private Runnable mInvalidateRunnable = new Runnable() {
		public void run() {
			VectorMapView.this.postInvalidate();
		}
	};

    private Rect mUpdateTileCacheRect;
	private Runnable mUpdateTileCacheRunnable = new Runnable() {
		public void run() {
            updateTileCache(mUpdateTileCacheRect);
            mPrivateHandler.post(mInvalidateRunnable);
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
    
}
