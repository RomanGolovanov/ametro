package com.ametro.model;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.Log;

import com.ametro.libs.ExtendedPath;

public class ModelRenderer {
	
	public static class RenderIterator implements Iterator<Tile>{

		@Override
		public boolean hasNext() {
			return !mIsRecycled && (mCurrentTileNumber < mTileCount);
		}

		@Override
		public Tile next() {
			if(mIsRecycled || mCurrentTileNumber >= mTileCount) {
				return null; // if iterator out of range or recycled - return null;
			}
			invalidateRenderTarget(); // check for render target update
			Tile tile = currentTile();
			advanceTilePosition();
			return tile;
		}
		
		@Override
		public void remove() {
			// do nothing
		}
		
		public int size(){
			return mOverallTileCount;
		}
		
		public int position(){
			return mOverallTileNumber;
		}
		
		public RenderIterator(Model model){
			mRenderer = new ModelRenderer(model);
			mModelHeight = model.getHeight();
			mModelWidth = model.getWidth();
			mMipMapLevel = 0;
			mOverallTileNumber = 0;
			mOverallTileCount = getOverallTileCount(mModelWidth, mModelHeight, mMipMapLevel);
			mTileRect = new Rect(0,0,Tile.WIDTH,Tile.HEIGHT);
			mTileImage = Bitmap.createBitmap(Tile.WIDTH, Tile.HEIGHT, Config.RGB_565);
			prepareRenderer();
		}

		
		private void prepareRenderer() {
			mRenderTargetOffset = 0;
			mWidth = Tile.getDimension(mModelWidth , mMipMapLevel);
			mHeight = Tile.getDimension(mModelHeight , mMipMapLevel);
			mColumnCount = Tile.getTileCount(mWidth);
			mRowCount = Tile.getTileCount(mHeight);
			int maxRenderHeight = Math.max(Tile.HEIGHT, Tile.HEIGHT * 100 / mColumnCount);
			mRenderTargetHeight = Math.min(mHeight, maxRenderHeight - (maxRenderHeight % Tile.HEIGHT));
			mTileCount = mColumnCount * mRowCount;
			
			mRenderTarget = null;
			mRenderRect = new Rect(0,0,mWidth,mRenderTargetHeight);
			mCurrentTileNumber = 0;
			
			mIsRecycled = false;
			
			Log.i("aMetro", "Prepaired for rendering mipmap level " + mMipMapLevel + ", height: " + mHeight + ", step: " + mRenderTargetHeight);
			
		}

		public void recycle(){
			mIsRecycled = true;
			mRenderTarget.recycle();
			mRenderTarget = null;
		}
		
		private static int getOverallTileCount(int x, int y, int level){
			if(level >= Tile.MIP_MAP_LEVELS){
				return 0;
			}
			final int xc = Tile.getTileCount(x, level);
			final int yc = Tile.getTileCount(y, level);
			final int count = xc*yc;
			Log.i("aMetro", "Level " + level + " tile count: " + count);
			return xc*yc + getOverallTileCount(x, y, level+1);
		}

		private Tile currentTile(){
			// calculate current cell row and column
			final int row = mCurrentTileNumber / mColumnCount; 
			final int column = mCurrentTileNumber % mColumnCount; 

			// calculate render targets rect
			final int left = column * Tile.WIDTH;
			final int top = row * Tile.HEIGHT - mRenderRect.top;
			final int right = Math.min( left + Tile.WIDTH, mWidth );
			final int bottom = Math.min( top + Tile.HEIGHT, mHeight );
			Rect source = new Rect(left,top,right,bottom);
			
			// resize destination size at borders 
			mTileRect.right = (right - left);
			mTileRect.bottom = (bottom - top);
			
			Canvas c = new Canvas(mTileImage);
			c.drawColor(Color.MAGENTA); // fill image and copy tile from render target
			c.drawBitmap(mRenderTarget, source, mTileRect, null);
			c.save();
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			mTileImage.compress(Bitmap.CompressFormat.PNG, 90, byteArray);

			return new Tile(row, column, mMipMapLevel, byteArray.toByteArray());
		}
		

		private void advanceTilePosition() {
			mCurrentTileNumber++;
			mOverallTileNumber++;
			if(mCurrentTileNumber>=mTileCount){
				recycle();
				
				mMipMapLevel++;
				if(mMipMapLevel<Tile.MIP_MAP_LEVELS){
					prepareRenderer();
				}
			}
		}	
		
		private void invalidateRenderTarget(){
			int row = mCurrentTileNumber / mColumnCount; 
			boolean outOfTarget = (row * Tile.HEIGHT) >= mRenderTargetOffset;
			if(mRenderTarget == null){
				mRenderTarget = Bitmap.createBitmap(mWidth, mRenderTargetHeight, Config.RGB_565);
				outOfTarget = true;
			}
			if(outOfTarget){
				nextRenderTarget();
			}
		}
		
		private void nextRenderTarget(){
			mRenderRect.top = mRenderTargetOffset;
			mRenderRect.bottom = mRenderTargetOffset + Math.min(mRenderTargetHeight, mHeight - mRenderTargetOffset);
			Canvas canvas = new Canvas(mRenderTarget);
			
			
			canvas.translate(-mRenderRect.left, -mRenderRect.top);
			if(mMipMapLevel>0){
			float scale = 1.0f/Tile.getScale(mMipMapLevel);
			canvas.scale(scale, scale);
			}

			Log.i("aMetro",String.format("Render target %s,%s,%s,%s ",
					Integer.toString(mRenderRect.left),
					Integer.toString(mRenderRect.top),
					Integer.toString(mRenderRect.right),
					Integer.toString(mRenderRect.bottom)
					));
			
			mRenderer.render(canvas,null);
			mRenderTargetOffset += mRenderTargetHeight;
		}


		
		private boolean mIsRecycled;
		
		private Bitmap mTileImage;	
		
		private ModelRenderer mRenderer;
		private Rect mRenderRect;
		private Rect mTileRect;
		
		private int mModelWidth;
		private int mModelHeight;
		
		private int mWidth;
		private int mHeight;
		
		private int mRowCount;
		private int mColumnCount;
		
		private int mMipMapLevel;
		
		private int mCurrentTileNumber;
		private int mTileCount;
		
		private int mOverallTileCount;
		private int mOverallTileNumber;
		
		private Bitmap mRenderTarget;
		private int mRenderTargetOffset;
		private int mRenderTargetHeight;

	}
	
	private Model mModel;

	private Paint mStationBorderPaint;
	private Paint mStationFillPaint;
	private Paint mLinePaint;
	private Paint mLineUnavailablePaint;
	private Paint mTextPaint;
	private Paint mTextBackgroundPaint;
	private Paint mTextForegroundPaint;
	private Paint mFillPaint;

	private int mLinesWidth;
	private boolean mWordWrap;
	private boolean mUpperCase;
	private int mStationDiameter;

	public ModelRenderer(Model model) {
		mModel = model;

		mLinesWidth = mModel.getLinesWidth();
		mWordWrap = mModel.isWordWrap();
		mUpperCase = mModel.isUpperCase();
		mStationDiameter = mModel.getStationDiameter();

		mStationFillPaint = new Paint();
		mStationFillPaint.setStyle(Style.FILL);
		mStationFillPaint.setAntiAlias(true);

		mStationBorderPaint = new Paint();
		mStationBorderPaint.setColor(Color.WHITE);
		mStationBorderPaint.setStyle(Style.STROKE);
		mStationBorderPaint.setStrokeWidth(0);
		mStationBorderPaint.setAntiAlias(true);

		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setPathEffect(new  CornerPathEffect(mLinesWidth*0.6f));

		mLineUnavailablePaint = new Paint(mLinePaint);

		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setStyle(Style.FILL_AND_STROKE);
		mTextPaint.setStrokeWidth(0);
		mTextPaint.setTypeface(Typeface.DEFAULT);
		mTextPaint.setFakeBoldText(true);
		mTextPaint.setTextSize(10);
		//mTextPaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		mTextPaint.setTextAlign(Align.LEFT);

		mLinePaint.setStrokeWidth( mLinesWidth );
		mLineUnavailablePaint.setStrokeWidth( mLinesWidth*0.75f );
		mLineUnavailablePaint.setPathEffect(new DashPathEffect(new float[]{ mLinesWidth*0.8f, mLinesWidth*0.4f }, 0));
	}


	private void render(Canvas canvas){
		Date startTimestamp = new Date();
		canvas.drawColor(Color.WHITE);
		drawLines(canvas);
		drawTransfers(canvas);
		drawStations(canvas);			
		canvas.save();
		Log.i("aMetro", String.format("Model %s rendering time: %sms", mModel.getMapName(), Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
	}

	public void render(Canvas canvas, Rect src)
	{
		if(src!=null){
			canvas.translate(-src.left, -src.top);
		}
		render(canvas);
	}

	private void drawTransfers(Canvas canvas) {
		final float radius = (float)mStationDiameter/2 + 2.2f;
		final float radiusBig = (float)mStationDiameter/2 + 3.5f;

		Paint whitePaint = new Paint();
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStyle(Style.FILL);
		whitePaint.setStrokeWidth(mLinesWidth+1.2f);
		whitePaint.setAntiAlias(true);

		Paint blackPaint =new Paint();
		blackPaint.setColor(Color.BLACK);
		blackPaint.setStyle(Style.FILL);
		blackPaint.setStrokeWidth(mLinesWidth+3.5f);
		blackPaint.setAntiAlias(true);


		for (Iterator<Transfer> transfers = mModel.getTransfers(); transfers.hasNext();) {
			Transfer transfer = transfers.next();
			int flags = transfer.getFlags();
			if( (flags & Transfer.INVISIBLE) != 0) continue;
			Point from = transfer.getFrom().getPoint();
			Point to = transfer.getTo().getPoint();
			if(from!=null && to!=null){
				canvas.drawCircle(from.x, from.y, radiusBig, blackPaint);
				canvas.drawCircle(to.x, to.y, radiusBig, blackPaint);
				canvas.drawLine(from.x, from.y, to.x, to.y, blackPaint);
			}

		}
		for (Iterator<Transfer> transfers = mModel.getTransfers(); transfers.hasNext();) {
			Transfer transfer = transfers.next();
			int flags = transfer.getFlags();
			if( (flags & Transfer.INVISIBLE) != 0) continue;
			Point from = transfer.getFrom().getPoint();
			Point to = transfer.getTo().getPoint();
			if(from!=null && to!=null){						
				canvas.drawCircle(from.x, from.y, radius, whitePaint);
				canvas.drawCircle(to.x, to.y, radius, whitePaint);
				canvas.drawLine(from.x, from.y, to.x, to.y, whitePaint);
			}

		}
	}

	private void drawLines(Canvas canvas) {
		Enumeration<Line> lines = mModel.getLines();
		while(lines.hasMoreElements()){
			Line line = lines.nextElement();
			for (Iterator<Segment> segments = line.getSegments(); segments.hasNext();) {
				Segment segment = segments.next();
				if( (segment.getFlags() & Segment.INVISIBLE ) == 0 ){
					drawSegment(canvas, line, segment);
				}
			}			
		}		

	}

	private void drawSegment(Canvas canvas, Line line, Segment segment) {
		Station from = segment.getFrom();
		Station to = segment.getTo();

		if(from.getPoint()==null || to.getPoint()==null){
			return;
		}

		Double delay = segment.getDelay();
		boolean lineWorking = (delay != null && delay > 0);

		Paint linePaint = lineWorking ? mLinePaint : mLineUnavailablePaint;
		linePaint.setColor(line.getColor());

		ExtendedPath path = new ExtendedPath();
		Segment opposite = line.getSegment(to,from);

		Point[] additionalPoints = segment.getAdditionalNodes();
		Point[] reversePoints = opposite==null ? null : opposite.getAdditionalNodes();

		boolean additionalForward = additionalPoints!=null;
		boolean additionalBackward = reversePoints!=null;

		if(!additionalForward && additionalBackward){
			// do nothing!
		}else{
			drawSegmentPath(line, segment, from, to, path, additionalPoints);
		}
		canvas.drawPath(path, linePaint);
	}

	private void drawSegmentPath(Line line, Segment segment, Station from, Station to, ExtendedPath path, Point[] additionalPoints) {
		Point pointFrom = from.getPoint();
		Point pointTo = to.getPoint();
		if(additionalPoints!=null){
			if( (segment.getFlags() & Segment.SPLINE) != 0 ){
				Point[] points = new Point[additionalPoints.length+2];
				points[0] = pointFrom;
				points[points.length-1] = pointTo;
				for (int i = 0; i < additionalPoints.length; i++) {
					Point point = additionalPoints[i];
					points[i+1] = point;
				}
				path.drawSpline(points, 0, points.length);
			}else{
				path.moveTo(pointFrom.x, pointFrom.y);
				for (int i = 0; i < additionalPoints.length; i++) {
					path.lineTo(additionalPoints[i].x, additionalPoints[i].y);	
				}
				path.lineTo(pointTo.x, pointTo.y);
			}
		}else{
			path.moveTo(pointFrom.x, pointFrom.y);
			path.lineTo(pointTo.x, pointTo.y);
		}
	}

	private void drawStations(Canvas canvas) {
		float radius = (float)mStationDiameter/2.0f;
		Enumeration<Line> lines = mModel.getLines();
		while(lines.hasMoreElements()){
			Line line = lines.nextElement();
			Enumeration<Station> stations = line.getStations();
			while(stations.hasMoreElements()){
				Station station = stations.nextElement();
				if(station.getPoint()!=null){
					drawStation(canvas, radius, station);
				}
			}
		}	
		lines = mModel.getLines();
		while(lines.hasMoreElements()){
			Line line = lines.nextElement();
			Enumeration<Station> stations = line.getStations();
			while(stations.hasMoreElements()){
				Station station = stations.nextElement();
				if(station.getPoint()!=null){
					drawStationName(canvas, radius, station);
				}
			}
		}			
	}

	private void drawStation(Canvas canvas, float radius, Station station) {
		final Point point = station.getPoint();
		final Line line = station.getLine();
		final int color =  line.getColor();
		final boolean hasConnections = station.hasConnections();
		if(!hasConnections){
			mStationFillPaint.setColor(color);
			canvas.drawCircle(point.x, point.y, radius, mStationFillPaint);
			mStationFillPaint.setColor(Color.WHITE);
			canvas.drawCircle(point.x, point.y, radius*0.7f, mStationFillPaint);

		}else{
			mStationFillPaint.setColor(color);
			mTextPaint.setColor(color);
			canvas.drawCircle(point.x, point.y, radius, mStationFillPaint);
			canvas.drawCircle(point.x, point.y, radius, mStationBorderPaint);
		}
	}

	private void drawStationName(Canvas canvas, float radius, Station station) {
		final Rect rect = station.getRect();
		final String name = station.getName();
		if(rect!=null && name!=null){
			final Point point = station.getPoint();
			final Line line = station.getLine();
			final int labelColor = line.getLabelColor();
			final int bgColor = line.getLabelBgColor();
			mTextPaint.setColor(labelColor);
			if(bgColor == 0){
				mTextBackgroundPaint = new Paint(mTextPaint);
				mTextBackgroundPaint.setColor(Color.BLACK);
				mTextForegroundPaint = new Paint(mTextPaint);
				mTextForegroundPaint.setColor(Color.WHITE);
				mFillPaint = null;
			}else{
				mTextBackgroundPaint = null;
				mTextForegroundPaint = null;

				mFillPaint = new Paint();
				mFillPaint.setColor(bgColor);
				mFillPaint.setStyle(Style.FILL);
			}

//			Paint borderPaint = new Paint(mTextPaint);
//			borderPaint.setPathEffect(new DashPathEffect(new float[]{3,3}, 0));
//			borderPaint.setStyle(Style.STROKE);
//			canvas.drawRect(rect, borderPaint);
			
			drawText(canvas, mUpperCase ? name.toUpperCase() : name, rect, point);
		}
	}

	private void drawText(Canvas canvas, String text, Rect rect, Point align){
		if( rect.width() == 0 || rect.height()==0) return;
		//text = text.toUpperCase();
		if( rect.width() > rect.height() ){
			drawRectText(canvas, text, rect, align);
		}else{
			Path textPath = new Path(); 
			Rect bounds = new Rect();
			mTextPaint.getTextBounds(text, 0, text.length()-1, bounds);
			final int right = rect.right;
			final int bottom = rect.bottom;
			final int top = rect.top;
			//final int left = rect.left;
			final int textHeight = bounds.height();
			final int textWidth = bounds.width();
			Rect textRect; 
			if(align.y > rect.centerY()){
				textRect = new Rect(right - textHeight, bottom - textWidth - 20, right, bottom-5);

			}else{
				textRect = new Rect(right - textHeight, top+5, right, top + textWidth+20);
			}
			textPath.moveTo(textRect.right, textRect.bottom);
			textPath.lineTo(textRect.right, textRect.top); 

			if(mFillPaint!=null) canvas.drawRect(textRect, mFillPaint);

			if(mTextForegroundPaint!=null) canvas.drawTextOnPath(text, textPath, -SHADOW_DELTA, -SHADOW_DELTA, mTextForegroundPaint);
			if(mTextBackgroundPaint!=null) canvas.drawTextOnPath(text, textPath, SHADOW_DELTA, SHADOW_DELTA, mTextBackgroundPaint);
			canvas.drawTextOnPath(text, textPath, 0, 0, mTextPaint);
		}
	}

	private void drawRectText(Canvas canvas, String text, Rect rect, Point align) {
		Rect bounds = new Rect();
		mTextPaint.getTextBounds(text, 0, text.length(), bounds);
		if(bounds.width()>rect.width() && mWordWrap){
			int space = text.indexOf(' ');
			if(space!=-1){
				drawTextOnWhite(canvas, text.substring(0, space), rect, align);
				Rect nextLineRect = new Rect(rect.left, rect.top+bounds.height()+2, rect.right, rect.bottom+bounds.height()+2);
				drawTextOnWhite(canvas, text.substring(space+1), nextLineRect, align);
				return;
			}
		}
		drawTextOnWhite(canvas, text, rect, align);
	}

	private static final int SHADOW_DELTA = 1; 
	
	private int drawTextOnWhite(Canvas canvas, String text, Rect rect, Point align) {
		final int len = text.length();
		final int top = rect.top;
		final int left = rect.left;
		final int right = rect.right;
		Rect bounds = new Rect();
		Rect fill = new Rect();
		if(align.x > rect.centerX()){ // align to right
			mTextPaint.setTextAlign(Align.RIGHT);
			if(mTextForegroundPaint!=null) mTextForegroundPaint.setTextAlign(Align.RIGHT);
			if(mTextBackgroundPaint!=null) mTextBackgroundPaint.setTextAlign(Align.RIGHT);
			mTextPaint.getTextBounds(text, 0, len, bounds);
			fill.set(right-bounds.width()-1, top, right+2, top + bounds.height()+1);
			if(mFillPaint!=null) canvas.drawRect(fill, mFillPaint);
			
			if(mTextForegroundPaint!=null) canvas.drawText(text, right-SHADOW_DELTA, top+bounds.height()-SHADOW_DELTA, mTextForegroundPaint);
			if(mTextBackgroundPaint!=null) canvas.drawText(text, right+SHADOW_DELTA, top+bounds.height()+SHADOW_DELTA, mTextBackgroundPaint);
			
			canvas.drawText(text, right, top+bounds.height(), mTextPaint);
		}else{ // align to left
			mTextPaint.setTextAlign(Align.LEFT);
			if(mTextForegroundPaint!=null) mTextForegroundPaint.setTextAlign(Align.LEFT);
			if(mTextBackgroundPaint!=null) mTextBackgroundPaint.setTextAlign(Align.LEFT);
			mTextPaint.getTextBounds(text, 0, len, bounds);
			fill.set(left-1, top, left+bounds.width()+2, top+bounds.height()+1);
			if(mFillPaint!=null) canvas.drawRect(fill, mFillPaint);
			
			if(mTextForegroundPaint!=null) canvas.drawText(text, left-SHADOW_DELTA, top+bounds.height()-SHADOW_DELTA, mTextForegroundPaint);
			if(mTextBackgroundPaint!=null) canvas.drawText(text, left+SHADOW_DELTA, top+bounds.height()+SHADOW_DELTA, mTextBackgroundPaint);
			
			canvas.drawText(text, left, top+bounds.height(), mTextPaint);
		}
		return bounds.height();
	}


}
