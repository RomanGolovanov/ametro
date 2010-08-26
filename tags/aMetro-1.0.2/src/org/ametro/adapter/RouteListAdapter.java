/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
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
package org.ametro.adapter;

import org.ametro.model.LineView;
import org.ametro.model.MapView;
import org.ametro.model.route.RouteView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class RouteListAdapter extends StationListAdapter {

	protected final Paint mBackgroundPaint;

	protected static final int ICON_DIAMETER = 9;
	
	private Drawable mFirstItemDrawable;
	private Drawable mLastItemDrawable;

	public RouteListAdapter(Activity activity, RouteView route,
			MapView map) {
		super(activity, route.getStations(), route.getDelays(), map);
		mBackgroundPaint = new Paint();
		mBackgroundPaint.setStyle(Style.STROKE);
		mBackgroundPaint.setAntiAlias(true);
		mBackgroundPaint.setColor(Color.BLACK);
		mBackgroundPaint.setStrokeWidth(2);
	}

	protected Drawable getItemIcon(int position) {
		final LineView line = mLines[mFilteredStations[position].lineViewId];
		final int color = 0xFF000000 | line.lineColor;
		Drawable dw = null;
		if (position == 0) {
			dw = mFirstItemDrawable;
			if (dw == null) {
				dw = createItemDrawable(color, ICON_HALF_HEIGHT, ICON_HEIGHT);
				mFirstItemDrawable = dw;
			}
		} else if (position == (mFilteredStations.length - 1)) {
			dw = mLastItemDrawable;
			if (dw == null) {
				dw = createItemDrawable(color, 0, ICON_HALF_HEIGHT);
				mLastItemDrawable = dw;
			}
		} else {
			dw = mLineDrawabled.get(line);
			if (dw == null) {
				dw = createItemDrawable(color, 0, ICON_HEIGHT);
				mLineDrawabled.put(line, dw);
			}
		}
		return dw;
	}

	private Drawable createItemDrawable(final int color, int ymin, int ymax) {
		Drawable dw;
		Bitmap bmp = Bitmap.createBitmap(ICON_WIDTH, ICON_HEIGHT,
				Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		mPaint.setColor(color);
		c.drawRect(ICON_HALF_WIDTH - ICON_LINE_WITH, ymin, ICON_HALF_WIDTH + ICON_LINE_WITH, ymax, mPaint);
		c.drawCircle(ICON_HALF_WIDTH, ICON_HALF_HEIGHT, ICON_DIAMETER, mPaint);
		c.drawCircle(ICON_HALF_WIDTH, ICON_HALF_HEIGHT, ICON_DIAMETER,
				mBackgroundPaint);
		dw = new BitmapDrawable(bmp);
		return dw;
	}
}
