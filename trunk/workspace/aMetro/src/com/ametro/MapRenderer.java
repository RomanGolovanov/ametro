package com.ametro;

import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.ametro.resources.MapResource;
import com.ametro.resources.Point;
import com.ametro.resources.TransportLine;

public class MapRenderer {

	Paint mStationBorderPaint;
	Paint mStationFillPaint;
	Paint mLinePaint;
	
	public MapRenderer(){
		mStationFillPaint = new Paint();
		mStationFillPaint.setStyle(Style.FILL);
		
		mStationBorderPaint = new Paint();
		mStationBorderPaint.setStyle(Style.STROKE);
		mStationBorderPaint.setStrokeWidth(0);
		
		mLinePaint = new Paint();
		mLinePaint.setStyle(Style.STROKE);
		
		mLinePaint.setAntiAlias(true);
		mStationBorderPaint.setAntiAlias(true);
	}
	
	public void renderMap(Canvas canvas, MapResource map) {
		if(map == null){
			return;
		}
		Hashtable<String, TransportLine> transportLines = map.getTransportLines();
		int diameter = map.getStationDiameter();
		int radius = diameter/2;
		int lineWidth = map.getLinesWidth();

		mLinePaint.setStrokeWidth( lineWidth );
		
		canvas.drawColor(Color.WHITE);
		
		Iterator<TransportLine> lines;
		lines = transportLines.values().iterator();
		while(lines.hasNext()){
			TransportLine line = lines.next();
			if(line.coordinates != null){
				drawTransportLine(lineWidth, canvas, line);
			}
		}
		lines = transportLines.values().iterator();
		while(lines.hasNext()){
			TransportLine line = lines.next();
			if(line.coordinates != null){
				drawTransportStations(diameter, radius, canvas, line);
			}
		}
		canvas.save();
	}
	
	private void drawTransportStations(int diameter, int radius, Canvas c, TransportLine line) {
		Iterator<Point> points = line.coordinates.iterator();
		while(points.hasNext()){
			Point p = points.next();
			mStationFillPaint.setColor( 0xFF000000 | line.linesColor  );
			c.drawCircle(p.x, p.y, radius, mStationFillPaint);
			mStationBorderPaint.setColor( Color.WHITE  );
			c.drawCircle(p.x, p.y, radius, mStationBorderPaint);
			
		}
	}

	private void drawTransportLine(int lineWidth, Canvas c, TransportLine line) {
		Point start ;
		Point end;
		int cnt = line.coordinates.size();
		start = line.coordinates.get(0);
		end = line.coordinates.get(cnt-1);
		
		float[] pts  = new float[cnt*4];
		for(int i = 0; i < cnt-1; i++){
			end = line.coordinates.get(i+1);
			pts[4*i] = start.x;
			pts[4*i+1] = start.y;
			pts[4*i+2] = end.x;
			pts[4*i+3] = end.y;
			start = end;
		}
		mLinePaint.setColor( 0xFF000000 | line.linesColor  );
		c.drawLines(pts, mLinePaint);
	}		
}
