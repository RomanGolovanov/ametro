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

import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.model.SubwayLine;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;
import org.ametro.util.DateUtil;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class StationListAdapter implements ListAdapter  {

	private static class ListItemWrapper
	{
		public final TextView Name;
		public final TextView Line;
		public final ImageView Image;
		public final TextView Delay;
		
		public ListItemWrapper(View view) {
			Name = (TextView)view.findViewById(R.id.station_list_item_name);
			Line = (TextView)view.findViewById(R.id.station_list_item_line);
			Image = (ImageView)view.findViewById(R.id.station_list_item_image);
			Delay = (TextView)view.findViewById(R.id.station_list_item_delay);
			view.setTag(this);
		}
	}	
	
	public StationListAdapter(Activity activity, ArrayList<SubwayStation> stations,SubwayMap map){
		mLineDrawabled = new HashMap<SubwayLine, Drawable>();
		mLines = map.lines;
		mStations = (SubwayStation[]) stations.toArray(new SubwayStation[stations.size()]);
		mDelays = null;
		mContextActivity = activity;
		mPaint = new Paint();
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mPaint.setAntiAlias(true);
	}

	public StationListAdapter(Activity activity, ArrayList<SubwayStation> stations, ArrayList<Long> delays, SubwayMap map){
		mLineDrawabled = new HashMap<SubwayLine, Drawable>();
		mLines = map.lines;
		mStations = (SubwayStation[]) stations.toArray(new SubwayStation[stations.size()]);
		mDelays = (Long[]) delays.toArray(new Long[delays.size()]);
		mContextActivity = activity;
		mPaint = new Paint();
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mPaint.setAntiAlias(true);
	}

	
	private final Activity mContextActivity;
	private final SubwayStation[] mStations;
	private final Long[] mDelays;
	private final SubwayLine[] mLines;
	private final Paint mPaint;
	
	private final HashMap<SubwayLine, Drawable> mLineDrawabled;

	public int getCount() {
		return mStations.length;
	}

	public Object getItem(int position) {
		return mStations[position];
	}

	public long getItemId(int position) {
		return mStations[position].id;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ListItemWrapper wrapper = null;
		
		if(convertView==null){
			view = mContextActivity.getLayoutInflater().inflate(R.layout.station_list_item, null);
			wrapper = new ListItemWrapper(view);
			
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}

		final SubwayStation station = mStations[position];
		final SubwayLine line = mLines[station.lineId]; 
		wrapper.Name.setText(station.name);
		wrapper.Line.setText(line.name);
		if(mDelays!=null){
			wrapper.Delay.setText(DateUtil.getLongTime(mDelays[position]));
		}else{
			wrapper.Delay.setText("");
		}
		wrapper.Image.setImageDrawable(getLineIcon(line));
		return view;		
	}

	private Drawable getLineIcon(SubwayLine line) {
		Drawable dw = mLineDrawabled.get(line);
		if(dw == null){
			Bitmap bmp = Bitmap.createBitmap(30, 50, Config.RGB_565);
			Canvas c = new Canvas(bmp);
			mPaint.setColor(line.color);
			c.drawCircle(15, 25, 7, mPaint);
			dw = new BitmapDrawable(bmp);
			mLineDrawabled.put(line, dw);
		}
		return dw;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isEmpty() {
		return mStations == null || mStations.length == 0;
	}

	public void registerDataSetObserver(DataSetObserver arg0) {
	}

	public void unregisterDataSetObserver(DataSetObserver arg0) {
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}
	
	
}
