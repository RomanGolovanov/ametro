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
package org.ametro.ui.adapters;

import java.util.HashMap;

import org.ametro.R;
import org.ametro.model.LineView;
import org.ametro.model.SchemeView;
import org.ametro.model.StationView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FavoriteRoutesListAdapter extends BaseAdapter implements OnClickListener {

	private static class ListItemWrapper
	{
		public final TextView NameFrom;
		public final ImageView ImageFrom;
		public final TextView NameTo;
		public final ImageView ImageTo;
		public final ImageView Delete;
		
		public ListItemWrapper(View view) {
			NameFrom = (TextView)view.findViewById(R.id.route_favorite_list_item_name_from);
			ImageFrom = (ImageView)view.findViewById(R.id.route_favorite_list_item_image_from);
			NameTo = (TextView)view.findViewById(R.id.route_favorite_list_item_name_to);
			ImageTo = (ImageView)view.findViewById(R.id.route_favorite_list_item_image_to);
			Delete = (ImageView)view.findViewById(R.id.route_favorite_list_item_delete);
			view.setTag(this);
		}
	}	

	public FavoriteRoutesListAdapter(Activity activity, Point[] routes, SchemeView map){
		mLineDrawabled = new HashMap<LineView, Drawable>();
		mLines = map.lines;
		mStations = map.stations;
		mRoutes = routes;
		mChecked = new boolean[routes.length];
		mContextActivity = activity;
		
		mPaint = new Paint();
		mPaint.setStyle(Style.FILL);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(0);
		
		mMapView = map;
		
		mIsCheckboxesVisible = false;
	}
	
	protected static final int ICON_WIDTH = 20;
	protected static final int ICON_HEIGHT = 20;
	protected static final int ICON_DIAMETER = 7;
	
	protected final SchemeView mMapView;
	protected final Activity mContextActivity;
	protected final HashMap<LineView, Drawable> mLineDrawabled;
	protected final LineView[] mLines;
	protected final StationView[] mStations;
	protected final Paint mPaint;
	protected Integer mTextColor;
	
	protected final Point[] mRoutes;
	protected final boolean[] mChecked;
	protected boolean mIsCheckboxesVisible;

	public void setCheckboxesVisible(boolean show){
		mIsCheckboxesVisible = show;
		notifyDataSetInvalidated();
	}
	
	public boolean isCheckboxesVisible(){
		return mIsCheckboxesVisible;
	}
	
	public boolean[] getChecked(){
		return mChecked;
	}
	
	public int getCount() {
		return mRoutes.length;
	}

	public void setTextColor(Integer color){
		mTextColor = color;
	}
	
	public static String getStationName(SchemeView map, StationView station){
		return station.getName() + " (" + map.lines[station.lineViewId].getName() + ")";
	}
	
	public Object getItem(int position) {
		return 
			getStationName(mMapView, mStations[ mRoutes[position].x ] ) 
			+ " - "
			+ getStationName(mMapView, mStations[ mRoutes[position].x ] );
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
			view = mContextActivity.getLayoutInflater().inflate(R.layout.favorite_route_list_item, null);
			wrapper = new ListItemWrapper(view);
			if(mTextColor!=null){
				wrapper.NameFrom.setTextColor(mTextColor);
				wrapper.NameTo.setTextColor(mTextColor);
			}
			wrapper.Delete.setOnClickListener(this);
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}

		final Point route = mRoutes[position];
		final StationView stationFrom = mStations[route.x];
		final StationView stationTo = mStations[route.y];
		final LineView lineFrom = mLines[stationFrom.lineViewId]; 
		final LineView lineTo = mLines[stationTo.lineViewId]; 
		wrapper.NameFrom.setText(stationFrom.getName());
		wrapper.NameTo.setText(stationTo.getName());
		
		wrapper.ImageFrom.setColorFilter(0xFF000000 | lineFrom.lineColor, Mode.SRC);
		wrapper.ImageTo.setColorFilter(0xFF000000 | lineTo.lineColor, Mode.SRC);
		
		//wrapper.ImageFrom.setImageDrawable(getItemIcon(lineFrom));
		//wrapper.ImageTo.setImageDrawable(getItemIcon(lineTo));
		
		wrapper.Delete.setVisibility(mIsCheckboxesVisible ? View.VISIBLE : View.GONE);
		if(mIsCheckboxesVisible){
			wrapper.Delete.setBackgroundResource( mChecked[position] ? R.drawable.icon_delete : R.drawable.icon_delete_disabled );
			wrapper.Delete.setTag(position);
		}
		return view;		
	}

	protected Drawable getItemIcon(LineView line) {
		Drawable dw = mLineDrawabled.get(line);
		if(dw == null){
			Bitmap bmp = Bitmap.createBitmap(ICON_WIDTH, ICON_HEIGHT, Config.ARGB_8888);
			Canvas c = new Canvas(bmp);
			mPaint.setColor(0xFF000000 | line.lineColor);
			c.drawCircle(ICON_WIDTH/2, ICON_HEIGHT/2, ICON_DIAMETER, mPaint);
			
			dw = new BitmapDrawable(bmp);
			mLineDrawabled.put(line, dw);
		}
		return dw;
	}

	public void onClick(View v) {
		int position = (Integer)v.getTag();
		toggleCheckbox(position);
	}

	public void toggleCheckbox(int position) {
		mChecked[position] = !mChecked[position];
		notifyDataSetChanged();
	}
	
}
