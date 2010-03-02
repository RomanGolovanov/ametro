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
package org.ametro.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.ametro.MapSettings;
import org.ametro.model.SubwayMap;
import org.ametro.model.SubwayStation;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SearchStation extends ListActivity {

	private ArrayList<SubwayStation> mStationList;
	
	private static class StationSortComparator implements Comparator<SubwayStation>
	{
		public int compare(SubwayStation first, SubwayStation second) {
			return first.name.compareTo(second.name);
		}
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		BrowseVectorMap.Instance.setNavigationStations(mStationList);
		BrowseVectorMap.Instance.setCurrentStation(mStationList.get(position));
		finish();
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			String searchKeywords = queryIntent.getStringExtra(SearchManager.QUERY).toLowerCase();
			doSearchKeywords(searchKeywords);
			bindData();
		}else{
			mStationList = BrowseVectorMap.Instance.getNavigationStations();
			bindData();
			SubwayStation selected = BrowseVectorMap.Instance.getCurrentStation();
			if(selected!=null){
				this.setSelection(mStationList.indexOf(selected));
			}
		}
	}

	private void bindData() {
		SubwayMap map = MapSettings.getModel();
		if(mStationList.size()>0){
			if(mStationList.size()>1){
				ArrayList<String> stationNamesList = new ArrayList<String>();
				for(SubwayStation station : mStationList){
						stationNamesList.add(station.name + " (" + map.getLine(station.lineId).name + ")");
				}
				this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stationNamesList));
			}else{
				BrowseVectorMap.Instance.setNavigationStations(mStationList);
				BrowseVectorMap.Instance.setCurrentStation(mStationList.get(0));
				finish();
			}
		}else{
			BrowseVectorMap.Instance.setNavigationStations(null);
			Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private SubwayMap doSearchKeywords(String searchKeywords) {
		SubwayMap map = MapSettings.getModel();
		mStationList = new ArrayList<SubwayStation>();
		for(SubwayStation station : map.stations){
			if(station.name.toLowerCase().indexOf(searchKeywords)!=-1){
				mStationList.add(station);
			}
		}
		Collections.sort(mStationList, new StationSortComparator());
		return map;
	}
}
