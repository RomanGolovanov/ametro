package com.ametro.model;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.Log;

import com.ametro.libs.ExtendedPath;
import com.ametro.libs.Helpers;

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
		mLineIndex = new Hashtable<Integer,String>();
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

	public Integer getStationId(String lineName, String name){
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

	public void addLineSegment(int lineId, int fromId, int toId, Double delay )
	{
		addGenericEdge(lineId, fromId, toId, delay, false, false, null, 0);
	}

	public void addTransfer(int fromId, int toId, Double delay, int flags)
	{
		addGenericEdge(null, fromId, toId, delay, true, false, null, flags);
	}

	public void addLineSegmentPoly(int fromId, int toId, Point[] additionalNode)
	{
		mEdgeAdditionalNodes[fromId][toId] = additionalNode;
	}

	public void addLineSegmentSpline(int fromId, int toId, Point[] additionalNode)
	{
		if( (mEdgeFlags[fromId][toId] & EDGE_FLAG_CREATED) != 0){
			mEdgeAdditionalNodes[fromId][toId] = additionalNode;
			int flags = mEdgeFlags[fromId][toId] | EDGE_FLAG_SPLINE;
			mEdgeFlags[fromId][toId] = flags;
		}else{
			Point[] revertedNodes = new Point[additionalNode.length];
			for (int i = 0; i < additionalNode.length; i++) {
				revertedNodes[additionalNode.length-i-1] = additionalNode[i];
			}
			mEdgeAdditionalNodes[toId][fromId] = revertedNodes;
			int flags = mEdgeFlags[toId][fromId] | EDGE_FLAG_SPLINE;
			mEdgeFlags[toId][fromId] = flags;
		}

	}


	public int addStation(int lineId, String name, Rect rect, Point point)
	{
		String lineName = getLineNameById(lineId);
		Integer id = mStationNameIndex.get(lineName + ";" + name);
		if(id==null){
			id = addGenericStation(lineId, name, rect, point);
		}else{
			mStationBoxes[id] = rect;
			mStationPoints[id] = point;
		}
		return id;
	}

	private String getLineNameById(int lineId) {
		return mLineIndex.get(lineId);
	}

	public int addStation(int lineId,String name)
	{
		String lineName = getLineNameById(lineId);
		Integer id = mStationNameIndex.get(lineName + ";" + name);
		if(id==null){
			id = addGenericStation(lineId, name,null,null);
		}
		return id;
	}

	public void setDimensions(int width, int height) {
		mWidth = width;
		mHeight = height;
	}

	public boolean isExistEdge(int fromStationId, int toStationId) {
		return (mEdgeFlags[fromStationId][toStationId] & EDGE_FLAG_CREATED)!=0
		|| (mEdgeFlags[toStationId][fromStationId] & EDGE_FLAG_CREATED)!=0;
	}

	public boolean isExistEdgeStrict(int fromStationId, int toStationId) {
		return (mEdgeFlags[fromStationId][toStationId] & EDGE_FLAG_CREATED)!=0;
	}

	public int addLine(String name, int color){
		Integer id = mLineNameIndex.get(name);
		if(id==null){
			id = getNextLineId();
			mLineNames[id] = name;
			mLineColors[id] = color;
			mLineNameIndex.put(name, id);
			mLineIndex.put(id,name);
		}
		return id;
	}

	public Integer getLineId(String name){
		return mLineNameIndex.get(name);
	}

	private int addGenericStation(Integer lineId, String name, Rect rect, Point point)
	{
		int id = getNextStationId();
		String lineName = getLineNameById(lineId);
		mStationNames[id] = name;
		mStationBoxes[id] = rect;
		mStationPoints[id] = point;
		mStationLine[id] = lineId;
		mStationNameIndex.put(lineName + ";" + name, id);
		return id;
	}

	private void addGenericEdge(Integer lineId, int fromId, int toId, Double delay, boolean isTransfer, boolean isSpline, Point[] additionalNode, int flags )
	{
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

		mEdgeDelays[fromId][toId] = delay != null ? delay : mEdgeDelays[fromId][toId];
		mEdgeAdditionalNodes[fromId][toId] = additionalNode;
		mEdgeLines[fromId][toId] = lineId != null ? lineId : LINE_TRANSFER;
		mEdgeFlags[fromId][toId] = flags;

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
	private Object[][]	mEdgeAdditionalNodes;
	private int[][] 	mEdgeFlags;
	private Integer[][]	mEdgeLines;

	private Dictionary<String, Integer> mLineNameIndex;
	private Dictionary<Integer,String> mLineIndex;

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
		Paint mLineUnavailablePaint;
		Paint mTextPaint;
		Paint mFillPaint;

		public MapRenderer(){
			mFillPaint = new Paint();
			mFillPaint.setColor(Color.WHITE);
			mFillPaint.setStyle(Style.FILL);
			mFillPaint.setAntiAlias(true);

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

			mLineUnavailablePaint = new Paint(mLinePaint);

			mTextPaint = new Paint();
			mTextPaint.setAntiAlias(true);
			mTextPaint.setStyle(Style.FILL_AND_STROKE);
			mTextPaint.setStrokeWidth(0);
			mTextPaint.setTextAlign(Align.LEFT);
			//mTextPaint.setTypeface(Typeface.MONOSPACE);

		}

		public void prepareObjects(){
			mLinePaint.setStrokeWidth( mLinesWidth );
			mLineUnavailablePaint.setStrokeWidth(mLinesWidth);
			mLineUnavailablePaint.setPathEffect(new DashPathEffect(new float[]{ mLinesWidth, mLinesWidth/3 }, 0));
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
				matrix.setTranslate(-src.left, -src.top);
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
					if(mEdgeLines[row][col] != null && mEdgeLines[row][col] == LINE_TRANSFER){
						int flags = mEdgeFlags[row][col];
						if( (flags & EDGE_FLAG_INVISIBLE) != 0) continue;
						Point from = mStationPoints[row];
						Point to = mStationPoints[col];
						if(from!=null && to!=null){
							canvas.drawCircle(from.x, from.y, radiusBig, blackPaint);
							canvas.drawCircle(to.x, to.y, radiusBig, blackPaint);
							canvas.drawLine(from.x, from.y, to.x, to.y, blackPaint);
						}
					}
				}
			}
			for(int row = 0; row < stationCount; row++){
				for(int col = 0; col < row; col++){
					if(mEdgeLines[row][col] != null && mEdgeLines[row][col] == LINE_TRANSFER){
						int flags = mEdgeFlags[row][col];
						if( (flags & EDGE_FLAG_INVISIBLE) != 0) continue;
						Point from = mStationPoints[row];
						Point to = mStationPoints[col];
						if(from!=null && to!=null){						
							canvas.drawCircle(from.x, from.y, radius, whitePaint);
							canvas.drawCircle(to.x, to.y, radius, whitePaint);
							canvas.drawLine(from.x, from.y, to.x, to.y, whitePaint);
						}
					}
				}
			}
		}

		private void renderTransportLines(Canvas canvas) {
			Date startTimestamp = new Date();
			final int stationCount = mStationCount; 
			for(int row = 0; row < stationCount; row++){
				for(int col = 0; col < row; col++){
					if((mEdgeFlags[row][col] & EDGE_FLAG_CREATED )!=0 || (mEdgeFlags[col][row] & EDGE_FLAG_CREATED )!=0)
					{
						int line = (mEdgeLines[row][col] != null) ? mEdgeLines[row][col] : mEdgeLines[col][row];
						if(line == LINE_TRANSFER) continue;
						
						boolean lineWorking = (mEdgeDelays[row][col] != null && mEdgeDelays[row][col] != 0 ) || (mEdgeDelays[col][row] != null && mEdgeDelays[col][row] != 0 );
						//if(lineWorking) continue;

						Paint linePaint = lineWorking ? mLinePaint : mLineUnavailablePaint;
						linePaint.setColor(mLineColors[line]);
						ExtendedPath path = new ExtendedPath();

						boolean additionalForward = mEdgeAdditionalNodes[row][col]!=null;
						boolean additionalBackward = mEdgeAdditionalNodes[col][row]!=null;
						if(!additionalForward && additionalBackward){
							drawLineSegment(line, path, col, row);
						}else{
							drawLineSegment(line, path, row, col);
						}
						canvas.drawPath(path, linePaint);
					}
				}
			}
//			for(int row = 0; row < stationCount; row++){
//				for(int col = 0; col < row; col++){
//					if((mEdgeFlags[row][col] & EDGE_FLAG_CREATED )!=0 || (mEdgeFlags[col][row] & EDGE_FLAG_CREATED )!=0)
//					{
//						int line = (mEdgeLines[row][col] != null) ? mEdgeLines[row][col] : mEdgeLines[col][row];
//						if(line == LINE_TRANSFER) continue;
//						
//						boolean lineWorking = (mEdgeDelays[row][col] != null && mEdgeDelays[row][col] != 0 ) || (mEdgeDelays[col][row] != null && mEdgeDelays[col][row] != 0 );
//						if(!lineWorking) continue;
//
//						Paint linePaint = lineWorking ? mLinePaint : mLineUnavailablePaint;
//						linePaint.setColor(mLineColors[line]);
//						ExtendedPath path = new ExtendedPath();
//
//						boolean additionalForward = mEdgeAdditionalNodes[row][col]!=null;
//						boolean additionalBackward = mEdgeAdditionalNodes[col][row]!=null;
//						if(!additionalForward && additionalBackward){
//							drawLineSegment(line, path, col, row);
//						}else{
//							drawLineSegment(line, path, row, col);
//						}
//						canvas.drawPath(path, linePaint);
//					}
//				}
//			}
			Log.d("aMetro", String.format("Rendering transport lines is %sms", Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
		}

		private void drawLineSegment(int line, ExtendedPath path, int row, int col) {
			int flags = mEdgeFlags[row][col];
			Point from = mStationPoints[row];
			Point to = mStationPoints[col];
			if(from==null || to==null){
				Log.e("aMetro", 
						"Error rendering line segment on line " + getLineNameById(line)
						+ " from " + mStationNames[row] 
						                           + " to " + mStationNames[col] );
				return;
			}
			Point[] additionalNode = (Point[])mEdgeAdditionalNodes[row][col];
			if(additionalNode!=null){
				if( (flags & EDGE_FLAG_SPLINE) != 0 ){
					Point[] points = new Point[additionalNode.length+2];
					points[0] = from;
					points[points.length-1] = to;
					for (int i = 0; i < additionalNode.length; i++) {
						Point point = additionalNode[i];
						points[i+1] = point;
					}
					path.drawSpline(points, 0, points.length);
				}else{
					path.moveTo(from.x, from.y);
					for (int i = 0; i < additionalNode.length; i++) {
						path.lineTo(additionalNode[i].x, additionalNode[i].y);	
					}
					path.lineTo(to.x, to.y);
				}
			}else{
				path.moveTo(from.x, from.y);
				path.lineTo(to.x, to.y);
			}

		}

		private void renderStations(Canvas canvas) {
			final int stationCount = mStationCount; 
			float radius = (float)mStationDiameter/2.0f;
			for(int station = 0; station < stationCount; station++){
				if(mStationLine[station]!=null && mStationNames[station]!=null && mStationPoints[station]!=null){
					drawStation(canvas, radius, station);
				}else{
					Log.e("aMetro",
							"Error rendering station #" + station + 
							"(name " + mStationNames[station] +  
					") due unsufficient data");
				}
			}
		}

		private void drawStation(Canvas canvas, float radius, int station) {
			Point point = mStationPoints[station];
			int color =  mLineColors[mStationLine[station]];

			boolean hasConnections = false;
			for(int i = 0; i < mStationCount; i++)
			{
				if( 
						((mEdgeFlags[station][i] & EDGE_FLAG_CREATED)!=0 && mEdgeDelays[station][i] != null && mEdgeDelays[station][i] != 0 )		
						||
						((mEdgeFlags[i][station] & EDGE_FLAG_CREATED)!=0 && mEdgeDelays[i][station] != null && mEdgeDelays[i][station] != 0 )		
				){
					hasConnections = true;
					break;
				}
			}
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

			mTextPaint.setColor(color);
			String name = mStationNames[station];
			Rect rect = mStationBoxes[station];
			if(rect!=null && name!=null){
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
					drawTextOnWhite(canvas, text.substring(0, space), rect, align);
					Rect nextLineRect = new Rect(rect.left, rect.top+bounds.height()+2, rect.right, rect.bottom+bounds.height()+2);
					drawTextOnWhite(canvas, text.substring(space+1), nextLineRect, align);
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


	public Double getLineDelay(int from, int to) {
		return mEdgeDelays[from][to];
	}

}
