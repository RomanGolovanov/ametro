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
package org.ametro.adapter;

import org.ametro.model.SubwayLine;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayRoute;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class RouteListViewAdapter extends StationListAdapter {

	protected final Paint mBackgroundPaint;
	
	public RouteListViewAdapter(Activity activity, SubwayRoute route, SubwayMap map) {
		super(activity, route.getStations(), route.getDelays(), map);
		mBackgroundPaint = new Paint();
		mBackgroundPaint.setStyle(Style.STROKE);
		mBackgroundPaint.setAntiAlias(true);
		mBackgroundPaint.setColor(Color.BLACK);
		mBackgroundPaint.setStrokeWidth(2);
	}

	protected Drawable getLineIcon(SubwayLine line) {
		Drawable dw = mLineDrawabled.get(line);
		if(dw == null){
			Bitmap bmp = Bitmap.createBitmap(30, 50, Config.ARGB_8888);
			Canvas c = new Canvas(bmp);
			mPaint.setColor(line.color);
			c.drawRect(10-1,0,20+1,50, mPaint);
			c.drawCircle(15, 25, 9, mPaint);
			c.drawCircle(15, 25, 9, mBackgroundPaint);
			dw = new BitmapDrawable(bmp);
			mLineDrawabled.put(line, dw);
		}
		return dw;
	}	
}
