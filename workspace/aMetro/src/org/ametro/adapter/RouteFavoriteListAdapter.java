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

import java.util.HashMap;

import org.ametro.R;
import org.ametro.model.SubwayLine;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RouteFavoriteListAdapter extends BaseAdapter {

	private static class ListItemWrapper
	{
		public final TextView NameFrom;
		public final ImageView ImageFrom;
		public final TextView NameTo;
		public final ImageView ImageTo;
		
		public ListItemWrapper(View view) {
			NameFrom = (TextView)view.findViewById(R.id.route_favorite_list_item_name_from);
			ImageFrom = (ImageView)view.findViewById(R.id.route_favorite_list_item_image_from);
			NameTo = (TextView)view.findViewById(R.id.route_favorite_list_item_name_to);
			ImageTo = (ImageView)view.findViewById(R.id.route_favorite_list_item_image_to);
			view.setTag(this);
		}
	}	

	public RouteFavoriteListAdapter(Activity activity, Point[] routes, SubwayMap map){
		mLineDrawabled = new HashMap<SubwayLine, Drawable>();
		mLines = map.lines;
		mStations = map.stations;
		mRoutes = routes;
		mContextActivity = activity;
		
		mPaint = new Paint();
		mPaint.setStyle(Style.FILL);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(0);
		
		mSubwayMap = map;
	}
	
	protected static final int ICON_WIDTH = 20;
	protected static final int ICON_HEIGHT = 20;
	protected static final int ICON_DIAMETER = 7;
	
	protected final SubwayMap mSubwayMap;
	protected final Activity mContextActivity;
	protected final HashMap<SubwayLine, Drawable> mLineDrawabled;
	protected final SubwayLine[] mLines;
	protected final SubwayStation[] mStations;
	protected final Paint mPaint;
	protected Integer mTextColor;
	
	protected final Point[] mRoutes;

	public int getCount() {
		return mRoutes.length;
	}

	public void setTextColor(Integer color){
		mTextColor = color;
	}
	
	public static String getStationName(SubwayMap map, SubwayStation station){
		return station.name + " (" + map.lines[station.lineId].name + ")";
	}
	
	public Object getItem(int position) {
		return 
			getStationName(mSubwayMap, mStations[ mRoutes[position].x ] ) 
			+ " - "
			+ getStationName(mSubwayMap, mStations[ mRoutes[position].x ] );
	}

	public Point getRoute(int position){
		return mRoutes[position];
	}
	
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ListItemWrapper wrapper = null;
		
		if(convertView==null){
			view = mContextActivity.getLayoutInflater().inflate(R.layout.route_favorite_list_item, null);
			wrapper = new ListItemWrapper(view);
			if(mTextColor!=null){
				wrapper.NameFrom.setTextColor(mTextColor);
				wrapper.NameTo.setTextColor(mTextColor);
			}
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}

		final Point route = mRoutes[position];
		final SubwayStation stationFrom = mStations[route.x];
		final SubwayStation stationTo = mStations[route.y];
		final SubwayLine lineFrom = mLines[stationFrom.lineId]; 
		final SubwayLine lineTo = mLines[stationTo.lineId]; 
		wrapper.NameFrom.setText(stationFrom.name);
		wrapper.NameTo.setText(stationTo.name);
		wrapper.ImageFrom.setImageDrawable(getItemIcon(lineFrom));
		wrapper.ImageTo.setImageDrawable(getItemIcon(lineTo));
		return view;		
	}

	protected Drawable getItemIcon(SubwayLine line) {
		Drawable dw = mLineDrawabled.get(line);
		if(dw == null){
			Bitmap bmp = Bitmap.createBitmap(ICON_WIDTH, ICON_HEIGHT, Config.ARGB_8888);
			Canvas c = new Canvas(bmp);
			mPaint.setColor(line.color);
			c.drawCircle(ICON_WIDTH/2, ICON_HEIGHT/2, ICON_DIAMETER, mPaint);
			
			dw = new BitmapDrawable(bmp);
			mLineDrawabled.put(line, dw);
		}
		return dw;
	}
	
	
}
