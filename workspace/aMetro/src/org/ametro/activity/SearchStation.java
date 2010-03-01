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
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		BrowseVectorMap.Instance.setSelectedStations(mStationList);
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
			mStationList = BrowseVectorMap.Instance.getSelectedStations();
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
				BrowseVectorMap.Instance.setSelectedStations(mStationList);
				BrowseVectorMap.Instance.setCurrentStation(mStationList.get(0));
				finish();
			}
		}else{
			Toast.makeText(BrowseVectorMap.Instance, "Not found", Toast.LENGTH_SHORT).show();
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
