package com.ametro.model;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import com.ametro.libs.ExtendedPath;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.Log;
import android.widget.ToggleButton;

public class TransportMapRenderer {

	private TransportMap mMap;

	private Paint mStationBorderPaint;
	private Paint mStationFillPaint;
	private Paint mLinePaint;
	private Paint mLineUnavailablePaint;
	private Paint mTextPaint;
	private Paint mFillPaint;

	private int mLinesWidth;
	private boolean mWordWrap;
	private boolean mUpperCase;
	private int mStationDiameter;

	public TransportMapRenderer(TransportMap map) {
		mMap = map;

		mLinesWidth = mMap.getLinesWidth();
		mWordWrap = mMap.isWordWrap();
		mUpperCase = mMap.isUpperCase();
		mStationDiameter = mMap.getStationDiameter();

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
	}

	public void prepareObjects(){
		mLinePaint.setStrokeWidth( mLinesWidth );
		mLineUnavailablePaint.setStrokeWidth( mLinesWidth*0.8f );
		mLineUnavailablePaint.setPathEffect(new DashPathEffect(new float[]{ mLinesWidth*0.8f, mLinesWidth*0.4f }, 0));
	}


	private void render(Canvas canvas){
		Date startTimestamp = new Date();
		prepareObjects();
		canvas.drawColor(Color.WHITE);
		drawLines(canvas);
		drawTransfers(canvas);
		drawStations(canvas);			
		canvas.save();
		Log.d("aMetro", String.format("Overall rendering time is %sms", Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
	}

	public void render(Canvas canvas, Rect src)
	{
		if(src!=null){
			canvas.translate(-src.left, -src.top);
		}
		canvas.translate(50, 50);
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


		for (Iterator<Transfer> transfers = mMap.getTransfers(); transfers.hasNext();) {
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
		for (Iterator<Transfer> transfers = mMap.getTransfers(); transfers.hasNext();) {
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
		Enumeration<Line> lines = mMap.getLines();
		while(lines.hasMoreElements()){
			Line line = lines.nextElement();
			for (Iterator<Segment> segments = line.getSegments(); segments.hasNext();) {
				Segment segment = segments.next();
				drawSegment(canvas, line, segment);					 
			}			
		}		

	}

	private void drawSegment(Canvas canvas, Line line, Segment segment) {
		Station from = segment.getFrom();
		Station to = segment.getTo();

		if(from.getPoint()==null || to.getPoint()==null){
			Log.e("aMetro", 
					"Error rendering line segment on line " + line.getName()
					+ " from " + from.getName() 
					+ " to " + to.getName() );
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
		Enumeration<Line> lines = mMap.getLines();
		while(lines.hasMoreElements()){
			Line line = lines.nextElement();
			Enumeration<Station> stations = line.getStations();
			while(stations.hasMoreElements()){
				Station station = stations.nextElement();
				drawStation(canvas, radius, station);
			}
		}		
	}

	private void drawStation(Canvas canvas, float radius, Station station) {
		Point point = station.getPoint();;
		int color =  station.getLine().getColor();
		boolean hasConnections = station.hasConnections();
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
		String name = station.getName();
		Rect rect = station.getRect();
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
