package com.ametro.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;

import com.ametro.model.Model;
import com.ametro.model.Station;

public class RenderStation extends RenderElement {

	public int X;
	public int Y;
	public float RadiusFirst;
	public float RadiusSecond;
	public Paint PaintFirst;
	public Paint PaintSecond;

	public RenderStation(Model model, Station station) {
		super();
		final boolean hasConnections = station.hasConnections();

		final int x = station.getPoint().x;
		final int y = station.getPoint().y;
		final int radius = model.getStationDiameter()/2;
		final float radiusFirst = (float)radius;
		final Paint paintFirst = new Paint();
		final Paint paintSecond= new Paint();

		paintFirst.setColor(station.getLine().getColor());
		paintFirst.setStyle(Style.FILL);
		paintFirst.setAntiAlias(true);

		paintSecond.setColor(Color.WHITE);
		paintSecond.setAntiAlias(true);

		float radiusSecond;
		if(hasConnections){
			radiusSecond = radiusFirst;
			paintSecond.setStyle(Style.STROKE);
			paintSecond.setStrokeWidth(0);
		}else{
			radiusSecond = radiusFirst * 0.7f;
			paintSecond.setStyle(Style.FILL);
		}

		this.X = x;
		this.Y = y;
		this.RadiusFirst = radiusFirst;
		this.RadiusSecond = radiusSecond;
		this.PaintFirst = paintFirst; 
		this.PaintSecond = paintSecond;

		setProperties(RenderProgram.TYPE_STATION, new Rect(x-radius,y-radius,x+radius,y+radius));
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawCircle(X, Y, RadiusFirst, PaintFirst);
		canvas.drawCircle(X, Y, RadiusSecond, PaintSecond);
	}

}
