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

    private Paint mTextPaint;
    private Paint mBorderPaint;

    private String mTextFirstLine;
    private Path mPathFirstLine;
    private Rect mRectFirstLine;
    private Point mPointFirstLine;

    private String mTextSecondLine;
    private Path mPathSecondLine;
    private Rect mRectSecondLine;
    private Point mPointSecondLine;

    public RenderStationName(SubwayMap subwayMap, SubwayStation station) {
        final boolean isUpperCase = subwayMap.upperCase;
        final boolean isWordWrap = subwayMap.wordWrap;

        final String text = isUpperCase ? station.name.toUpperCase() : station.name;
        final int textLength = text.length();
        final Rect rect = station.rect;
        final Point point = station.point;

        final SubwayLine line = subwayMap.lines[station.lineId];
        final int textColor = line.labelColor;
        final int backColor = line.labelBgColor;

        initializePaints(textColor, backColor);

        if (rect.width() > rect.height()) {
            // horizontal text
            final Rect bounds = new Rect();
            mTextPaint.getTextBounds(text, 0, textLength, bounds);
            boolean isNeedSecondLine = bounds.width() > rect.width() && isWordWrap;
            int spacePosition = -1;
            if (isNeedSecondLine) {
                spacePosition = text.indexOf(' ');
                isNeedSecondLine = spacePosition != -1;
            }
            if (isNeedSecondLine) {
                final String firstText = text.substring(0, spacePosition);
                final int firstLength = firstText.length();

                final String secondText = text.substring(spacePosition + 1);
                final int secondLength = secondText.length();

                final Rect secondRect = new Rect(
                        rect.left,
                        rect.top + bounds.height() + 2,
                        rect.right,
                        rect.bottom + bounds.height() + 2);

                initializeFirstHorizontalLine(firstText, firstLength, rect, point);
                initializeSecondHorizontalLine(secondText, secondLength, secondRect, point);


            } else {
                initializeFirstHorizontalLine(text, textLength, rect, point);
            }


        } else {
            // vertical text
            initializeVerticalText(text, textLength, rect, point);
        }

        Rect box = new Rect(mRectFirstLine);
        if (mRectSecondLine != null) {
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
        if (point.x > rect.centerX()) { // align to right
            mTextPaint.setTextAlign(Align.RIGHT);
            mBorderPaint.setTextAlign(Align.RIGHT);
            mTextPaint.getTextBounds(text, 0, textLength, bounds);
            fill.set(right - bounds.width() - 1, top, right + 2, top + bounds.height() + 1);
            position.set(right, top + bounds.height());

        } else { // align to left
            mTextPaint.setTextAlign(Align.LEFT);
            mBorderPaint.setTextAlign(Align.LEFT);
            mTextPaint.getTextBounds(text, 0, textLength, bounds);
            fill.set(left - 1, top, left + bounds.width() + 2, top + bounds.height() + 1);
            position.set(left, top + bounds.height());
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
        if (point.x > rect.centerX()) { // align to right
            mTextPaint.setTextAlign(Align.RIGHT);
            mBorderPaint.setTextAlign(Align.RIGHT);
            mTextPaint.getTextBounds(text, 0, textLength, bounds);
            fill.set(right - bounds.width() - 1, top, right + 2, top + bounds.height() + 1);
            position.set(right, top + bounds.height());

        } else { // align to left
            mTextPaint.setTextAlign(Align.LEFT);
            mBorderPaint.setTextAlign(Align.LEFT);
            mTextPaint.getTextBounds(text, 0, textLength, bounds);
            fill.set(left - 1, top, left + bounds.width() + 2, top + bounds.height() + 1);
            position.set(left, top + bounds.height());
        }
        mTextFirstLine = text;
        mRectFirstLine = fill;
        mPathFirstLine = null;
        mPointFirstLine = position;
    }

    private void initializePaints(final int textColor, final int backColor) {
        final Paint textPaint = new Paint();

        textPaint.setSubpixelText(false);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(10);
        textPaint.setTextAlign(Align.LEFT);
        textPaint.setColor(textColor);
        textPaint.setStyle(Style.FILL);
        mTextPaint = textPaint;

        final Paint fillPaint = new Paint(textPaint);
        fillPaint.setColor(backColor != 0 ? backColor : Color.WHITE);
        fillPaint.setStyle(Style.STROKE);
        fillPaint.setStrokeWidth(3);
        mBorderPaint = fillPaint;
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
        if (point.y > rect.centerY()) {
            textRect = new Rect(right - textHeight, bottom - textWidth - 20, right, bottom - 5);

        } else {
            textRect = new Rect(right - textHeight, top + 5, right, top + textWidth + 20);
        }
        textPath.moveTo(textRect.right, textRect.bottom);
        textPath.lineTo(textRect.right, textRect.top);

        mTextFirstLine = text;
        mRectFirstLine = textRect;
        mPathFirstLine = textPath;
        mPointFirstLine = null;
    }

    public void setAntiAlias(boolean enabled)
    {
    	mTextPaint.setAntiAlias(enabled);
    	mBorderPaint.setAntiAlias(enabled);
    }
    
    protected void setMode(boolean grayed)
    {
    	mTextPaint.setAlpha(grayed ?  80 : 255);
    }
    
    public void draw(Canvas canvas) {

        if (mPointFirstLine != null) {
            canvas.drawText(mTextFirstLine, mPointFirstLine.x, mPointFirstLine.y, mBorderPaint);
            canvas.drawText(mTextFirstLine, mPointFirstLine.x, mPointFirstLine.y, mTextPaint);
        } else {
            canvas.drawTextOnPath(mTextFirstLine, mPathFirstLine, 0, 0, mBorderPaint);
            canvas.drawTextOnPath(mTextFirstLine, mPathFirstLine, 0, 0, mTextPaint);
        }

        if (mTextSecondLine != null) {
            if (mPointSecondLine != null) {
                canvas.drawText(mTextSecondLine, mPointSecondLine.x, mPointSecondLine.y, mBorderPaint);
                canvas.drawText(mTextSecondLine, mPointSecondLine.x, mPointSecondLine.y, mTextPaint);
            } else {
                canvas.drawTextOnPath(mTextSecondLine, mPathSecondLine, 0, 0, mBorderPaint);
                canvas.drawTextOnPath(mTextSecondLine, mPathSecondLine, 0, 0, mTextPaint);
            }
        }

    }

}
