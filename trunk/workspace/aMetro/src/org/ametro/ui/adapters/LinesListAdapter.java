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
import java.util.Collections;

import org.ametro.R;
import org.ametro.model.LineView;
import org.ametro.model.SchemeView;
import org.ametro.model.StationView;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LinesListAdapter extends BaseAdapter {

	private static class ListItemWrapper {
		public final TextView Name;
		public final ImageView StationImage;
		public final ImageView LineImage;
		
		public ListItemWrapper(View view) {
			Name = (TextView)view.findViewById(R.id.station_name);
			StationImage = (ImageView)view.findViewById(R.id.station_image);
			LineImage = (ImageView)view.findViewById(R.id.line_image);
			view.setTag(this);
		}
	}	

	private static class ListItem implements Comparable<ListItem> {
		private LineView mLineView;
		
		public int getId() {
			return mLineView.id;
		}
		
		public int getColor() {
			return mLineView.lineColor;
		}
		
		public String getName() {
			return mLineView.getName();
		}
		
		public LineView getLineView() {
			return mLineView;
		}
		
		public ListItem(LineView lineView) {
			super();
			mLineView = lineView;
		}

		public int compareTo(ListItem another) {
			return getName().compareTo(another.getName());
		}
	}
	
	public LinesListAdapter(Context context, SchemeView map){
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mMapView = map;
		mLines = createListItems(map);
	}
	
	private ListItem[] createListItems(SchemeView map) {
		ArrayList<ListItem> items = new ArrayList<LinesListAdapter.ListItem>();
		for(LineView line : map.lines){
			ArrayList<StationView> stations = line.getStations(mMapView);
			if(stations!=null && stations.size()>1){
				items.add(new ListItem(line));
			}
		}
		Collections.sort(items);
		return (ListItem[]) items.toArray(new ListItem[items.size()]);
	}

	protected final SchemeView mMapView;
	protected final Context mContext;
	protected LayoutInflater mInflater;
	
	protected final ListItem[] mLines;
	
	public int getCount() {
		return mLines.length;
	}

	public Object getItem(int position) {
		return mLines[position].getLineView();
	}

	public long getItemId(int position) {
		return mLines[position].getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ListItemWrapper wrapper = null;
		
		if(convertView==null){
			view = mInflater.inflate(R.layout.line_list_item, null);
			wrapper = new ListItemWrapper(view);
		}else{
			view = convertView;
			wrapper = (ListItemWrapper)view.getTag();
		}
		setListItemView(wrapper, position);
		return view;		
	}

	protected void setListItemView(ListItemWrapper wrapper, int position){
		final ListItem line = mLines[position]; 
		wrapper.Name.setText(line.getName());
		
		GradientDrawable stationDrawable = (GradientDrawable)wrapper.StationImage.getDrawable();
		stationDrawable.setColor(0xFF000000 | line.getColor());
		
		GradientDrawable lineDrawable = (GradientDrawable)wrapper.LineImage.getDrawable();
		lineDrawable.setColor(0xFF000000 | line.getColor());
	}
	
}
