/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.render;

import org.ametro.model.SchemeView;
import org.ametro.model.StationView;
import org.ametro.model.TransportStation;
import org.ametro.model.util.ModelUtil;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class RenderStationName extends RenderElement {

	private boolean mVertical;
	
	private Paint mTextPaint;
	private Paint mBorderPaint;

	private String mTextFirstLine;
	private Point mPointFirstLine;

	private String mTextSecondLine;
	private Point mPointSecondLine;

    private int colorNormal;
    private int colorGrayed;
    
    private int colorBackgroundNormal;
    private int colorBackgroundGrayed;
	
	public RenderStationName(SchemeView map, StationView view, TransportStation station) {
		final boolean isUpperCase = map.isUpperCase;
		final boolean isWordWrap = map.isWordWrap;

		final String text = isUpperCase ? station.getName().toUpperCase() : station.getName();
		final int textLength = text.length();
		final Rect textRect = ModelUtil.toRect( view.stationNameRect );
		final Point point = ModelUtil.toPoint( view.stationPoint );

        colorNormal = map.lines[view.lineViewId].labelColor;
        colorGrayed = RenderProgram.getGrayedColor(colorNormal);

        colorBackgroundNormal = map.lines[view.lineViewId].labelBackgroundColor;
        if(colorBackgroundNormal==-1){
        	colorBackgroundNormal = Color.WHITE;
        	colorBackgroundGrayed = Color.WHITE;
        }else{
            colorBackgroundGrayed = RenderProgram.getGrayedColor(colorBackgroundNormal);
        }
        
		final Paint textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setTypeface(Typeface.DEFAULT);
		textPaint.setFakeBoldText(true);
		textPaint.setTextSize(10);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setColor(colorNormal);
		textPaint.setStyle(Style.FILL);

		final Paint borderPaint = new Paint(textPaint);
		borderPaint.setColor(Color.WHITE);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(3);
		
		final boolean vertical = textRect.width() < textRect.height();

		final Align align = vertical 
			? ((point.y > textRect.centerY()) ? Align.LEFT : Align.RIGHT)
			: ((point.x > textRect.centerX() ? Align.RIGHT : Align.LEFT));
		Rect rect;
		if(vertical){
			if(align == Align.LEFT){
				rect = new Rect(textRect.left, textRect.bottom, textRect.left + textRect.height(), textRect.bottom + textRect.width());
			}else{
				rect = new Rect(textRect.left - textRect.height(), textRect.top, textRect.left , textRect.top + textRect.width());
			}
		}else{
			rect = new Rect(textRect);
		}

		final Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, textLength, bounds);
		boolean isNeedSecondLine = bounds.width() > rect.width() && isWordWrap;
		int spacePosition = -1;
		if (isNeedSecondLine) {
			spacePosition = text.indexOf(' ');
			isNeedSecondLine = spacePosition != -1;
		}

		if (isNeedSecondLine) {
			final String firstText = text.substring(0, spacePosition);

			final String secondText = text.substring(spacePosition + 1);

			final Rect secondRect = new Rect(rect.left, rect.top
					+ bounds.height() + 2, rect.right, rect.bottom
					+ bounds.height() + 2);

			mTextFirstLine = firstText;
			mPointFirstLine = initializeLine(firstText, vertical, rect, textPaint, align);
			
			mTextSecondLine = secondText;
			mPointSecondLine = initializeLine(secondText, vertical,  secondRect, textPaint, align);
			mPointSecondLine.offset(-mPointFirstLine.x, -mPointFirstLine.y);
			

		} else {
			mTextFirstLine = text;
			mPointFirstLine = initializeLine(text, vertical,  rect, textPaint, align);
		}

		mTextPaint = textPaint;
		mBorderPaint = borderPaint;
		
		mVertical = vertical;
		mTextPaint.setTextAlign(align);
		mBorderPaint.setTextAlign(align);

		Rect box = new Rect(textRect);
		setProperties(RenderProgram.TYPE_STATION_NAME + view.id, box);
	}

	private static Point initializeLine(final String text, boolean vertical, final Rect rect, final Paint paint, final Align align) {
		Point position = new Point();
		final Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		if (align == Align.RIGHT) { // align to right
			position.set(rect.right + (vertical ? bounds.height() : 0 ) , rect.top + (vertical ? 0 : bounds.height()));
		} else { // align to left
			position.set(rect.left + (vertical ? bounds.height() : 0 ), rect.top +  (vertical ? 0 : bounds.height()));
		}
		return position;
	}


	public void setAntiAlias(boolean enabled) {
		mTextPaint.setAntiAlias(enabled);
		mBorderPaint.setAntiAlias(enabled);
	}

	protected void setMode(boolean grayed) { 
		mTextPaint.setColor(grayed ? colorGrayed : colorNormal);
		mTextPaint.setAlpha(255);
		
		mBorderPaint.setColor(grayed ? colorBackgroundGrayed: colorBackgroundNormal);
		mBorderPaint.setAlpha(255);
		
	}

	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(mPointFirstLine.x, mPointFirstLine.y);
		if(mVertical){
			canvas.rotate(-90);
		}
		canvas.drawText(mTextFirstLine, 0, 0, mBorderPaint);
		canvas.drawText(mTextFirstLine, 0, 0, mTextPaint);
		if (mTextSecondLine != null) {
			canvas.translate(mPointSecondLine.x, mPointSecondLine.y);
			canvas.drawText(mTextSecondLine, 0, 0, mBorderPaint);
			canvas.drawText(mTextSecondLine, 0, 0, mTextPaint);
		}
		canvas.restore();
	}

	
}
