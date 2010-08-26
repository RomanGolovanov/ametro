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

import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.model.LineView;
import org.ametro.model.MapView;
import org.ametro.model.StationView;
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
			public StationView[] Stations; 
			public Long[] Delays;
		}

		protected FilterResults performFiltering(CharSequence constraint) {
			final StationView[] allStations = mStations;
			final Long[] allDelays = mDelays;

			if(constraint==null || constraint.length() == 0){
				ResultContainer container = new ResultContainer();
				container.Stations = allStations;
				container.Delays = allDelays;
				FilterResults results = new FilterResults();
				results.values = container;
				results.count = allStations.length;
				return results;
			}else{
				final String prefix = constraint.toString().toLowerCase();
				final ArrayList<StationView> stations = new ArrayList<StationView>();
				final ArrayList<Long> delays = new ArrayList<Long>();
				final ArrayList<StationView> stationsAtEnd = new ArrayList<StationView>();
				final ArrayList<Long> delaysAtEnd = new ArrayList<Long>();
				final int length = allStations.length;
				StationView station;
				
				for(int i = 0; i < length; i++){
					station = allStations[i];
					final String name = station.getName().toLowerCase();
					if(name.startsWith(prefix)){
						stations.add(station);
						if(allDelays!=null){
							delays.add(allDelays[i]);
						}
					}else{
				        final String[] words = name.split(" ");
				        final int wordsCount = words.length;

				        for (int k = 0; k < wordsCount; k++) {
				            if (words[k].startsWith(prefix)) {
				            	stationsAtEnd.add(station);
								if(allDelays!=null){
									delaysAtEnd.add(allDelays[i]);
								}
				                break;
				            }
				        }						
					}
				}	
				stations.addAll(stationsAtEnd);
				delays.addAll(delaysAtEnd);
				ResultContainer container = new ResultContainer();
				container.Stations = (StationView[]) stations.toArray(new StationView[stations.size()]);
				if(allDelays!=null){
					container.Delays = (Long[]) delays.toArray(new Long[delays.size()]);
				}
				FilterResults results = new FilterResults();
				results.values = container;
				results.count = stations.size();
				return results;
			}
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

	public StationListAdapter(Activity activity, ArrayList<StationView> stations,MapView map){
		this(activity, stations,null,map);
	}

	public StationListAdapter(Activity activity, ArrayList<StationView> stations, ArrayList<Long> delays, MapView map){
		this(activity
			, (StationView[]) stations.toArray(new StationView[stations.size()])
			, delays==null ? null : (Long[]) delays.toArray(new Long[delays.size()])
			, map);
	}

	public StationListAdapter(Activity activity, StationView[] stations, MapView map){
		this(activity, stations, null, map);
	}

	public StationListAdapter(Activity activity, StationView[] stations, Long[] delays, MapView map){
		mLineDrawabled = new HashMap<LineView, Drawable>();
		mLines = map.lines;
		mStations = stations;// (StationView[]) stations.toArray(new StationView[stations.size()]);
		mDelays = delays;// (Long[]) delays.toArray(new Long[delays.size()]);
		mContextActivity = activity;
		
		mPaint = new Paint();
		mPaint.setStyle(Style.FILL);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(0);
		
		mFilteredStations = mStations;
		mFilteredDelays = mDelays;
		mMapView = map;
	}
	
	protected static final int ICON_WIDTH = 30;
	protected static final int ICON_HEIGHT = 46;
	protected static final int ICON_DIAMETER = 7;
	protected static final int ICON_LINE_WITH = 5;
	protected static final int ICON_HALF_WIDTH = ICON_WIDTH/2;
	protected static final int ICON_HALF_HEIGHT = ICON_HEIGHT/2;
	
	
	
	protected final MapView mMapView;
	protected final Activity mContextActivity;
	protected final HashMap<LineView, Drawable> mLineDrawabled;
	protected final LineView[] mLines;
	protected final Paint mPaint;
	protected Integer mTextColor;
	
	protected final StationView[] mStations;
	protected final Long[] mDelays;
	
	protected StationView[] mFilteredStations;
	protected Long[] mFilteredDelays;
	

	public int getCount() {
		return mFilteredStations.length;
	}

	public void setTextColor(Integer color){
		mTextColor = color;
	}
	
	public static String getStationName(MapView map, StationView station){
		return station.getName() + " (" + map.lines[station.lineViewId].getName() + ")";
	}
	
	public Object getItem(int position) {
		return getStationName(mMapView, mFilteredStations[position]);
	}

	public StationView getStation(int position){
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
				//wrapper.Line.setTextColor(mTextColor);
			}
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}

		final StationView station = mFilteredStations[position];
		final LineView line = mLines[station.lineViewId]; 
		wrapper.Name.setText(station.getName());
		wrapper.Line.setText(line.getName());
		if(mFilteredDelays!=null){
			wrapper.Delay.setText(DateUtil.getTimeHHMM(mFilteredDelays[position]));
		}else{
			wrapper.Delay.setText("");
		}
		wrapper.Image.setImageDrawable(getItemIcon(position));
		return view;		
	}

	protected Drawable getItemIcon(int position) {
		LineView line = mLines[ mFilteredStations[position].lineViewId ];
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

	public Filter getFilter() {
		return new StationFilter();
	}
	
	
}
