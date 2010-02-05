package com.ametro.render;

import com.ametro.model.Model;
import com.ametro.model.Station;
import com.ametro.model.Transfer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;

public class RenderTransferBackground extends RenderElement {

	private int FromX;
	private int FromY;
	private int ToX;
	private int ToY;
	private float RadiusBig;
	private Paint Paint;
	
	public RenderTransferBackground(Model model, Transfer transfer) {
		super();
		final Station fromStation = transfer.getFrom();
		final Point from = fromStation.getPoint();
		FromX = from.x;
		FromY = from.y;
		
		final Station toStation = transfer.getTo();
		final Point to = toStation.getPoint();
		ToX = to.x;
		ToY = to.y;
		
		final int lineWidth = model.getLinesWidth(); 
		final int radius = model.getStationDiameter()/2;
		
		RadiusBig = (float)radius + 3.5f;

		final Paint paint =new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.FILL);
		paint.setStrokeWidth(lineWidth+3.5f);
		paint.setAntiAlias(true);
		Paint = paint;
		
		final int left = Math.min(FromX, ToX) - radius;
		final int right = Math.max(FromX, ToX) + radius;
		final int top = Math.min(FromY, ToY) - radius;
		final int bottom = Math.max(FromY, ToY) + radius;

		setProperties(RenderProgram.TYPE_TRANSFER_BACKGROUND, new Rect(left,top,right,bottom));
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawCircle(FromX, FromY, RadiusBig, Paint);
		canvas.drawCircle(ToX, ToY, RadiusBig, Paint);
		canvas.drawLine(FromX, FromY, ToX, ToY, Paint);
	}

}
