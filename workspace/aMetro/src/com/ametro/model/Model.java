package com.ametro.model;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import com.ametro.libs.Helpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.Log;

public class Model {

	public Model(String mapName, int size){
		mMapRenderer = new MapRenderer();

		mMapName = mapName;
		
		mStationCount = 0;
		mStationPreservedCount = 0;
		mStationNameIndex = new Hashtable<String, Integer>();
		reallocateStations(size);

		mLineCount = 0;
		mLinePreservedCount = 0;
		mLineNameIndex = new Hashtable<String, Integer>();
		reallocateLines(10);
	}

	public String getCityName() {
		return mCityName;
	}

	public String getCountryName() {
		return mCountryName;
	}

	public void setCityName(String cityName) {
		mCityName = cityName;
	}

	public void setCountryName(String countryName) {
		mCountryName = countryName;
	}

	public int getStationId(String lineName, String name){
		return mStationNameIndex.get( lineName+";"+name );
	}

	public int getStationCount(){
		return mStationCount;
	}

	public String getStationName(int id){
		return mStationNames[id];
	}

	public Rect getStationBox(int id){
		return mStationBoxes[id];
	}

	public Point getStationPoint(int id){
		return mStationPoints[id];
	}

	public Rect getStationRect(int id){
		return mStationBoxes[id];
	}

	public void addLineEdge(int fromId, int toId, Double delay, int lineId )
	{
		addGenericEdge(fromId, toId, delay, false, false, null, lineId, 0);
	}

	public void addTransferEdge(int fromId, int toId, Double delay, int flags)
	{
		addGenericEdge(fromId, toId, delay, true, false, null, null, flags);
	}

	public void addAdditionToEdge(int fromId, int toId, Point additionalNode)
	{
		mEdgeAdditionalNodes[fromId][toId] = mEdgeAdditionalNodes[toId][fromId] = additionalNode;
	}

	public void addSplineToEdge(int fromId, int toId, Point additionalNode)
	{
		mEdgeAdditionalNodes[fromId][toId] = mEdgeAdditionalNodes[toId][fromId] = additionalNode;
		int flags = mEdgeFlags[fromId][toId] | EDGE_FLAG_SPLINE;
		mEdgeFlags[fromId][toId] = mEdgeFlags[toId][fromId] = flags;
	}


	public int addStation(int lineId, String lineName, String name, Rect rect, Point point)
	{
		Integer id = mStationNameIndex.get(lineName + ";" + name);
		if(id==null){
			id = addGenericStation(lineId, lineName, name, rect, point);
		}else{
			mStationBoxes[id] = rect;
			mStationPoints[id] = point;
		}
		return id;
	}

	public int addStation(int lineId, String lineName, String name)
	{
		Integer id = mStationNameIndex.get(lineName + ";" + name);
		if(id==null){
			id = addGenericStation(lineId, lineName, name,null,null);
		}
		return id;
	}

	public void setDimensions(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	public boolean isExistEdge(int fromStationId, int toStationId) {
		return (mEdgeFlags[fromStationId][toStationId] & EDGE_FLAG_CREATED)!=0;
	}

	public int addLine(String name, int color){
		Integer id = mLineNameIndex.get(name);
		if(id==null){
			id = getNextLineId();
			mLineNames[id] = name;
			mLineColors[id] = color;
		}
		return id;
	}

	public Integer getLineId(String name){
		return mLineNameIndex.get(name);
	}

	private int addGenericStation(Integer lineId, String lineName, String name, Rect rect, Point point)
	{
		int id = getNextStationId();
		mStationNames[id] = name;
		mStationBoxes[id] = rect;
		mStationPoints[id] = point;
		mStationLine[id] = lineId;
		mStationNameIndex.put(lineName + ";" + name, id);
		return id;
	}

	private void addGenericEdge(int fromId, int toId, Double delay, boolean isTransfer, boolean isSpline, Point additionalNode, Integer lineId, int flags )
	{
		mEdgeDelays[fromId][toId] = mEdgeDelays[toId][fromId] = delay;
		mEdgeAdditionalNodes[fromId][toId] = mEdgeAdditionalNodes[toId][fromId] = additionalNode;
		mEdgeLines[fromId][toId] = mEdgeLines[toId][fromId] = lineId != null ? lineId : LINE_TRANSFER;

		flags |= EDGE_FLAG_CREATED;
		if (isTransfer){
			flags |= EDGE_FLAG_TRANSFER;
		}
		if(isSpline){
			flags |= EDGE_FLAG_SPLINE;
		}
		if(lineId!=null){
			flags |= EDGE_FLAG_LINE;
		}

		mEdgeFlags[fromId][toId] = mEdgeFlags[toId][fromId] = flags;
	}

	private int getNextStationId()
	{
		if(mStationCount>=mStationPreservedCount){
			reallocateStations( (mStationCount / 10 + 1) * 10 );
		}
		return mStationCount++;
	}

	private void reallocateStations(int size){
		if(size < mStationPreservedCount)
			throw new InvalidParameterException("New size cannot be less that current allocated");
		Log.d("aMetro", String.format("Reallocate stations from %s to %s", Integer.toString(mStationPreservedCount), Integer.toString(size) ));

		mStationNames = Helpers.resizeArray(mStationNames, size);
		mStationBoxes = Helpers.resizeArray(mStationBoxes, size);
		mStationPoints = Helpers.resizeArray(mStationPoints, size);
		mStationLine= Helpers.resizeArray(mStationLine, size);

		mEdgeDelays = Helpers.resizeArray(mEdgeDelays, size,size);
		mEdgeAdditionalNodes = Helpers.resizeArray(mEdgeAdditionalNodes, size,size);
		mEdgeFlags = Helpers.resizeArray(mEdgeFlags, size,size);
		mEdgeLines = Helpers.resizeArray(mEdgeLines, size,size);

		mStationPreservedCount = size;
	}

	private int getNextLineId()
	{
		if(mLineCount>=mLinePreservedCount){
			reallocateLines( (mLineCount / 10 + 1) * 10 );
		}
		return mLineCount++;
	}

	private void reallocateLines(int size){
		if(size < mLinePreservedCount)
			throw new InvalidParameterException("New size cannot be less that current allocated");
		Log.d("aMetro", String.format("Reallocate lines from %s to %s", Integer.toString(mLinePreservedCount), Integer.toString(size) ));
		mLineNames = Helpers.resizeArray(mLineNames, size);
		mLineColors = Helpers.resizeArray(mLineColors, size);
		mLinePreservedCount = size;
	}


	public static final int EDGE_FLAG_CREATED =		0x01;
	public static final int EDGE_FLAG_SPLINE =		0x02;
	public static final int EDGE_FLAG_TRANSFER =	0x04;
	public static final int EDGE_FLAG_LINE =		0x08;
	public static final int EDGE_FLAG_INVISIBLE =	0x10;

	public static final int LINE_TRANSFER =	-1;

	private MapRenderer mMapRenderer;

	private String mCityName;
	private String mCountryName;

	private Dictionary<String, Integer> mStationNameIndex;

	private int mStationCount = 0;
	private int mStationPreservedCount = 0;

	private String[] 	mStationNames;
	private Rect[]		mStationBoxes;
	private Point[]		mStationPoints;
	private Integer[]	mStationLine;

	private Double[][] 	mEdgeDelays; // stores delays
	private Point[][]	mEdgeAdditionalNodes;
	private int[][] 	mEdgeFlags;
	private int[][] 	mEdgeLines;

	private Dictionary<String, Integer> mLineNameIndex;

	private int mLineCount = 0;
	private int mLinePreservedCount = 0;

	private String[]	mLineNames;
	private int[]		mLineColors;

	private String mMapName;
	
	private int mWidth;
	private int mHeight;

	private int mStationDiameter;
	private int mLinesWidth;
	private boolean mWordWrap;
	private boolean mUpperCase;



	public boolean isUpperCase() {
		return mUpperCase;
	}

	public void setUpperCase(boolean mUpperCase) {
		this.mUpperCase = mUpperCase;
	}

	public boolean isWordWrap() {
		return mWordWrap;
	}

	public void setWordWrap(boolean mWordWrap) {
		this.mWordWrap = mWordWrap;
	}

	public int getLineCount() {
		return mLineCount;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public int getStationDiameter() {
		return mStationDiameter;
	}

	public int getLinesWidth() {
		return mLinesWidth;
	}

	public void setStationDiameter(int mStationDiameter) {
		this.mStationDiameter = mStationDiameter;
	}

	public void setLinesWidth(int mLineWidth) {
		this.mLinesWidth = mLineWidth;
	}

	public void render(Canvas canvas){
		mMapRenderer.render(canvas,null);
	}

	public void render(Canvas canvas, Rect src){
		mMapRenderer.render(canvas, src);
	}
	
	public String getMapName() {
		return mMapName;
	}

	
	private class MapRenderer {

		Paint mStationBorderPaint;
		Paint mStationFillPaint;
		Paint mLinePaint;
		Paint mTextPaint;
		Paint mFillPaint;

		public MapRenderer(){
			mFillPaint = new Paint();
			mFillPaint.setColor(Color.WHITE);
			mFillPaint.setStyle(Style.FILL);
			mFillPaint.setAntiAlias(true);

			mStationFillPaint = new Paint();
			mStationFillPaint.setStyle(Style.FILL);

			mStationBorderPaint = new Paint();
			mStationBorderPaint.setColor(Color.WHITE);
			mStationBorderPaint.setStyle(Style.STROKE);
			mStationBorderPaint.setStrokeWidth(0);
			mStationBorderPaint.setAntiAlias(true);

			mLinePaint = new Paint();
			mLinePaint.setStyle(Style.STROKE);
			mLinePaint.setAntiAlias(true);

			mTextPaint = new Paint();
			mTextPaint.setAntiAlias(true);
			mTextPaint.setStyle(Style.FILL_AND_STROKE);
			mTextPaint.setStrokeWidth(0);
			mTextPaint.setTextAlign(Align.LEFT);
			//mTextPaint.setTypeface(Typeface.MONOSPACE);

		}

		public void prepareObjects(){
			mLinePaint.setStrokeWidth( mLinesWidth );
		}


		private void render(Canvas canvas){
			Date startTimestamp = new Date();
			prepareObjects();
			canvas.drawColor(Color.WHITE);
			renderTransportLines(canvas);
			renderTransfers(canvas);
			renderStations(canvas);			
			canvas.save();
			Log.d("aMetro", String.format("Overall rendering time is %sms", Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
		}

		public void render(Canvas canvas, Rect src)
		{
			if(src!=null){
				Matrix matrix;
				matrix = canvas.getMatrix();
				matrix.preTranslate(-src.left, -src.top);
				//matrix.setTranslate(src.left, src.top);
				canvas.setMatrix(matrix);
			}
			render(canvas);
		}

		private void renderTransfers(Canvas canvas) {
			final int stationCount = mStationCount;
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

			for(int row = 0; row < stationCount; row++){
				for(int col = 0; col < row; col++){
					if(mEdgeLines[row][col] == LINE_TRANSFER){
						int flags = mEdgeFlags[row][col];
						if( (flags & EDGE_FLAG_INVISIBLE) != 0) continue;
						Point from = mStationPoints[row];
						Point to = mStationPoints[col];
						canvas.drawCircle(from.x, from.y, radiusBig, blackPaint);
						canvas.drawCircle(to.x, to.y, radiusBig, blackPaint);
						canvas.drawLine(from.x, from.y, to.x, to.y, blackPaint);
					}
				}
			}
			for(int row = 0; row < stationCount; row++){
				for(int col = 0; col < row; col++){
					if(mEdgeLines[row][col] == LINE_TRANSFER){
						int flags = mEdgeFlags[row][col];
						if( (flags & EDGE_FLAG_INVISIBLE) != 0) continue;
						Point from = mStationPoints[row];
						Point to = mStationPoints[col];
						canvas.drawCircle(from.x, from.y, radius, whitePaint);
						canvas.drawCircle(to.x, to.y, radius, whitePaint);
						canvas.drawLine(from.x, from.y, to.x, to.y, whitePaint);
					}
				}
			}
		}

		private void renderTransportLines(Canvas canvas) {
			Date startTimestamp = new Date();
			final int lineCount = mLineCount;
			final int stationCount = mStationCount; 
			for(int line = 0; line < lineCount; line++){
				mLinePaint.setColor(mLineColors[line]);
				Path path = new Path();
				for(int row = 0; row < stationCount; row++){
					for(int col = 0; col < row; col++){
						if( mEdgeDelays[row][col] != null && 
								mEdgeLines[row][col] == line )
						{
							int flags = mEdgeFlags[row][col];
							Point from = mStationPoints[row];
							Point to = mStationPoints[col];
							Point additionalNode = mEdgeAdditionalNodes[row][col];
							if(additionalNode!=null){
								if( (flags & EDGE_FLAG_SPLINE) != 0 ){
									drawArc(canvas, from.x,from.y,additionalNode.x, additionalNode.y, to.x, to.y, mLinePaint);
								}else{
									path.moveTo(from.x, from.y);
									path.lineTo(additionalNode.x, additionalNode.y);
									path.lineTo(to.x, to.y);
								}
							}else{
								path.moveTo(from.x, from.y);
								path.lineTo(to.x, to.y);
							}
						}
					}
				}
				canvas.drawPath(path, mLinePaint);
			}
			Log.d("aMetro", String.format("Rendering transport lines is %sms", Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
		}


		private float calculateAngle( float x0, float y0, float x, float y )
		{
			float angle = (float)(Math.atan( (y-y0)/(x-x0) ) / Math.PI * 180);
			float dx = x-x0;
			float dy = y-y0;
			if( angle > 0 ){
				if( dx < 0 && dy < 0 ){
					angle += 180;
				}
			}else if(angle < 0){
				if( dx < 0 && dy > 0 ){
					angle += 180;
				}else{
					angle += 360;
				}
			}else{
				if (dx<0)
				{
					angle = 180;
				}
			}
			return angle;
		}

		private void drawArc(Canvas canvas, float x1, float y1, float x2, float y2, float x3, float y3, Paint linePaint) {
			float x12 = (x1+x2)/2;
			float y12 = (y1+y2)/2;
			float x23 = (x2+x3)/2;
			float y23 = (y2+y3)/2;
			float k12 = -(x1-x2)/(y1-y2); 
			float b12 = y12 - k12 * x12;
			float k23 = -(x2-x3)/(y2-y3); 
			float b23 = y23 - k23 * x23;

			float y0 = (k12*b23 - k23*b12)/(k12-k23);
			float x0 = (y0 - b12)/k12;

			float R = (float)Math.sqrt(  (x0-x1)*(x0-x1) + (y0-y1)*(y0-y1)  );

			float angle1 = calculateAngle(x0, y0, x1, y1); //(float)(Math.atan( (y1-y0)/(x1-x0) ) / Math.PI * 180);
			float angle3 = calculateAngle(x0, y0, x3, y3); //(float)(Math.atan( (y3-y0)/(x3-x0) ) / Math.PI * 180);

			float startAngle = Math.min(angle1, angle3);
			float endAngle = Math.max(angle1, angle3);
			float sweepAngle = endAngle-startAngle;
			if(startAngle<90 && endAngle>270){
				//sweepAngle = startAngle - end
				sweepAngle = 360 - endAngle + startAngle;
				startAngle = endAngle;
			}

			RectF oval = new RectF(x0-R, y0-R, x0+R, y0+R);
			canvas.drawArc(oval, startAngle, sweepAngle, false, linePaint);


		}

		private void renderStations(Canvas canvas) {
			final int stationCount = mStationCount; 
			float radius = (float)mStationDiameter/2.0f;
			for(int station = 0; station < stationCount; station++){
				if(mStationLine[station]!=null && mStationNames[station]!=null){
					Point point = mStationPoints[station];
					int color =  mLineColors[mStationLine[station]];
					if(point.y < 50){
						point.y=point.y+50;
					}

					mStationFillPaint.setColor(color);
					mTextPaint.setColor(color);
					canvas.drawCircle(point.x, point.y, radius, mStationFillPaint);
					canvas.drawCircle(point.x, point.y, radius, mStationBorderPaint);

					String name = mStationNames[station];
					Rect rect = mStationBoxes[station];
					if(rect!=null && name!=null){
						drawText(canvas, mUpperCase ? name.toUpperCase() : name, rect, point);
					}

				}
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
				final int right = rect.right;
				final int bottom = rect.bottom;
				mTextPaint.getTextBounds(text, 0, text.length()-1, bounds);
				textPath.moveTo(right,bottom);
				textPath.lineTo(right, rect.top);
				canvas.drawRect(right-bounds.height(), bottom - bounds.width()-10, right+2, bottom, mFillPaint);
				canvas.drawTextOnPath(text, textPath, 0, 0, mTextPaint);
			}
		}

		private void drawRectText(Canvas canvas, String text, Rect rect, Point align) {
			Rect bounds = new Rect();
			mTextPaint.getTextBounds(text, 0, text.length(), bounds);
			if(bounds.width()>rect.width() && mWordWrap){
				int space = text.indexOf(' ');
				if(space!=-1){
					int offset = drawTextOnWhite(canvas, text.substring(0, space), rect, align);
					rect.offset(0, offset+2);
					drawTextOnWhite(canvas, text.substring(space+1), rect, align);
					return;
				}
			}
			drawTextOnWhite(canvas, text, rect, align);
		}

		private int drawTextOnWhite(Canvas canvas, String text, Rect rect, Point align) {
			final int len = text.length();
			final int top = rect.top;
			final int left = rect.left;
			final int right = rect.right;
			Rect bounds = new Rect();
			Rect fill = new Rect();
			if(align.x > rect.centerX()){ // align to right
				mTextPaint.setTextAlign(Align.RIGHT);
				mTextPaint.getTextBounds(text, 0, len, bounds);
				fill.set(right-bounds.width()-1, top, right+2, top + bounds.height()+1);
				canvas.drawRect(fill, mFillPaint);
				canvas.drawText(text, right, top+bounds.height(), mTextPaint);
			}else{ // align to left
				mTextPaint.setTextAlign(Align.LEFT);
				mTextPaint.getTextBounds(text, 0, len, bounds);
				fill.set(left-1, top, left+bounds.width()+2, top+bounds.height()+1);
				canvas.drawRect(fill, mFillPaint);
				canvas.drawText(text, left, top+bounds.height(), mTextPaint);
			}
			return bounds.height();
		}

	}

}
