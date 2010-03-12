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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class StationListAdapter extends BaseAdapter implements Filterable {

	public class StationFilter extends Filter
	{
		private class ResultContainer
		{
			public SubwayStation[] Stations; 
			public Long[] Delays;
		}
		
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			ArrayList<SubwayStation> stations = new ArrayList<SubwayStation>();
			ArrayList<Long> delays = new ArrayList<Long>();
			
			final SubwayStation[] allStations = mStations;
			final Long[] allDelays = mDelays;
			final int length = allStations.length;
			SubwayStation station;
			Long delay = null;

			final String text = constraint!=null ? constraint.toString().toLowerCase() : null; 
			
			for(int i = 0; i < length; i++){
				station = allStations[i];
				if(allDelays!=null){
					delay = allDelays[i];
				}
				if(text==null || text.length()==0){
					stations.add(station);
					if(allDelays!=null){
						delays.add(delay);
					}
				}else{
					final String name = station.name.toLowerCase();
					if(name.contains(text)){
						stations.add(station);
						if(allDelays!=null){
							delays.add(delay);
						}
					}
				}
			}

			ResultContainer container = new ResultContainer();
			container.Stations = (SubwayStation[]) stations.toArray(new SubwayStation[stations.size()]);
			if(allDelays!=null){
				container.Delays = (Long[]) delays.toArray(new Long[delays.size()]);
			}
			results.values = container;
			results.count = stations.size();
			return results;
		}

		protected void publishResults(CharSequence constraint, FilterResults results) {
			ResultContainer container = (ResultContainer)results.values;
			mFilteredStations = container.Stations;
			mFilteredDelays = container.Delays;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
		}
		
	}
	
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
		this(activity, stations,null,map);
	}

	public StationListAdapter(Activity activity, ArrayList<SubwayStation> stations, ArrayList<Long> delays, SubwayMap map){
		this(activity
			, (SubwayStation[]) stations.toArray(new SubwayStation[stations.size()])
			, delays==null ? null : (Long[]) delays.toArray(new Long[delays.size()])
			, map);
	}

	public StationListAdapter(Activity activity, SubwayStation[] stations, SubwayMap map){
		this(activity, stations, null, map);
	}

	public StationListAdapter(Activity activity, SubwayStation[] stations, Long[] delays, SubwayMap map){
		mLineDrawabled = new HashMap<SubwayLine, Drawable>();
		mLines = map.lines;
		mStations = stations;// (SubwayStation[]) stations.toArray(new SubwayStation[stations.size()]);
		mDelays = delays;// (Long[]) delays.toArray(new Long[delays.size()]);
		mContextActivity = activity;
		
		mPaint = new Paint();
		mPaint.setStyle(Style.FILL);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(0);
		
		mFilteredStations = mStations;
		mFilteredDelays = mDelays;
		mSubwayMap = map;
	}
	
	protected final SubwayMap mSubwayMap;
	protected final Activity mContextActivity;
	protected final HashMap<SubwayLine, Drawable> mLineDrawabled;
	protected final SubwayLine[] mLines;
	protected final Paint mPaint;
	protected Integer mTextColor;
	
	protected final SubwayStation[] mStations;
	protected final Long[] mDelays;
	
	protected SubwayStation[] mFilteredStations;
	protected Long[] mFilteredDelays;
	

	public int getCount() {
		return mFilteredStations.length;
	}

	public void setTextColor(Integer color){
		mTextColor = color;
	}
	
	public static String getStationName(SubwayMap map, SubwayStation station){
		return station.name + " (" + map.lines[station.lineId].name + ")";
	}
	
	public Object getItem(int position) {
		return getStationName(mSubwayMap, mFilteredStations[position]);
	}

	public SubwayStation getStation(int position){
		return mFilteredStations[position];
	}
	
	public long getItemId(int position) {
		return mFilteredStations[position].id;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ListItemWrapper wrapper = null;
		
		if(convertView==null){
			view = mContextActivity.getLayoutInflater().inflate(R.layout.station_list_item, null);
			wrapper = new ListItemWrapper(view);
			if(mTextColor!=null){
				wrapper.Name.setTextColor(mTextColor);
				wrapper.Line.setTextColor(mTextColor);
			}
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}

		final SubwayStation station = mFilteredStations[position];
		final SubwayLine line = mLines[station.lineId]; 
		wrapper.Name.setText(station.name);
		wrapper.Line.setText(line.name);
		if(mFilteredDelays!=null){
			wrapper.Delay.setText(DateUtil.getTimeHHMM(mFilteredDelays[position]));
		}else{
			wrapper.Delay.setText("");
		}
		wrapper.Image.setImageDrawable(getLineIcon(line));
		return view;		
	}

	protected Drawable getLineIcon(SubwayLine line) {
		Drawable dw = mLineDrawabled.get(line);
		if(dw == null){
			Bitmap bmp = Bitmap.createBitmap(30, 50, Config.ARGB_8888);
			Canvas c = new Canvas(bmp);
			mPaint.setColor(line.color);
			c.drawCircle(15, 25, 9, mPaint);
			
			dw = new BitmapDrawable(bmp);
			mLineDrawabled.put(line, dw);
		}
		return dw;
	}

	public Filter getFilter() {
		return new StationFilter();
	}
	
	
}
