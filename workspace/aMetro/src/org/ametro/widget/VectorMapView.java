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

import org.ametro.model.Model;
import org.ametro.render.RenderProgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;

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

	public void setModel(Model model) {
		if (model != null) {
			mModel = model;
			mRenderProgram = new RenderProgram(model);
			setInitialized(true);
			calculateDimensions();
		} else {
			setInitialized(false);
			mRenderProgram = null;
			mModel = null;
		}
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
	
	public void setModelScrollCenter(PointF p) {
		setModelScrollCenter(p.x,p.y);
	}

	public PointF getModelScrollCenter(){
		Point p = super.getScrollCenter();
		float x = p.x / mScale;
		float y = p.y / mScale;
		return new PointF(x,y);
	}	
	
	@Override
	protected int getContentHeight() {
		return mContentHeight;
	}

	@Override
	protected int getContentWidth() {
		return mContentWidth;
	}

	protected void onDrawRect(Canvas canvas, Rect viewport) {
//		long time = System.currentTimeMillis();

		invalidateTileCache(viewport, false);
		Rect tileOuter = screenToOuterTileRect(viewport);
		Rect cache = mTileCacheRect;

		if(!Rect.intersects(tileOuter, cache)){
			// redraw entire page
			invalidateTileCache(viewport, true);
		}else if (!cache.contains(tileOuter)){
			// redraw part of page
			updateTileCache(viewport);
		}
		cache = mTileCacheRect;
		int dx = cache.left * mTileSize - viewport.left;
		int dy = cache.top * mTileSize - viewport.top;
		canvas.drawBitmap(mTileCache, dx, dy, null);

//		if(LogUtil.loggable(Log.INFO)){
//			LogUtil.info("draw time: " + (System.currentTimeMillis() - time) + "ms");
//		}
	}

	private void updateTileCache(Rect screenCoords) {
		Rect tileOuter = screenToOuterTileRect(screenCoords);
		Rect modelOuter = tileToModelRect(tileOuter);
		Rect entireCache = mTileCacheRect;
		Rect cache = new Rect(entireCache);
		cache.intersect(tileOuter);

		Rect dst = new Rect(cache); // control canvas position
		dst.offsetTo(cache.left - tileOuter.left , cache.top - tileOuter.top );
		Rect dstOnScreen = tileToScreenRect(dst);

		Rect src = new Rect(cache); // cache canvas position
		src.offsetTo(cache.left - entireCache.left, cache.top - entireCache.top);
		Rect srcOnScreen = tileToScreenRect(src);

		Rect verticalSpan = new Rect(tileOuter);
		Rect horizontalSpan = new Rect(tileOuter);

		if(tileOuter.right == cache.right && tileOuter.bottom == cache.bottom){
			horizontalSpan.bottom = cache.top;
			verticalSpan.right = cache.left;
		}else if(tileOuter.right == cache.right && tileOuter.top == cache.top){
			horizontalSpan.top = cache.bottom;
			verticalSpan.right = cache.left;
		}else if(tileOuter.left == cache.left && tileOuter.bottom == cache.bottom){
			horizontalSpan.bottom = cache.top;
			verticalSpan.left = cache.right;
		}else if(tileOuter.left == cache.left && tileOuter.top == cache.top){
			horizontalSpan.top = cache.bottom;
			verticalSpan.left = cache.right;
		}else{
			throw new RuntimeException("Invalid viewport splitting algorithm");
		}

		Rect horizontalSpanInModel = tileToModelRect(horizontalSpan);
		Rect verticalSpanInModel = tileToModelRect(verticalSpan);

		Canvas canvas = new Canvas(mTileCacheBuffer);
		canvas.drawColor(Color.MAGENTA);

		canvas.save();
		canvas.scale(mScale,mScale);
		canvas.translate(-modelOuter.left,-modelOuter.top);
		
		mRenderProgram.clearVisibility();
		mRenderProgram.addVisibility(horizontalSpanInModel);
		mRenderProgram.addVisibility(verticalSpanInModel);
		mRenderProgram.draw(canvas);
		
		canvas.restore();		

		canvas.save();
		canvas.clipRect(dstOnScreen);
		canvas.drawColor(Color.GRAY);
		canvas.drawBitmap(mTileCache, srcOnScreen, dstOnScreen, null);
		canvas.restore();


		mTileCacheRect = tileOuter;
		Bitmap swap = mTileCache;
		mTileCache = mTileCacheBuffer;
		mTileCacheBuffer = swap;

	}


	private void invalidateTileCache(Rect screenCoords, boolean force) {
		Rect tileOuter = screenToOuterTileRect(screenCoords);
		final int width = getWidth() + mTileSize*2;
		final int height = getHeight() + mTileSize*2;
		final boolean isViewportChanged = mTileCacheScale != mScale || mTileCacheWidth != width || mTileCacheHeight != height;
		if (mTileCache == null || isViewportChanged || force) {
			if (isViewportChanged) {
				destroyTileCache();
			}
			if (mTileCache == null) {
				mTileCache = Bitmap.createBitmap(width, height, Config.RGB_565);
				mTileCacheBuffer = Bitmap.createBitmap(width, height, Config.RGB_565);
			}

			Rect modelOuter = tileToModelRect(tileOuter);

			Canvas canvas = new Canvas(mTileCache);
			canvas.drawColor(Color.MAGENTA);
			canvas.clipRect(0, 0, tileOuter.width()*mTileSize, tileOuter.height()*mTileSize);

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
				src.left*mTileSize, 
				src.top*mTileSize, 
				src.right*mTileSize, 
				src.bottom*mTileSize);
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

	private Rect tileToModelRect(Rect src) {

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
		return new Rect(
				(int) Math.floor(modelLeft),
				(int) Math.floor(modelTop), 
				(int) Math.ceil(modelRight),
				(int) Math.ceil(modelBottom));
	}


	private void calculateDimensions() {
		mContentWidth = (int) Math.ceil(mModel.getWidth() * mScale);
		mContentHeight = (int) Math.ceil(mModel.getHeight() * mScale);
	}

	private Model mModel;
	private RenderProgram mRenderProgram;

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
