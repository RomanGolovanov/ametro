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
package org.ametro.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.ametro.R;
import org.ametro.adapter.StationListAdapter;
import org.ametro.model.MapView;
import org.ametro.model.StationView;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class StationSearchActivity extends ListActivity {

	private ArrayList<StationView> mStationList;
	
	private static class StationSortComparator implements Comparator<StationView>
	{
		public int compare(StationView first, StationView second) {
			return first.getName().compareTo(second.getName());
		}
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		MapViewActivity.Instance.setNavigationStations(mStationList);
		MapViewActivity.Instance.setCurrentStation(mStationList.get(position));
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
			mStationList = MapViewActivity.Instance.getNavigationStations();
			bindData();
			StationView selected = MapViewActivity.Instance.getCurrentStation();
			if(selected!=null){
				this.setSelection(mStationList.indexOf(selected));
			}
		}
	}

	private void bindData() {
		MapView map = MapViewActivity.Instance.getMapView();
		if(mStationList.size()>0){
			if(mStationList.size()>1){
				ArrayList<String> stationNamesList = new ArrayList<String>();
				for(StationView station : mStationList){
						stationNamesList.add(station.getName() + " (" + map.lines[station.lineViewId].getName() + ")");
				}
				this.setListAdapter(new StationListAdapter(this, mStationList, map));
			}else{
				MapViewActivity.Instance.setNavigationStations(mStationList);
				MapViewActivity.Instance.setCurrentStation(mStationList.get(0));
				finish();
			}
		}else{
			MapViewActivity.Instance.setNavigationStations(null);
			Toast.makeText(this, getString(R.string.msg_station_not_found), Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private MapView doSearchKeywords(String searchKeywords) {
		MapView map = MapViewActivity.Instance.getMapView();
		mStationList = new ArrayList<StationView>();
		for(StationView station : map.stations){
			if(station.getName().toLowerCase().indexOf(searchKeywords)!=-1){
				mStationList.add(station);
			}
		}
		Collections.sort(mStationList, new StationSortComparator());
		return map;
	}
}
