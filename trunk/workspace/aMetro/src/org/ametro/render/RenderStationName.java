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

package org.ametro.render;

import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import org.ametro.model.SubwayLine;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;

public class RenderStationName extends RenderElement {

	private boolean mVertical;

	private Paint mTextPaint;
	private Paint mBorderPaint;

	private String mTextFirstLine;
	private Point mPointFirstLine;

	private String mTextSecondLine;
	private Point mPointSecondLine;

	public RenderStationName(SubwayMap subwayMap, SubwayStation station) {
		final boolean isUpperCase = subwayMap.upperCase;
		final boolean isWordWrap = subwayMap.wordWrap;

		final String text = isUpperCase ? station.name.toUpperCase() : station.name;
		final int textLength = text.length();
		final Rect textRect = station.rect;
		final Point point = station.point;

		final SubwayLine line = subwayMap.lines[station.lineId];
		final int textColor = line.labelColor;
		final int backColor = line.labelBgColor;

		final Paint textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setTypeface(Typeface.DEFAULT);
		textPaint.setFakeBoldText(true);
		textPaint.setTextSize(10);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setColor(textColor);
		textPaint.setStyle(Style.FILL);

		final Paint fillPaint = new Paint(textPaint);
		fillPaint.setColor(backColor != 0 ? backColor : Color.WHITE);
		fillPaint.setStyle(Style.STROKE);
		fillPaint.setStrokeWidth(3);
		
		final boolean vertical = textRect.width() < textRect.height();

		final Rect rect = vertical 
			? new Rect(textRect.left, textRect.bottom, textRect.left + textRect.height(), textRect.bottom + textRect.width()) 
			: new Rect(textRect);
		final Align align = vertical 
			? ((point.y > textRect.centerY()) ? Align.LEFT : Align.RIGHT)
			: ((point.x > textRect.centerX() ? Align.RIGHT : Align.LEFT));
		

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
		mBorderPaint = fillPaint;
		
		mVertical = vertical;
		mTextPaint.setTextAlign(align);
		mBorderPaint.setTextAlign(align);

		Rect box = new Rect(textRect);
		setProperties(RenderProgram.TYPE_STATION_NAME, box);
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
		mTextPaint.setAlpha(grayed ? 80 : 255);
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
