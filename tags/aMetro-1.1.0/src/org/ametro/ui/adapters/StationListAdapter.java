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

import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.model.LineView;
import org.ametro.model.SchemeView;
import org.ametro.model.StationView;
import org.ametro.util.DateUtil;
import org.ametro.util.StringUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
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
					if(StringUtil.startsWithoutDiacritics(name,prefix)){
						stations.add(station);
						if(allDelays!=null){
							delays.add(allDelays[i]);
						}
					}else{
				        final String[] words = name.split(" ");
				        final int wordsCount = words.length;

				        for (int k = 0; k < wordsCount; k++) {
				            if (StringUtil.startsWithoutDiacritics(words[k],prefix)) {
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
	
	public static class ListItemWrapper
	{
		public final TextView Name;
		public final TextView Line;
		public final ImageView StationImage;
		public final ImageView StationImageShadow;
		public final ImageView LineImage;
		public final TextView Delay;
		
		public ListItemWrapper(View view) {
			Name = (TextView)view.findViewById(R.id.station_name);
			Line = (TextView)view.findViewById(R.id.line_name);
			StationImage = (ImageView)view.findViewById(R.id.station_image);
			StationImageShadow = (ImageView)view.findViewById(R.id.station_image_shadow);
			LineImage = (ImageView)view.findViewById(R.id.line_image);
			Delay = (TextView)view.findViewById(R.id.delay);
			view.setTag(this);
		}
	}	

	public StationListAdapter(Context activity, ArrayList<StationView> stations,SchemeView map){
		this(activity, stations,null,map);
	}

	public StationListAdapter(Context context, ArrayList<StationView> stations, ArrayList<Long> delays, SchemeView map){
		this(context
			, (StationView[]) stations.toArray(new StationView[stations.size()])
			, delays==null ? null : (Long[]) delays.toArray(new Long[delays.size()])
			, map);
	}

	public StationListAdapter(Context context, StationView[] stations, SchemeView map){
		this(context, stations, null, map);
	}

	public StationListAdapter(Context context, StationView[] stations, Long[] delays, SchemeView map){
		mInflater = LayoutInflater.from(context);
		mLineDrawabled = new HashMap<LineView, Drawable>();
		mLines = map.lines;
		mStations = stations;// (StationView[]) stations.toArray(new StationView[stations.size()]);
		mDelays = delays;// (Long[]) delays.toArray(new Long[delays.size()]);
		mContext = context;
		
		mFilteredStations = mStations;
		mFilteredDelays = mDelays;
		mMapView = map;
	}
	
	protected final SchemeView mMapView;
	protected final Context mContext;
	protected LayoutInflater mInflater;
	protected final HashMap<LineView, Drawable> mLineDrawabled;
	protected final LineView[] mLines;
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
	
	public static String getStationName(SchemeView map, StationView station){
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
			view = mInflater.inflate(R.layout.station_list_item, null);
			wrapper = new ListItemWrapper(view);
			if(mTextColor!=null){
				wrapper.Name.setTextColor(mTextColor);
			}
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}
		setListItemView(wrapper, position);
		return view;		
	}

	protected void setListItemView(ListItemWrapper wrapper, int position){
		final StationView station = mFilteredStations[position];
		final LineView line = mLines[station.lineViewId]; 
		wrapper.Name.setText(station.getName());
		wrapper.Line.setText(line.getName());
		if(mFilteredDelays!=null){
			wrapper.Delay.setText(DateUtil.getTimeHHMM(mFilteredDelays[position]));
		}else{
			wrapper.Delay.setText("");
		}


		
		
		GradientDrawable stationDrawable = (GradientDrawable)wrapper.StationImage.getDrawable();
		stationDrawable.setColor(0xFF000000 | line.lineColor);
		
		GradientDrawable lineDrawable = (GradientDrawable)wrapper.LineImage.getDrawable();
		lineDrawable.setColor(0xFF000000 | line.lineColor);
		
		//wrapper.StationImage.setColorFilter(0xFF000000 | line.lineColor, Mode.SRC_ATOP);
		//wrapper.LineImage.setColorFilter(0xFF000000 | line.lineColor, Mode.SRC_ATOP);
	}
	
	public Filter getFilter() {
		return new StationFilter();
	}
	
	
}
