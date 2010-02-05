package com.ametro.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

import com.ametro.model.Line;
import com.ametro.model.Model;
import com.ametro.model.Station;

public class RenderStationName extends RenderElement {

	private boolean mIsFilled;
	
	private Paint mTextPaint;
	private Paint mFillPaint;
	
	private String mTextFirstLine;
	private Path mPathFirstLine;
	private Rect mRectFirstLine;
	private Point mPointFirstLine;

	private String mTextSecondLine;
	private Path mPathSecondLine;
	private Rect mRectSecondLine;
	private Point mPointSecondLine;
	
	public RenderStationName(Model model, Station station){
		final boolean isUpperCase = model.isUpperCase();
		final boolean isWordWrap = model.isWordWrap();
		
		final String text = isUpperCase ?  station.getName().toUpperCase() : station.getName();
		final int textLength = text.length();
		final Rect rect = station.getRect();
		final Point point = station.getPoint();
		
		final Line line = station.getLine();
		final int textColor = line.getLabelColor();
		final int backColor = line.getLabelBgColor();
		
		final Paint textPaint = new Paint();
		final Paint fillPaint = new Paint();

		initializePaints(textColor, backColor, textPaint, fillPaint);
		mIsFilled = backColor!=0;
		
		if(rect.width() > rect.height()){
			// horizontal text
			final Rect bounds = new Rect();
			textPaint.getTextBounds(text, 0, textLength, bounds);			
			boolean isNeedSecondLine =  bounds.width()>rect.width() && isWordWrap;
			int spacePosition = -1; 
			if(isNeedSecondLine){
				spacePosition = text.indexOf(' ');
				isNeedSecondLine = spacePosition!=-1;
			}
			if(isNeedSecondLine){
				final String firstText = text.substring(0, spacePosition);
				final int firstLength = firstText.length();
				
				final String secondText = text.substring(spacePosition+1);
				final int secondLength = secondText.length();
				
				final Rect secondRect = new Rect(
						rect.left, 
						rect.top+bounds.height()+2, 
						rect.right, 
						rect.bottom+bounds.height()+2);
				
				initializeFirstHorizontalLine(firstText, firstLength, rect, point);
				initializeSecondHorizontalLine(secondText, secondLength, secondRect, point);
				
				
			}else{
				initializeFirstHorizontalLine(text, textLength, rect, point);
			}
			
			
		}else{
			// vertical text
			initializeVerticalText(text, textLength, rect, point);
		}

		Rect box = new Rect( mRectFirstLine );
		if(mRectSecondLine!=null){
			box.union(mRectSecondLine);
		}
		setProperties(RenderProgram.TYPE_STATION_NAME, box);
	}

	private void initializeSecondHorizontalLine(final String text,
			final int textLength, final Rect rect, final Point point) {
		final Rect fill = new Rect();
		final Point position = new Point();
		final Rect bounds = new Rect();
		mTextPaint.getTextBounds(text, 0, textLength, bounds);				
		
		final int top = rect.top;
		final int left = rect.left;
		final int right = rect.right;
		if(point.x > rect.centerX()){ // align to right
			mTextPaint.setTextAlign(Align.RIGHT);
			mTextPaint.getTextBounds(text, 0, textLength, bounds);
			fill.set(right-bounds.width()-1, top, right+2, top + bounds.height()+1);
			position.set(right, top+bounds.height());
			
		}else{ // align to left
			mTextPaint.setTextAlign(Align.LEFT);
			mTextPaint.getTextBounds(text, 0, textLength, bounds);
			fill.set(left-1, top, left+bounds.width()+2, top+bounds.height()+1);
			position.set(left, top+bounds.height());
		}
		mTextSecondLine = text;
		mRectSecondLine = fill;
		mPathSecondLine = null;
		mPointSecondLine = position;
	}
	
	private void initializeFirstHorizontalLine(final String text,
			final int textLength, final Rect rect, final Point point) {
		Rect fill = new Rect();
		Point position = new Point();
		final Rect bounds = new Rect();
		mTextPaint.getTextBounds(text, 0, textLength, bounds);				

		final int top = rect.top;
		final int left = rect.left;
		final int right = rect.right;
		if(point.x > rect.centerX()){ // align to right
			mTextPaint.setTextAlign(Align.RIGHT);
			mTextPaint.getTextBounds(text, 0, textLength, bounds);
			fill.set(right-bounds.width()-1, top, right+2, top + bounds.height()+1);
			position.set(right, top+bounds.height());
			
		}else{ // align to left
			mTextPaint.setTextAlign(Align.LEFT);
			mTextPaint.getTextBounds(text, 0, textLength, bounds);
			fill.set(left-1, top, left+bounds.width()+2, top+bounds.height()+1);
			position.set(left, top+bounds.height());
		}
		mTextFirstLine = text;
		mRectFirstLine = fill;
		mPathFirstLine = null;
		mPointFirstLine = position;
	}

	private void initializePaints(final int textColor, final int backColor,
			final Paint textPaint, final Paint fillPaint) {
		textPaint.setAntiAlias(true);
		textPaint.setTypeface(Typeface.DEFAULT);
		textPaint.setFakeBoldText(true);
		textPaint.setTextSize(10);
		textPaint.setTextAlign(Align.LEFT);		
		textPaint.setColor(textColor);
		mTextPaint = textPaint;
		
		fillPaint.setColor(backColor);
		fillPaint.setStyle(Style.FILL);		
		mFillPaint = fillPaint;
	}

	private void initializeVerticalText(final String text, final int textLength, final Rect rect, final Point point) {
		final Path textPath = new Path(); 
		final Rect bounds = new Rect();
		mTextPaint.getTextBounds(text, 0, textLength, bounds);
		final int right = rect.right;
		final int bottom = rect.bottom;
		final int top = rect.top;
		final int textHeight = bounds.height();
		final int textWidth = bounds.width();
		Rect textRect; 
		if(point.y > rect.centerY()){
			textRect = new Rect(right - textHeight, bottom - textWidth - 20, right, bottom-5);

		}else{
			textRect = new Rect(right - textHeight, top+5, right, top + textWidth+20);
		}
		textPath.moveTo(textRect.right, textRect.bottom);
		textPath.lineTo(textRect.right, textRect.top);
		
		mTextFirstLine = text;
		mRectFirstLine = textRect;
		mPathFirstLine = textPath;
		mPointFirstLine = null;
	}
	
	@Override
	public void draw(Canvas canvas) {
		final boolean isFilled = mIsFilled;
		
		if(isFilled)canvas.drawRect(mRectFirstLine, mFillPaint);
		if(mPointFirstLine!=null){
			canvas.drawText(mTextFirstLine, mPointFirstLine.x, mPointFirstLine.y,  mTextPaint);
		}else{
			canvas.drawTextOnPath(mTextFirstLine, mPathFirstLine, 0, 0, mTextPaint);
		}
		
		if(mTextSecondLine!=null){
			if(isFilled)canvas.drawRect(mRectSecondLine, mFillPaint);
			if(mPointSecondLine!=null){
				canvas.drawText(mTextSecondLine, mPointSecondLine.x, mPointSecondLine.y,  mTextPaint);
			}else{
				canvas.drawTextOnPath(mTextSecondLine, mPathSecondLine, 0, 0, mTextPaint);
			}
		}
	}


}
